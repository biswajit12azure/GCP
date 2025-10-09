package khorospv.tableDeletion;

import khorospv.tableDeletion.service.DropTableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.*;

public class TableDeletionTest {

    private MockMvc mockMvc;
    @Mock
    private JdbcTemplate jdbcTemplate;

    private DropTableService dropTableService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        dropTableService = new DropTableService(jdbcTemplate); // Pass the mocked JdbcTemplate
        ReflectionTestUtils.setField(dropTableService,"tableNamePrefix","author_");
        ReflectionTestUtils.setField(dropTableService,"recordStatus","SELECT COUNT(*) FROM AUTHOR_09_08_2023 WHERE record_status NOT IN ('Completed', 'QUEUE_NOT_CONFIGURED')");
        ReflectionTestUtils.setField(dropTableService,"authorTablesQuery","SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'AUTHOR_%'");
        ReflectionTestUtils.setField(dropTableService,"spotifyIdsQuery","SELECT SPOTIFY_DATA_TIME_INTERVAL_ID FROM AUTHOR_09_08_2023 WHERE record_status NOT IN ('Completed', 'QUEUE_NOT_CONFIGURED')");
        ReflectionTestUtils.setField(dropTableService,"deleteRecordsFromConversationQuery","DELETE FROM conversationTableName WHERE SPOTIFY_DATA_TIME_INTERVAL_ID NOT IN (144L)");
    }


    @Test
    public void testCleanupOldAuthorTables() throws Exception {

        List<String> tableNames = new ArrayList<>();
        tableNames.add("author_09_08_2023");
        List<Long> expectedSpotifyIdsList = Arrays.asList(144L);
        int recordsDeleted=1;
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM AUTHOR_09_08_2023 WHERE record_status NOT IN ('Completed', 'QUEUE_NOT_CONFIGURED')",Integer.class)).thenReturn(1);
        when(jdbcTemplate.queryForList(any(), eq(String.class))).thenReturn(tableNames);

        Mockito.when(jdbcTemplate.queryForList(eq("SELECT SPOTIFY_DATA_TIME_INTERVAL_ID FROM AUTHOR_09_08_2023 WHERE record_status NOT IN ('Completed', 'QUEUE_NOT_CONFIGURED')"),eq(Long.class))).thenReturn(expectedSpotifyIdsList);
        when(jdbcTemplate.update("DELETE FROM conversationTableName WHERE SPOTIFY_DATA_TIME_INTERVAL_ID NOT IN ('144L')",Integer.class)).thenReturn(1);
        when(jdbcTemplate.update(anyString())).thenReturn(1);
        when(jdbcTemplate.update(any(), eq(String.class))).thenReturn(1);

       int expected= 1;

        // Call the method to be tested
        int result=dropTableService.cleanupOldAuthorTables();


        // Add your assertions here based on the expected behavior
        assertEquals(expected,result);
    }






}



