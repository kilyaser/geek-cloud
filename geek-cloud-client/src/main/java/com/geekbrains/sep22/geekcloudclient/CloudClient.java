package com.geekbrains.sep22.geekcloudclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CloudClient extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/geekbrains/sep22/geekcloudclient/auth_page_client.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Log in!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}