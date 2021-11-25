package meowdb.entity.jdbc;

/**
 * 主键
 *
 * @author justin.qiong@outlook.com
 * @since 2021/3/1 15:52
 */
public class PrimaryKey {
    private String name;
    private String value;

    public PrimaryKey() {

    }

    public PrimaryKey(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
