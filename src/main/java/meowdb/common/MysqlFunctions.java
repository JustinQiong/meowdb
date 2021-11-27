package meowdb.common;

import java.util.ArrayList;
import java.util.List;

/**
 * mysql函数
 *
 * @author justin.qiong@outlook.com
 * @since 2021/3/4 14:56
 */
public enum MysqlFunctions {

    COUNT("COUNT"),
    NOW("NOW"),
    MAX("MAX"),
    MIN("MIN");

    private static final List<String> all = new ArrayList<>();

    static {
        for (MysqlFunctions v : values()) {
            all.add(v.word);
            all.add(v.word.toLowerCase());
        }
    }

    final private String word;

    MysqlFunctions(String word) {
        this.word = word;
    }

    public static List<String> all() {
        return all;
    }

}
