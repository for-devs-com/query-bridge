package com.fordevs.querybridge.service;

import com.fordevs.querybridge.dto.DatabaseConnectionRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Interface for database-related operations.
 */
public interface DatabaseService {

    /**
     * Connects to the database using dynamic data sources.
     *
     * @param databaseConnectionRequest The database credentials provided in the request body.
     * @return ResponseEntity with connection status.
     */
    ResponseEntity<String> setDatabaseConnection(DatabaseConnectionRequest databaseConnectionRequest);

    /**
     * Lists the tables in the database.
     *
     * @return ResponseEntity with the list of tables.
     */
    ResponseEntity<List<String>> listTables();

    /**
     * Lists the columns of a table.
     *
     * @param tableName The name of the table.
     * @return ResponseEntity with the list of columns.
     */
    ResponseEntity<List<Map<String, Object>>> listColumns(String tableName);

    /**
     * Gets the data of a table with pagination.
     *
     * @param tableName The name of the table.
     * @param page      The page number.
     * @param size      The number of rows per page.
     * @return ResponseEntity with the table data.
     */
    ResponseEntity<Map<String, Object>> getTableData(String tableName, int page, int size);

    /**
     * Executes a SQL query.
     *
     * @param query The SQL query to be executed.
     * @return ResponseEntity with the query result.
     */
    ResponseEntity<List<Map<String, Object>>> executeQuery(String query);
}
