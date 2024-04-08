package fordevs.dynamicqueryengine.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DataSourceContextService {

    private static final ThreadLocal<JdbcTemplate> currentTemplate = new ThreadLocal<>();

    public static void setCurrentTemplate(JdbcTemplate jdbcTemplate) {
        currentTemplate.set(jdbcTemplate);
    }

    public static JdbcTemplate getCurrentTemplate() {
        return currentTemplate.get();
    }

    public static void clear() {
        currentTemplate.remove();
    }
}