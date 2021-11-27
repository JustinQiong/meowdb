package meowdb.entity.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * 表信息缓存
 *
 * @author justin.qiong@outlook.com
 * @since 2021/2/23 9:28
 */
public class TableCache extends CacheBase {

    private final Map<String, ColumnCache> columns = new HashMap<>();

    public TableCache(String key) {
        super(key);
    }

    public Map<String, ColumnCache> getColumns() {
        return columns;
    }

    public void addColumn(ColumnCache column) {
        columns.put(column.getKey(), column);
    }

    public ColumnCache getColumn(String columnName) {
        return columns.get(columnName);
    }
}
