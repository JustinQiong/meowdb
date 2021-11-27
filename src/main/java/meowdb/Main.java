package meowdb;

import javafx.application.Application;
import javafx.stage.Stage;
import meowdb.boot.Bootstrap;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        new Bootstrap(stage).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
