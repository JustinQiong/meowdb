package meowdb.entity.config;

/**
 * 服务器配置
 *
 * @author justin.qiong@outlook.com
 * @since 2021/2/22 16:19
 */
public class Server {

    private String id;
    private String name;
    private String host;
    private Integer port;
    private String user;
    private String password;

    public Server() {

    }

    public Server(String name) {
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

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
