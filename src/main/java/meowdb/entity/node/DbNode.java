package meowdb.entity.node;

/**
 * 树状图的数据库节点
 *
 * @author justin.qiong@outlook.com
 * @since 2021/2/20 9:21
 */
public class DbNode extends NodeBase {

    private ServerNode server;

    public DbNode(String name) {
        super(name);
    }

    public ServerNode getServer() {
        return server;
    }

    public void setServer(ServerNode server) {
        this.server = server;
    }
}
