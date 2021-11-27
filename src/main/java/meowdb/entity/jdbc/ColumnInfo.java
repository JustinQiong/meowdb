package meowdb.entity.jdbc;

/**
 * 列信息
 *
 * @author justin.qiong@outlook.com
 * @since 2021/3/1 15:57
 */
public class ColumnInfo {
    private String field;
    private String type;
    private String Null;
    private String key;
    private String def;
    private String extra;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNull() {
        return Null;
    }

    public void setNull(String isNull) {
        this.Null = isNull;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
