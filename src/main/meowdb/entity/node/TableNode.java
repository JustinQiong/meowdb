package meowdb.entity.node;

import java.util.List;

/**
 * 树状图的表节点
 *
 * @author justin.qiong@outlook.com
 * @since 2021/2/20 9:22
 */
public class TableNode extends NodeBase {

    private List<String> pks;

    private DbNode database;

    public TableNode(String name) {
        super(name);
    }

    public DbNode getDatabase() {
        return database;
    }

    public void setDatabase(DbNode database) {
        this.database = database;
    }

    public List<String> getPks() {
        return pks;
    }

    public void setPks(List<String> pks) {
        this.pks = pks;
    }
}
