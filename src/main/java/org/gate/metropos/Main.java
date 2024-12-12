package org.gate.metropos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.gate.metropos.config.DatabaseConfig;
import org.gate.metropos.scheduler.SyncScheduler;

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
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("SplashScreen.fxml"));
        //for no toolbar
        stage.initStyle(StageStyle.UNDECORATED);

        Scene scene = new Scene(fxmlLoader.load(), 700, 500);
        stage.setTitle("Metro POS");
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