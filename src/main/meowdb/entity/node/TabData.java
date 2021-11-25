package meowdb.entity.node;

/**
 * Tab信息
 *
 * @author justin.qiong@outlook.com
 * @since 2021/2/25 11:29
 */
public class TabData {
    private String serverId;
    private String serverName;
    private String dbName;
    private String sessionId;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
