package meowdb.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询关键字
 *
 * @author justin.qiong@outlook.com
 * @since 2021/2/18 18:00
 */
public enum Keywords {

    INSERT("INSERT"),
    DELETE("DELETE"),
    UPDATE("UPDATE"),
    SELECT("SELECT"),
    ALTER("ALTER"),
    CREATE("CREATE"),
    DROP("DROP"),
    ADD("ADD"),
    INDEX("INDEX"),
    KEY("KEY"),
    PRIMARY("PRIMARY"),
    MODIFY("MODIFY"),
    COLUMN("COLUMN"),
    COLUMNS("COLUMNS"),
    TABLE("TABLE"),
    TABLES("TABLES"),
    DATABASE("DATABASE"),
    DATABASES("DATABASES"),
    FROM("FROM"),
    WHERE("WHERE"),
    AND("AND"),
    OR("OR"),
    IS("IS"),
    NOT("NOT"),
    LIKE("LIKE"),
    NULL("NULL"),
    BETWEEN("BETWEEN"),
    HAVING("HAVING"),
    GROUP_BY("GROUP BY"),
    ORDER_BY("ORDER BY"),
    ASC("ASC"),
    DESC("DESC"),
    LIMIT("LIMIT"),
    SKIP("SKIP"),
    SHOW("SHOW"),
    SET("SET"),
    TRUNCATE("TRUNCATE"),
    AS ("AS"),
    IN("IN"),
    INTO("INTO");

    private final String word;
    private static final List<String> all = new ArrayList<>();

    static {
         for (Keywords v : values()) {
             all.add(v.word);
             all.add(v.word.toLowerCase());
        }
    }

    Keywords(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public static List<String> all() {
        return all;
    }
}
