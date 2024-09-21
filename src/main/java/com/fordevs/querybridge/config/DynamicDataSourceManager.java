package com.fordevs.querybridge.config;

import com.fordevs.querybridge.dto.DatabaseCredentials;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class to manage dynamic data sources.
 * It provides methods to create and test connections, cache data sources, and handle JdbcTemplate instances.
 */
@Slf4j
@Service
public class DynamicDataSourceManager {

    // Cache to store JdbcTemplate instances by their key
    private final Map<String, JdbcTemplate> dataSourceCache = new ConcurrentHashMap<>();

    // Database drivers and URLs
    private static final Map<String, String> DRIVER_MAP = new HashMap<>();
    static {
        DRIVER_MAP.put("postgresql", "org.postgresql.Driver");
        DRIVER_MAP.put("mysql", "com.mysql.cj.jdbc.Driver");
        DRIVER_MAP.put("mariadb", "org.mariadb.jdbc.Driver");
        DRIVER_MAP.put("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        DRIVER_MAP.put("oracle", "oracle.jdbc.OracleDriver");
        DRIVER_MAP.put("db2", "com.ibm.db2.jcc.DB2Driver");
        DRIVER_MAP.put("mongodb", "mongodb.jdbc.MongoDriver");
        // Agregar más entradas según sea necesario
    }
    /**
     * Creates and tests a new database connection using the provided credentials.
     *
     * @param credentials the database credentials
     * @return true if the connection is successful, false otherwise
     */
    public boolean createAndTestConnection(DatabaseCredentials credentials) {
        String key = generateKeyForUserDataSource(credentials);
        JdbcTemplate jdbcTemplate = dataSourceCache.computeIfAbsent(key, k -> {
            DataSource dataSource = createDataSource(credentials);
            return new JdbcTemplate(dataSource);
        });

        if (testConnection(jdbcTemplate)) {
            DataSourceContextService.setCurrentTemplate(jdbcTemplate);
            return true;
        } else {
            closeDataSource(key);
            return false;
        }
    }

    /**
     * Tests the connection using the provided JdbcTemplate.
     *
     * @param jdbcTemplate the JdbcTemplate to test
     * @return true if the connection is successful, false otherwise
     */
    private boolean testConnection(JdbcTemplate jdbcTemplate) {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            log.error("Error testing connection", e);
            DataSourceContextService.clear();
            return false;
        }
    }

    /**
     * Gets the JdbcTemplate for the specified database key.
     *
     * @param key the key for the database
     * @return the JdbcTemplate, or the current context template if not found
     */
    public JdbcTemplate getJdbcTemplateForDb(String key) {
        return dataSourceCache.getOrDefault(key, DataSourceContextService.getCurrentTemplate());
    }

    /**
     * Creates a DataSource using the provided credentials.
     *
     * @param credentials the database credentials
     * @return the created DataSource
     */
    private DataSource createDataSource(DatabaseCredentials credentials) {
        HikariConfig hikariConfig = new HikariConfig();

        // Set the driver class name based on the database type
        String driverClassName = DRIVER_MAP.get(credentials.getDatabaseType().toLowerCase());
        if (driverClassName == null) {
            throw new IllegalArgumentException("Unsupported database type: " + credentials.getDatabaseType());
        }
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setJdbcUrl("jdbc:" + credentials.getDatabaseType() + "://" + credentials.getHost() + ":" + credentials.getPort() + "/" + credentials.getDatabaseName());
        hikariConfig.setUsername(credentials.getUserName());
        hikariConfig.setPassword(credentials.getPassword());
        return new HikariDataSource(hikariConfig);
    }

    /**
     * Generates a unique key based on the database credentials.
     *
     * @param credentials the database credentials
     * @return the generated key
     */
    private String generateKeyForUserDataSource(DatabaseCredentials credentials) {
        // TODO: Implement a better key generation
        return credentials.getDatabaseType() + "-" + credentials.getHost() + "-" + credentials.getPort() + "-" + credentials.getDatabaseName() + "-" + credentials.getUserName() + "-" + credentials.getPassword();
    }

    /**
     * Closes the data source for the specified key.
     *
     * @param key the key for the data source to close
     */
    public void closeDataSource(String key) {
        JdbcTemplate jdbcTemplate = dataSourceCache.remove(key);
        DataSourceContextService.clear();
        if (jdbcTemplate != null) {
            DataSource dataSource = jdbcTemplate.getDataSource();
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
            }
        }
    }

    /**
     * Generates a key for the given database credentials.
     *
     * @param credentials the database credentials
     * @return the generated key
     */
    public String getKey(DatabaseCredentials credentials) {
        return generateKeyForUserDataSource(credentials);
    }
}
