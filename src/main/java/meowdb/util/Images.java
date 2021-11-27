package meowdb.util;

import javafx.scene.image.Image;
import meowdb.boot.Bootstrap;

import java.util.Objects;

/**
 * 图片工具类
 *
 * @author justin.qiong@outlook.com
 * @since 2021/2/25 9:52
 */
public class Images {

    private static final ClassLoader classLoader;

    static {
        classLoader = Bootstrap.class.getClassLoader();
    }

    public static Image of(String name) {
        return new Image(Objects.requireNonNull(classLoader.getResourceAsStream(name)));
    }
}
