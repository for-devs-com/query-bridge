package fordevs.dynamicqueryengine.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fordevs.dynamicqueryengine.dto.DatabaseCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DynamicDataSourceManager {
    private final Map<String, JdbcTemplate> dataSourceCache = new ConcurrentHashMap<>();

    @Autowired
    private DataSourceContextService dataSourceContextService;



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

    public JdbcTemplate getJdbcTemplateForDb(String key) {
        return dataSourceCache.getOrDefault(key, dataSourceContextService.getCurrentTemplate());
    }

    private DataSource createDataSource(DatabaseCredentials credentials) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.postgresql.Driver"); // TODO: Support other databases and change Driver Dynamically
        hikariConfig.setJdbcUrl("jdbc:postgresql://" + credentials.getHost() + ":" + credentials.getPort() + "/" + credentials.getDatabaseName());
        hikariConfig.setUsername(credentials.getUser());
        hikariConfig.setPassword(credentials.getPassword());
        return new HikariDataSource(hikariConfig);

    }

     public String generateKeyForUserDataSource(DatabaseCredentials credentials) {
        // TODO: Implement a better key generation
        return credentials.getDatabaseManager() + "-" + credentials.getHost() + "-" + credentials.getPort() + "-" + credentials.getDatabaseName() + "-" + credentials.getUser() + "-" + credentials.getPassword();

    }

    public String getKey(DatabaseCredentials credentials) {
        return generateKeyForUserDataSource(credentials);
    }

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
}

