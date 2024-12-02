package org.example.metropos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.metropos.config.DatabaseConfig;
import org.example.metropos.scheduler.SyncScheduler;

import java.io.IOException;

public class Main extends Application {
    SyncScheduler scheduler;

    @Override
    public void init() {
        scheduler = new SyncScheduler();

        scheduler.startScheduler();

    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() throws Exception {
        scheduler.shutdownScheduler();
        DatabaseConfig.shutdown();
        super.stop();
    }
}