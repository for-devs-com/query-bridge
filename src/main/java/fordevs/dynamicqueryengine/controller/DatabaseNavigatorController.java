package fordevs.dynamicqueryengine.controller;

import fordevs.dynamicqueryengine.config.DataSourceContextService;
import fordevs.dynamicqueryengine.config.DynamicDataSourceManager;
import fordevs.dynamicqueryengine.dto.DatabaseCredentials;
import fordevs.dynamicqueryengine.dto.DynamicTableData;
import fordevs.dynamicqueryengine.service.SchemaDiscoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class DatabaseNavigatorController {

    @Autowired
    private DynamicDataSourceManager dataSourceManager;

    @Autowired
    private DataSourceContextService dataSourceContextService;

    @Autowired
    private SchemaDiscoveryService schemaDiscoveryService;

    @Autowired
    private Environment environment;

    private DatabaseCredentials credentials;

    /**
     * Connects to the database using dynamic data sources.
     *
     * @param credentials The database credentials provided in the request body.
     * @return ResponseEntity with connection status.
     */
    @PostMapping("/connect-database")
    public ResponseEntity<String> connectToDatabaseDynamically(@RequestBody DatabaseCredentials credentials) {
        // Validate the credentials
        if (!validateCredentials(credentials)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid credentials provided");
        }

        this.credentials = credentials; // Store the credentials for use in other methods

        try {
            // Try to create and test the connection with the provided credentials
            if (!dataSourceManager.createAndTestConnection(credentials)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to connect to database: " + credentials.getDatabaseName());
            }

            // Get the key for the DataSource
            String key = dataSourceManager.getKey(credentials);
            // Get the JdbcTemplate for the database and set it as the current template
            JdbcTemplate jdbcTemplate = dataSourceManager.getJdbcTemplateForDb(key);
            // Set the current JdbcTemplate in the data source context
            dataSourceContextService.setCurrentTemplate(jdbcTemplate);
            return ResponseEntity.ok("Connected successfully to database: " + credentials.getDatabaseName());
        } catch (Exception e) {
            log.error("Error connecting to the database", e);
            return handleException(e, "Error connecting to the database: ");
        }
    }

    /**
     * Lists the tables in the database using dynamic data sources and SchemaDiscoveryService.
     *
     * @return ResponseEntity with the list of tables.
     */
    @GetMapping("/listTables")
    public ResponseEntity<List<String>> listSchema() {
        // Validate the credentials
        ResponseEntity<List<String>> errorResponse = validateCredentials();
        if (errorResponse != null) {
            return errorResponse;
        }

        try {
            // Get the list of tables in the database
            List<String> tables = schemaDiscoveryService.listTables(this.credentials);
            log.info("Tables: {}", tables);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            log.error("Error listing tables", e);
            return handleException(e, "Error listing tables: ");
        }
    }

    /**
     * Lists the columns of a table using dynamic data sources and SchemaDiscoveryService.
     *
     * @param tableName The name of the table.
     * @return ResponseEntity with the list of columns.
     */
    @GetMapping("/columns/{tableName}")
    public ResponseEntity<List<Map<String, Object>>> listColumns(@PathVariable String tableName) {
        // Validate the credentials
        ResponseEntity<List<Map<String, Object>>> errorResponse = validateCredentials();
        if (errorResponse != null) {
            return errorResponse;
        }

        try {
            // Get the key for the DataSource
            String key = dataSourceManager.getKey(this.credentials);
            // Get the list of columns in the table
            List<Map<String, Object>> columns = schemaDiscoveryService.listColumns(tableName, key);
            return ResponseEntity.ok(columns);
        } catch (SQLException e) {
            log.error("SQL error listing columns for table: {}", tableName, e);
            return handleException(e, "SQL error listing columns for table: ");
        } catch (Exception e) {
            log.error("Error listing columns for table: {}", tableName, e);
            return handleException(e, "Error listing columns for table: ");
        }
    }

    /**
     * Gets the data of a table with pagination using dynamic data sources and SchemaDiscoveryService.
     *
     * @param tableName The name of the table.
     * @param page The page number.
     * @param size The number of rows per page.
     * @return ResponseEntity with the table data.
     */
    @GetMapping("/data/{tableName}")
    public ResponseEntity<Map<String, Object>> getTableData(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Validate the credentials
        ResponseEntity<Map<String, Object>> errorResponse = validateCredentials();
        if (errorResponse != null) {
            return errorResponse;
        }

        try {
            // Get the table data with pagination
            ResponseEntity<DynamicTableData> responseEntity = schemaDiscoveryService.getTableDataWithPagination(tableName, this.credentials, page, size);
            DynamicTableData tableData = responseEntity.getBody();

            // Create a response map that includes the data and pagination information
            Map<String, Object> response = new HashMap<>();
            response.put("rows", tableData.getRows());
            response.put("columns", tableData.getColumns());
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("totalRows", tableData.getTotalRows());
            response.put("tableName", tableName);

            log.info("Table Response Data: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error obtaining data from table: {}", tableName, e);
            return handleException(e, "Error obtaining data from table: ");
        }
    }

    /**
     * Executes a SQL query using dynamic data sources.
     *
     * @param query The SQL query to be executed.
     * @return ResponseEntity with the query result.
     */
    @PostMapping("/executeQuery")
    public ResponseEntity<List<Map<String, Object>>> executeQuery(@RequestBody String query) {
        // Validate the credentials
        ResponseEntity<List<Map<String, Object>>> errorResponse = validateCredentials();
        if (errorResponse != null) {
            return errorResponse;
        }

        try {
            // Get the key for the DataSource
            String key = dataSourceManager.getKey(this.credentials);
            // Get the JdbcTemplate for the database
            JdbcTemplate jdbcTemplate = dataSourceManager.getJdbcTemplateForDb(key);
            // Execute the query and get the result
            List<Map<String, Object>> result = jdbcTemplate.queryForList(query);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error executing query", e);
            return handleException(e, "Error executing query: ");
        }
    }

    /**
     * Validates the provided database credentials.
     *
     * @param credentials The database credentials.
     * @return True if the credentials are valid, false otherwise.
     */
    private boolean validateCredentials(DatabaseCredentials credentials) {
        return credentials.getDatabaseName() != null && !credentials.getDatabaseName().isEmpty() &&
                credentials.getHost() != null && !credentials.getHost().isEmpty() &&
                credentials.getUserName() != null && !credentials.getUserName().isEmpty() &&
                credentials.getPassword() != null && !credentials.getPassword().isEmpty();
    }

    /**
     * Validates if credentials are set.
     *
     * @return ResponseEntity with an error message if credentials are not set, null otherwise.
     */
    private <T> ResponseEntity<T> validateCredentials() {
        if (this.credentials == null) {
            log.error("Credentials must be set before calling this method.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return null;
    }

    /**
     * Handles exceptions based on the active environment.
     *
     * @param e The exception that occurred.
     * @param message The message to log.
     * @return ResponseEntity with the appropriate error message.
     */
    private <T> ResponseEntity<T> handleException(Exception e, String message) {
        if ("prod".equals(environment.getProperty("spring.profiles.active"))) {
            return (ResponseEntity<T>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyMap());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((T) (message + e.getMessage()));
        }
    }

    // TODO: Implement additional methods to get information from the database, such as views, stored procedures, etc.
    // TODO: Implement methods to execute queries, insert, update, and delete records, etc.
    // TODO: Implement methods to change the context to connect to another database with a new connection or an existing one in the current context
    // TODO: Implement methods to close the database connection and release resources used
}
