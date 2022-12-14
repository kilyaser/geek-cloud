package com.geekbrains.sep22.geekcloudclient.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import protocol.model.SignUpRequest;

import java.io.IOException;

public class SceneUtils {

    public static void changScene(ActionEvent event, String fxmlFile, String title, String username, String password) {
        Parent root = null;
        if(username != null) {
            try {
                FXMLLoader loader = new FXMLLoader(SceneUtils.class.getResource(fxmlFile));
                root = loader.load();
                CloudMainController controller = loader.getController();
                controller.setUserInformation(username);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
             try {
                  root = FXMLLoader.load(SceneUtils.class.getResource(fxmlFile));
             } catch (IOException e) {
                  e.printStackTrace();
             }
        }
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static SignUpRequest signUpUser(ActionEvent event, String username, String password) {
        return new SignUpRequest(username, password);
    }
}
