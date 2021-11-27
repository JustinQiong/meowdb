package meowdb.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import meowdb.entity.config.ServerConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 配置解析器
 *
 * @author justin.qiong@outlook.com
 * @since 2021/2/22 16:47
 */
public class Configs {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String SERVER_CONFIG_PATH = "server.json";

    public static <T> T parse(Class<T> type, String path) throws IOException {
        File file = new File(path);
        return mapper.readValue(file, type);
    }

    public static <T> void save(T config, String path) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(writer, config);
        }
    }

    public static ServerConfig loadServerConfig() throws IOException {
        return parse(ServerConfig.class, SERVER_CONFIG_PATH);
    }

    public static void saveServerConfig(ServerConfig config) throws IOException {
        save(config, SERVER_CONFIG_PATH);
    }

    public static String nextServerId() {
        return String.valueOf(System.currentTimeMillis());
    }
}
