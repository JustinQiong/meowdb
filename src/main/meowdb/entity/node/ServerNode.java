package meowdb.entity.node;

import meowdb.entity.config.Server;

/**
 * 服务器节点
 *
 * @author justin.qiong@outlook.com
 * @since 2021/2/22 16:58
 */
public class ServerNode extends NodeBase {

    private String id;

    public ServerNode(Server server) {
        super(server.getName());
        this.id = server.getId();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
