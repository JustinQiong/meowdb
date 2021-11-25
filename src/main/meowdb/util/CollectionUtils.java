package meowdb.util;

import java.util.Collection;

/**
 * @author justin.qiong@outlook.com
 * @since 2021/2/18 17:30
 */
public class CollectionUtils {

    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }
}
