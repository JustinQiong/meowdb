package meowdb.entity.cache;

/**
 * 缓存基础类
 *
 * @author justin.qiong@outlook.com
 * @since 2021/2/23 9:31
 */
public class CacheBase {

    private String key;

    public CacheBase(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
