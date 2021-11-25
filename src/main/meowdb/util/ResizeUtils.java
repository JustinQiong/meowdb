package meowdb.util;

import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * @author justin.qiong@outlook.com
 * @since 2021/3/12 17:34
 */
public class ResizeUtils {

    public static void enableVerticalResize(Region node) {
        // 鼠标在边缘时样式变为上下拖动
        node.setOnMouseMoved((MouseEvent event) -> {
            event.consume();
            double y = event.getY();
            double height = node.getHeight();

            if (height - y < 5) {
                node.setCursor(Cursor.V_RESIZE);
            } else {
                node.setCursor(Cursor.DEFAULT);
            }
        });

        // 样式为拖动时改变代码框的高度
        node.setOnMouseDragged((MouseEvent event) -> {
            event.consume();
            if (Cursor.V_RESIZE.toString().equals(node.getCursor().toString())) {
                double y = event.getY();
                node.setPrefHeight(y);
            }
        });
    }

    public static void enableHorizontalResize(Region node) {
        // 鼠标在边缘时样式变为上下拖动
        node.setOnMouseMoved((MouseEvent event) -> {
            event.consume();
            double x = event.getX();
            System.out.println("x=" + x);
            double width = node.getWidth();
            System.out.println("width=" + width);

            if (width - x < 5) {
                node.setCursor(Cursor.H_RESIZE);
            } else {
                node.setCursor(Cursor.DEFAULT);
            }
        });

        // 样式为拖动时改变代码框的高度
        node.setOnMouseDragged((MouseEvent event) -> {
            event.consume();
            if (Cursor.H_RESIZE.toString().equals(node.getCursor().toString())) {
                double x = event.getX();
                node.setPrefWidth(x);
            }
        });
    }
}
