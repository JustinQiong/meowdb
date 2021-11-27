package meowdb.entity.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据库查询结果
 *
 * @author justin.qiong@outlook.com
 * @since 2021/2/23 11:59
 */
public class DataTable {

    List<String> columns = new ArrayList<>();
    List<Map<String, String>> rows = new ArrayList<>();

    public List<String> getColumns() {
        return columns;
    }

    public void addColumn(String column) {
        columns.add(column);
    }

    public List<Map<String, String>> getRows() {
        return rows;
    }

    public void addRow(Map<String, String> row) {
        rows.add(row);
    }
}
