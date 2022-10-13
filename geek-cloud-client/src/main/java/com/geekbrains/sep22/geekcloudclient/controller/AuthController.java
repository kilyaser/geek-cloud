package com.geekbrains.sep22.geekcloudclient.controller;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import protocol.DaemonThreadFactory;
import protocol.model.*;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {
    @FXML
    public TextField tf_username;
    @FXML
    public TextField tf_password;
    @FXML
    public Button button_login;
    @FXML
    public Button button_sign_up;
    private Network<ObjectDecoderInputStream, ObjectEncoderOutputStream> network;
    private Socket socket;
    private DaemonThreadFactory factory;
    private boolean isReadMessages = true;
    ActionEvent actionEvent;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        isReadMessages = true;
        factory = new DaemonThreadFactory();
        initNetwork();
        button_login.setOnAction(this::sendDataForAuthorization);
        button_sign_up.setOnAction(event -> SceneUtils.changScene(event,
                "/com/geekbrains/sep22/geekcloudclient/sign-up.fxml",
                "Sign up!",
                null, null));

    }

    private void initNetwork()  {
        try {
            socket = new Socket("localhost", 8189);
            network = new Network<>(
                    new ObjectDecoderInputStream(socket.getInputStream()),
                    new ObjectEncoderOutputStream(socket.getOutputStream())
            );
            factory.getThread(this::readMassages,
                            Thread.currentThread().getName())
                    .start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void readMassages() {
        try {
            while (isReadMessages) {
                CloudMessage message = (CloudMessage) network.getInputStream().readObject();
                System.out.println("Received command: " + message.getType());
                switch (message.getType()) {
                    case AUTH -> {
                        AuthRequest authRequest = (AuthRequest) message;
                        Platform.runLater(()-> getServerAnswer(authRequest));
                    }
                }
            }
        } catch (IOException e) {
            System.out.printf("Server off %s", e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void getServerAnswer(AuthRequest authRequest) {

        if (authRequest.isCheckUser()) {
            SceneUtils.changScene(actionEvent,
                    "/com/geekbrains/sep22/geekcloudclient/geek-cloud-client.fxml",
                    "Cloud Client!", authRequest.getUsername(), authRequest.getPassword());
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("wrong username or password");
            alert.show();
        }
    }

    private void sendDataForAuthorization(ActionEvent event) {
        actionEvent = event;
        try {
            network.getOutputStream().writeObject(new AuthRequest(tf_username.getText(), tf_password.getText()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
