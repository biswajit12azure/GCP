package khorospv.tableDeletion.service;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;


import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class DropTableService {

    //private static final Logger logger = Logger.getLogger(DropTableService.class);

    @Value("${khoros.query.getAuthorTables}")
    private String authorTablesQuery;
    @Value("${khoros.master.truncateQuery}")
    private String truncateQuery;

    @Value("${khoros.query.deleteRecordsFromConversation}")
    private String deleteRecordsFromConversationQuery;

    @Value("${khoros.query.getSpotifyIdsFromAuthor}")
    private String spotifyIdsQuery;

    @Value("${author.tableNamePrefix}")
    private String tableNamePrefix;

    @Value("${actionlog.conversationTableNamePrefix}")
    private String conversationTableName;

    @Value("${khoros.query.checkAuthorRecords}")
    private String recordStatus;
    private static JdbcTemplate jdbcTemplate;

    @Autowired
    public DropTableService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static String generateTableName(LocalDate date) {
        return "author_" + date.format(DateTimeFormatter.ofPattern("dd_MM_yyyy"));
    }

    @SneakyThrows
    //this method will clean old 7 days records/tables based on record_status value in author/conversation_action_log table
    public int cleanupOldAuthorTables() throws ParseException {
        //String tablePrefix = "author_";
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Date sevenDaysAgo = cal.getTime();


        int tablesDeleted = 0;
        // taking list of all author table in khoros_test DB
        List<String> tableNamesToDelete = jdbcTemplate.queryForList(authorTablesQuery, String.class);

        for (String tableName : tableNamesToDelete) {
            log.info(tableName);
            Date tableDate = sdf.parse(tableName.substring(tableNamePrefix.length()));
            if (tableDate.before(sevenDaysAgo)) {

                // Check if the record_status field from author table is equal to 'Completed' and 'queue_not_configured'
               String  updatedTableName= recordStatus.replaceAll("tableName", tableName);


                log.info("Query: " + updatedTableName);
                Integer count = jdbcTemplate.queryForObject(updatedTableName, Integer.class);
                log.info("Rows affected: " + count);


                //if count is 0 drop the author table and also corresponding conversation_action_log table
                if (count != null && count == 0){
                    // Construct the corresponding conversation table name based on the date
                    String conversationTableName = "conversation_action_log_" + sdf.format(tableDate);

                    // Drop the author table
                    String dropAuthorSql = "DROP TABLE " + tableName;
                    jdbcTemplate.execute(dropAuthorSql);

                    // Drop the conversation table
                    String dropConversationSql = "DROP TABLE IF EXISTS " + conversationTableName;
                    jdbcTemplate.execute(dropConversationSql);

                } else {
                    //if count greater than 0 then take the spotify_id from author table where record_status is not euqal to complete/queue_not_configured ,
                    // then only truncate records with completed status from author table and also corresponding record from conversation_action_log table
                    // Truncate author table records
                    String truncateAuthorQuery = "DELETE FROM " + tableName + " WHERE record_status IN ('Completed', 'QUEUE_NOT_CONFIGURED')";
                    jdbcTemplate.execute(truncateAuthorQuery);
                    log.info("Truncated table: " + tableName);


                    // Construct the corresponding conversation table name based on the date
                    String conversationTableName = "conversation_action_log_" + sdf.format(tableDate);

                    // Get the SPOTIFY_DATA_TIME_INTERVAL_ID values from the "author" table
                    String spotifyIdsString = getSpotifyIdsFromAuthorTable(tableName);

                    // Delete records from the conversation table
                    tablesDeleted=  deleteRecordsFromConversationTable(conversationTableName, spotifyIdsString);

                }
            }
        }

        return tablesDeleted;
    }


    public String getSpotifyIdsFromAuthorTable(String tableName) {
        // Construct the SELECT query to get SPOTIFY_DATA_TIME_INTERVAL_ID where record_status is not 'Completed' and not 'QUEUE_NOT_CONFIGURED'
        String updatedSpotifyIdsQuery = spotifyIdsQuery.replace("tableName", tableName);
        // Execute the SELECT query to get SPOTIFY_DATA_TIME_INTERVAL_ID values
        List<Long> spotifyIdsList = jdbcTemplate.queryForList(updatedSpotifyIdsQuery, Long.class);
        // Convert the list of Long values to a comma-separated string
        String spotifyIdsString = spotifyIdsList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        return spotifyIdsString;
    }


    public int deleteRecordsFromConversationTable(String conversationTableName, String spotifyIdsString) {
        int recordsDeleted=0;
        if (!spotifyIdsString.isEmpty()) {
            // Construct the DELETE query to truncate records from conversation_action_log with spotifyIdsString
            String newDeleteRecordsFromConversationQuery = deleteRecordsFromConversationQuery
                    .replace("conversationTableName", conversationTableName)
                    .replace("spotifyIdsString", spotifyIdsString);

             recordsDeleted=jdbcTemplate.update(newDeleteRecordsFromConversationQuery);

            log.info("Deleted records from conversation table: " + conversationTableName);
        }
return recordsDeleted;
    }


    public int truncateRecordsOlderThan7Days() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Date sevenDaysAgo = cal.getTime();
        String sevenDaysAgoStr = sdf.format(sevenDaysAgo);

        //execute the truncate query to truncate the records with status completed from mast table
        int rowsDeleted = jdbcTemplate.update(truncateQuery, sevenDaysAgoStr);

        log.info("Deleted " + rowsDeleted + " records older than 7 days from SPOTIFY_DATA_TIME_INTERVALS table ");
        return rowsDeleted;
    }

}



