package fordevs.dynamicqueryengine.service;

import fordevs.dynamicqueryengine.config.DynamicDataSourceManager;
import fordevs.dynamicqueryengine.dto.DatabaseCredentials;
import fordevs.dynamicqueryengine.dto.DynamicTableData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SchemaDiscoveryService {

    private final DynamicDataSourceManager dataSourceManager;

    public SchemaDiscoveryService(DynamicDataSourceManager dataSourceManager) {
        this.dataSourceManager = dataSourceManager;
    }

    /**
     * Obtiene la lista de tablas de la base de datos  y la devuelve como una lista de cadenas de texto
     * con el nombre de las tablas encontradas en la base de datos
     */
    public List<String> listTables(DatabaseCredentials credentials) throws DataAccessException, SQLException {
        String key = dataSourceManager.getKey(credentials);
        JdbcTemplate jdbcTemplate = dataSourceManager.getJdbcTemplateForDb(key);

        if (jdbcTemplate == null) {
            throw new SQLException("Unable to obtain JdbcTemplate for given credentials.");
        }

        return jdbcTemplate.execute((Connection con) -> {
            List<String> tableList = new ArrayList<>();
            DatabaseMetaData metaData = con.getMetaData();
            try (ResultSet rs = metaData.getTables(null, "public", "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tableList.add(rs.getString("TABLE_NAME"));
                }
            }
            return tableList;
        });
    }

    /**
     * Obtiene la lista de columnas de una tabla en la base de datos y la devuelve como una lista de mapas
     * con el nombre de la columna, el tipo de dato y el tamaño de la columna
     */
    public List<Map<String, Object>> listColumns(String tableName, String credentialsKey) throws SQLException {
        JdbcTemplate jdbcTemplate = dataSourceManager.getJdbcTemplateForDb(credentialsKey);

        if (jdbcTemplate == null) {
            throw new SQLException("Unable to obtain JdbcTemplate for key: " + credentialsKey);
        }

        return jdbcTemplate.execute((Connection con) -> {
            List<Map<String, Object>> columnList = new ArrayList<>();
            DatabaseMetaData metaData = con.getMetaData();
            try (ResultSet rs = metaData.getColumns(null, null, tableName, "%")) {
                while (rs.next()) {
                    Map<String, Object> column = new HashMap<>();
                    column.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
                    column.put("TYPE_NAME", rs.getString("TYPE_NAME"));
                    column.put("COLUMN_SIZE", rs.getInt("COLUMN_SIZE"));
                    columnList.add(column);
                }
            }
            return columnList;
        });
    }

    /**
     * Obtiene los datos de una tabla con paginación y el conteo total de filas.
     */
    public ResponseEntity<DynamicTableData> getTableDataWithPagination(String tableName, DatabaseCredentials credentials, int page, int size) throws SQLException {
        try {

            String key = dataSourceManager.getKey(credentials);
            JdbcTemplate jdbcTemplate = dataSourceManager.getJdbcTemplateForDb(key);

            if (jdbcTemplate == null) {
                throw new SQLException("Unable to obtain JdbcTemplate for key: " + key);
            }

            // Obtiene los datos de las filas.
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT * FROM " + tableName + " LIMIT ? OFFSET ?", size, page * size
            );

            // Obtiene los metadatos de las columnas.
            List<Map<String, Object>> columns = listColumns(tableName, key);

            int totalRows = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);


            DynamicTableData response = new DynamicTableData();
            response.setRows(rows);
            response.setColumns(columns);
            response.setTotalRows(totalRows);
            response.setTableName(tableName);
            response.setPageSize(size);
            response.setCurrentPage(page);


            // Return the populated DynamicTableData object
            return ResponseEntity.ok(response);
        } catch (Exception e) {

            log.error("Error obtaining data from table: " + tableName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }
}
