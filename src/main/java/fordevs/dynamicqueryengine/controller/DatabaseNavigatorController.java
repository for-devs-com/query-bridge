package fordevs.dynamicqueryengine.controller;

import fordevs.dynamicqueryengine.config.DataSourceContextService;
import fordevs.dynamicqueryengine.config.DynamicDataSourceManager;
import fordevs.dynamicqueryengine.dto.DatabaseCredentials;
import fordevs.dynamicqueryengine.service.SchemaDiscoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class DatabaseNavigatorController {

    @Autowired
    public DynamicDataSourceManager dataSourceManager;

    @Autowired
    private DataSourceContextService dataSourceContextService;

    private DatabaseCredentials credentials;


    public SchemaDiscoveryService schemaDiscoveryService;

    public DatabaseNavigatorController(SchemaDiscoveryService schemaDiscoveryService) {
        this.schemaDiscoveryService = schemaDiscoveryService;
    }


    /**
     * Conecta a la base de datos con DataSources Dinámicos
     */
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
            // Establece el JdbcTemplate actual en el contexto del DataSource
            dataSourceContextService.setCurrentTemplate(jdbcTemplate);
            // Devuelve una respuesta indicando la conexión exitosa
            return ResponseEntity.ok("Conexión exitosa a la base de datos: " + credentials.getDatabaseName());
        } else {
            // Si la conexión falla, devuelve una respuesta indicando el fallo
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al conectar con la base de datos: " + credentials.getDatabaseName());
        }
    }


    // Obtiene las tablas de la base de datos con DataSources Dinámicos y JdbcTemplate directamente (sin el servicio SchemaDiscoveryService)
    // Este método no es recomendable, ya que se debería delegar la lógica de obtener las tablas a un servicio específico
    // para mantener la cohesión y reutilización del código (como se hace en el método listSchema)
   /* @GetMapping("/tables")
    public ResponseEntity<String> listTables() {
        if (this.credentials == null) {
            log.error("Credentials must be set before calling this method.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Credentials are not set.");
        }

        try {

            String key = dataSourceManager.getKey(this.credentials); // Obtiene la clave para el DataSource
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
    }*/

    /**
     * Obtiene las tablas de la base de datos con DataSources Dinámicos y el servicio SchemaDiscoveryService
     * al que se delega la lógica para obtener las tablas manteniendo la cohesión y reutilización del código
     */
    @GetMapping("/listTables")
    public ResponseEntity<String> listSchema() {
        // Verifica que las credenciales estén establecidas
        if (this.credentials == null) {
            log.error("Credentials must be set before calling this method.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Credentials are not set.");
        }
        try {
            // Obtiene las tablas de la base de datos
            List<String> tables = schemaDiscoveryService.listTables(this.credentials);
            log.info("Tables: {}", tables);
            return ResponseEntity.ok(tables.toString());
        } catch (Exception e) {
            log.error("Error listing tables", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtiene las columnas de una tabla
    /*
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
    */

    /**
     * Obtiene las columnas de una tabla con DataSources Dinámicos y el servicio SchemaDiscoveryService
     **/
    @GetMapping("/columns/{tableName}")
    public ResponseEntity<String> listColumns(@PathVariable String tableName) {
        try {

            String key = dataSourceManager.getKey(this.credentials); // Obtiene la clave para el DataSource
            List<Map<String, Object>> columns = schemaDiscoveryService.listColumns(tableName, key); // Obtiene las columnas de la tabla
            return ResponseEntity.ok(columns.toString()); // Devuelve las columnas
        } catch (SQLException e) {
            log.error("Error listing columns for table: {}", tableName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {

            log.error("Credentials must be set before calling this method.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Credentials are not set.");
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
            log.error("Error executing query", e.toString());
            // Se debería manejar la excepción de manera más específica según el caso
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    // TODO: Implementar otros métodos para obtener información de la base de datos, como las vistas, procedimientos almacenados, etc.
    // TODO: Implementar métodos para ejecutar consultas, insertar, actualizar y eliminar registros, etc.
    // TODO: Implementar Metodos para cambiar el contexto para conectarme a otra base de datos con una nueva conexión o una existente en el contexto actual
    // TODO: Implementar métodos para cerrar la conexión con la base de datos y liberar los recursos utilizados
}