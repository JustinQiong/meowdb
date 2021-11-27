package meowdb.entity.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author justin.qiong@outlook.com
 * @since 2021/2/22 16:34
 */
public class ServerConfig {

    private Map<String, Server> servers = new LinkedHashMap<>();

    public ServerConfig() {

    }

    public ServerConfig(Map<String, Server> servers) {
        this.servers = servers;
    }

    public Map<String, Server> getServers() {
        return servers;
    }

    public void setServers(Map<String, Server> servers) {
        this.servers = servers;
    }

    public void addServer(Server server) {
        servers.put(server.getId(), server);
    }

    public void removeServer(Server server) {
        servers.remove(server.getId());
    }

    public boolean existsServer(Server server) {
        return servers.containsKey(server.getId());
    }
}
