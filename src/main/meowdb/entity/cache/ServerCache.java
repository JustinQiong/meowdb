package meowdb.entity.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务器信息缓存
 *
 * @author justin.qiong@outlook.com
 * @since 2021/2/23 9:40
 */
public class ServerCache extends CacheBase {

    private final Map<String, DbCache> databases = new HashMap<>();

    public ServerCache(String key) {
        super(key);
    }

    public Map<String, DbCache> getDatabases() {
        return databases;
    }

    public void addDatabase(DbCache db) {
        databases.put(db.getKey(), db);
    }

    public DbCache getDatabase(String dbName) {
        return databases.get(dbName);
    }
}
