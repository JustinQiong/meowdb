package meowdb.entity.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库信息缓存
 *
 * @author justin.qiong@outlook.com
 * @since 2021/2/23 9:24
 */
public class DbCache extends CacheBase {

    private final Map<String, TableCache> tables = new HashMap<>();

    public DbCache(String key) {
        super(key);
    }

    public Map<String, TableCache> getTables() {
        return tables;
    }

    public TableCache getTable(String tableName) {
        return tables.get(tableName);
    }

    public void addTable(TableCache table) {
        tables.put(table.getKey(), table);
    }
}
