package fordevs.dynamicqueryengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicTableData {
    private String tableName;
    private List<Map<String, Object>> rows;
    private int totalRows;
    private List<String> columns;
    private String credentialsKeys;
}
