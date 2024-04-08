package fordevs.dynamicqueryengine.controller;

import fordevs.dynamicqueryengine.config.DataSourceContextService;
import fordevs.dynamicqueryengine.config.DynamicDataSourceManager;
import fordevs.dynamicqueryengine.dto.DatabaseCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class DatabaseNavigatorController {

    @Autowired
    public DynamicDataSourceManager dataSourceManager;

    @Autowired
    private DataSourceContextService dataSourceContextService;

    private DatabaseCredentials credentials;


    // Conecta a la base de datos con DataSources Dinámicos
    @PostMapping("/connect-database")
    public ResponseEntity<String> connectToDatabaseDynamically(@RequestBody DatabaseCredentials credentials) {
        this.credentials = credentials; // Establece las credenciales para usarlas en otros métodos
        // Intenta crear y probar la conexión con las credenciales proporcionadas
        boolean isConnected = dataSourceManager.createAndTestConnection(credentials);


        // Verifica la conexión y devuelve una respuesta adecuada
        if (isConnected) {
            // Obtiene la clave para el DataSource
            String key = dataSourceManager.getKey(credentials);
            // Obtiene el JdbcTemplate para la base de datos y lo establece como el actual
            JdbcTemplate jdbcTemplate = dataSourceManager.getJdbcTemplateForDb(key);
            dataSourceContextService.setCurrentTemplate(jdbcTemplate);
            return ResponseEntity.ok("Conexión exitosa a la base de datos: " + credentials.getDatabaseName());
        } else {
            // Si la conexión falla, devuelve una respuesta indicando el fallo
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al conectar con la base de datos: " + credentials.getDatabaseName());
        }
    }


    // Obtiene las tablas de la base de datos
    @GetMapping("/tables")
    public ResponseEntity<String> listTables() {
        if (this.credentials == null) {
            log.error("Credentials must be set before calling this method.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Credentials are not set.");
        }

        try {

            String key = dataSourceManager.getKey(this.credentials); // Obtiene la clave para el DataSource
            //JdbcTemplate jdbcTemplate = dataSourceManager.getJdbcTemplateForDb(key); // Se obtiene el JdbcTemplate para la base de datos
            JdbcTemplate jdbcTemplate = dataSourceManager.getJdbcTemplateForDb(key); // Se crea y prueba la conexión con las credenciales proporcionadas
            if (jdbcTemplate == null) {
                log.error("JdbcTemplate is null for key: {}", key);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }else {
                log.info("JdbcTemplate is not null for key: {}", key);
                log.info("JdbcTemplate: {}", jdbcTemplate);
            }

            List<String> tables = jdbcTemplate.execute((Connection con) -> {
                List<String> tableList = new ArrayList<>();
                DatabaseMetaData metaData = con.getMetaData();
                try (ResultSet rs = metaData.getTables(null, "public", "%", new String[]{"TABLE"})) {
                    while (rs.next()) {
                        tableList.add(rs.getString("TABLE_NAME"));
                    }
                }
                return tableList;
            });
            return ResponseEntity.ok(tables.toString());
        } catch (Exception e) {
            log.error("Error listing tables", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // Obtiene las columnas de una tabla
    @GetMapping("/columns/{tableName}")
    public ResponseEntity<List<Map<String, Object>>> listColumns(@PathVariable String tableName) {
        String key = dataSourceManager.getKey(this.credentials); // Obtiene la clave para el DataSource
        JdbcTemplate jdbcTemplate = dataSourceManager.getJdbcTemplateForDb(key); // Se crea y prueba la conexión con las credenciales proporcionadas
        if (jdbcTemplate == null) {
            log.error("JdbcTemplate is null for key: {}", key);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }else {
            log.info("JdbcTemplate is not null for key: {}", key);
            log.info("JdbcTemplate: {}", jdbcTemplate);
        }
        try {
            List<Map<String, Object>> columns = jdbcTemplate.execute((Connection con) -> {
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
            return ResponseEntity.ok(columns);
        } catch (Exception e) {
            // Se debería manejar la excepción de manera más específica según el caso
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Ejecuta una consulta
    @PostMapping("/executeQuery")
    public ResponseEntity<List<Map<String, Object>>> executeQuery(@RequestBody String query) {
        String key = dataSourceManager.getKey(this.credentials); // Obtiene la clave para el DataSource
        JdbcTemplate jdbcTemplate = dataSourceManager.getJdbcTemplateForDb(key); // Se crea y prueba la conexión con las credenciales proporcionadas
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(query);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Se debería manejar la excepción de manera más específica según el caso
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
