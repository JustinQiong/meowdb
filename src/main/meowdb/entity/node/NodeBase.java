package meowdb.entity.node;

/**
 * 节点基础类
 *
 * @author justin.qiong@outlook.com
 * @since 2021/2/20 9:29
 */
public class NodeBase {

    private String id;
    private String name;
    private boolean loaded;

    public NodeBase() {

    }

    public NodeBase(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public String toString() {
        return name;
    }
}
