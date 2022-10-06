package com.geekbrains.sep22.geekcloudclient.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class SignUpController implements Initializable {
    @FXML
    public TextField tf_username;
    @FXML
    public TextField tf_password;
    @FXML
    public Button button_login;
    @FXML
    public Button button_sign_up;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        button_sign_up.setOnAction(event -> {
            if (!tf_username.getText().trim().isEmpty() && tf_password.getText().trim().isEmpty()) {
                //TODO server connection and entry to DB
            } else {
                System.out.println("Please fill in all information");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please fill in all information");
                alert.show();
            }
        });

        button_login.setOnAction(event -> DBUtils.changScene(event,
                "/com/geekbrains/sep22/geekcloudclient/auth_page_client.fxml",
                "Log in!", null, null));
    }
}
