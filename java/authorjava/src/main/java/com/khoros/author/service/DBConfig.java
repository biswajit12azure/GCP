package com.khoros.author.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;


@Component
@EnableScheduling
@Log4j2
public class DBConfig {

    @Value("${author.table.schema}")
    private String authorSchema;

    @Value("${author.table.rename}")
    private String authorSchemaRename;

    @Value("${author.table.defaultName}")
    private String authorTableDefaultName;


    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbcTemplate;


//    @Scheduled(cron = "0 1 0 * * ?")
//    public void runTask() {
//        createTableForYesterdayDate();
//    }


    @Transactional
    public void createTableForDate(Date processDate) {
        String tableDate = new SimpleDateFormat("dd_MM_YYYY").format(processDate);
        String tableName = "AUTHOR_" + tableDate;
        // Check if the table already exists
        if (!isTableExists(tableName)) {
            String authorSchemaSQL = authorSchema.replaceFirst("AUTHOR_", tableName);
            entityManager.createNativeQuery(authorSchemaSQL).executeUpdate();
            log.info("Created New Table for Author : " + tableName);
        }
    }


    private boolean isTableExists(String tableName) {
        String query = "SELECT  COUNT(1) FROM " + tableName;
        try {
            jdbcTemplate.queryForObject(query, Integer.class);
            log.info(tableName + " Table Exist");

            return true;
        } catch (Exception ex) {
            log.error("Exception at isTableExists : " + ex.getMessage());
            return false;
        }
    }


    public String getAuthorTableName(Date processDate) {
        String tableDate = new SimpleDateFormat("dd_MM_YYYY").format(processDate);
        String tableName = "AUTHOR_" + tableDate;
        // Check if the table already exists
        if (isTableExists(tableName)) {
            return tableName;
        }
        return null;
    }

}
