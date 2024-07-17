package com.fordevs.dynamicqueryengine.controller;

import com.fordevs.dynamicqueryengine.dto.DatabaseCredentials;
import com.fordevs.dynamicqueryengine.service.DatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for handling database navigation requests.
 */
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class DatabaseNavigatorController {

    @Autowired
    private DatabaseService databaseService;

    /**
     * Connects to the database using dynamic data sources.
     *
     * @param databaseCredentials The database credentials provided in the request body.
     * @return ResponseEntity with connection status.
     */
    @PostMapping("/connect-database")
    public ResponseEntity<String> connectToDatabaseDynamically(@RequestBody DatabaseCredentials databaseCredentials) {
        return databaseService.connectToDatabaseDynamically(databaseCredentials);
    }

    /**
     * Lists the tables in the database.
     *
     * @return ResponseEntity with the list of tables.
     */
    @GetMapping("/listTables")
    public ResponseEntity<List<String>> listTables() {
        return databaseService.listTables();
    }

    /**
     * Lists the columns of a table.
     *
     * @param tableName The name of the table.
     * @return ResponseEntity with the list of columns.
     */
    @GetMapping("/columns/{tableName}")
    public ResponseEntity<List<Map<String, Object>>> listColumns(@PathVariable String tableName) {
        return databaseService.listColumns(tableName);
    }

    /**
     * Gets the data of a table with pagination.
     *
     * @param tableName The name of the table.
     * @param page      The page number.
     * @param size      The number of rows per page.
     * @return ResponseEntity with the table data.
     */
    @GetMapping("/data/{tableName}")
    public ResponseEntity<Map<String, Object>> getTableData(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return databaseService.getTableData(tableName, page, size);
    }

    /**
     * Executes a SQL query.
     *
     * @param query The SQL query to be executed.
     * @return ResponseEntity with the query result.
     */
    @PostMapping("/executeQuery")
    public ResponseEntity<List<Map<String, Object>>> executeQuery(@RequestBody String query) {
        return databaseService.executeQuery(query);
    }
}
