package fordevs.dynamicqueryengine.dto;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseCredentials {
    private String databaseManager;
    private String host;
    private Integer port;
    private String databaseName;
    private String user;
    private String password;
}
