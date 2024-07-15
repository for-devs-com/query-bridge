package com.fordevs.dynamicqueryengine.config;

import com.fordevs.dynamicqueryengine.dto.DatabaseCredentials;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
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

//    @Autowired
//    private DataSourceContextService dataSourceContextService;

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
        hikariConfig.setDriverClassName("org.postgresql.Driver"); // TODO: Support other databases and change Driver Dynamically
        hikariConfig.setJdbcUrl("jdbc:postgresql://" + credentials.getHost() + ":" + credentials.getPort() + "/" + credentials.getDatabaseName());
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
        return credentials.getDatabaseManager() + "-" + credentials.getHost() + "-" + credentials.getPort() + "-" + credentials.getDatabaseName() + "-" + credentials.getUserName() + "-" + credentials.getPassword();
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
