package khorospv.tableDeletion.controller;

import khorospv.tableDeletion.service.DropTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
public class DropTableController {
    @Autowired
    DropTableService dropTableService;


    @PostMapping("/drop-table-author")
    public ResponseEntity<String> deleteTablesOlderThan7Days() throws ParseException {
        int tablesDeleted = dropTableService.cleanupOldAuthorTables();
        if (tablesDeleted > 0) {
            return ResponseEntity.ok("Table deletion completed successfully. Tables deleted: " + tablesDeleted);
        } else {
            return ResponseEntity.ok("No tables older than 7 days to delete.");
        }

    }

    @PostMapping("/truncate")
    public ResponseEntity<String> truncateRecordsOlderThan7Days() {

        int rowsDeleted = dropTableService.truncateRecordsOlderThan7Days();
        if (rowsDeleted > 0) {
            return ResponseEntity.ok("Records older than 7 days have been truncated.");
        } else {
            return ResponseEntity.ok("No records to delete");
        }
    }
}
