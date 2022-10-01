package com.geekbrains.sep22.geekcloudclient.controller;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import protocol.AssistanceAlertUtils;
import protocol.DaemonThreadFactory;
import protocol.model.*;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static protocol.Constants.*;


public class CloudMainController implements Initializable{
    public ListView<String> clientView;
    public ListView<String> serverView;
    public MenuItem handleRename;
    private Socket socket;
    private String currentDir;
    private Network<ObjectDecoderInputStream, ObjectEncoderOutputStream> network;
    private boolean isReadMessages = true;
    private DaemonThreadFactory factory;

    public void sendToServer(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        network.getOutputStream().writeObject(new FileMessage(Path.of(currentDir).resolve(fileName)));
    }

    private void readMassages() {
        try {
            while (isReadMessages) {
                CloudMessage message = (CloudMessage) network.getInputStream().readObject();
                System.out.println("Received command: " + message.getType());
                switch (message.getType()) {
                    case FILE -> {
                        FileMessage fm = (FileMessage)message;
                        Files.write(Path.of(currentDir).resolve(fm.getFileName()), fm.getBytes());
                        Platform.runLater(() -> fillView(clientView, getFiles(currentDir)));
                    }
                    case LIST -> {
                        ListMessage lm = (ListMessage) message;
                        Platform.runLater(() -> fillView(serverView, lm.getFiles()));
                    }
                }
            }
        } catch (IOException e) {
            System.out.printf("Server off %s", e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void getFromServer(ActionEvent actionEvent) {
        String fileName = serverView.getSelectionModel().getSelectedItem();
            try {
                network.getOutputStream().writeObject(new FileRequest(fileName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }

    private void initNetwork()  {
        try {
            socket = new Socket("localhost", 8189);
            network = new Network<>(
                    new ObjectDecoderInputStream(socket.getInputStream()),
                    new ObjectEncoderOutputStream(socket.getOutputStream())
            );
            factory.getThread(this::readMassages,
                    "cloud-client-read-thread-%")
                    .start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        isReadMessages = true;
        factory = new DaemonThreadFactory();
        initNetwork();
        setCurrentDir(System.getProperty("user.home"));
        fillView(clientView, getFiles(currentDir));
        clientView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
               actionSelected();
            }
        });
        serverView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                serverActionSelected();
            }
        });
    }


    public void handleDeleteFileOption(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        Alert infoAlert = AssistanceAlertUtils.getInformationAlert(fileName, true);
        Alert alert = AssistanceAlertUtils.getWarningConfirm(fileName);
        Optional<ButtonType> answer = alert.showAndWait();
        if (answer.get() == ButtonType.OK) {
            Path path = Path.of(currentDir + DELIMITER + fileName);
            if (Files.exists(path)) {
                Files.delete(path);
                Platform.runLater(() -> fillView(clientView, getFiles(currentDir)));
                infoAlert.showAndWait();
            }
        } else {
            infoAlert = AssistanceAlertUtils.getInformationAlert(fileName, false);
            infoAlert.showAndWait();
        }

    }
    public void handleServerFileDeleteOption(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        network.getOutputStream().writeObject(new DeleteRequest(fileName));
    }

      private void setCurrentDir(String dir) {
        currentDir = dir;
        fillView(clientView, getFiles(currentDir));
    }

    public void fillView(ListView<String> view, List<String> data) {
        view.getItems().clear();
        view.getItems().addAll(data);
    }

    public List<String> getFiles(String directory) {
        File dir = new File(directory);
        if (dir.isDirectory()) {
            String[] listFiles = dir.list();
            if (listFiles != null) {
                List<String> files = new ArrayList<>(Arrays.asList(listFiles));
                files.add(0, "..");
                return files;
            }
        }
        return List.of("..");
    }

    private void actionSelected() {
        String selected = clientView.getSelectionModel().getSelectedItem();
        File selectedFile = new File(currentDir + DELIMITER + selected);
            if (selectedFile.isDirectory()) {
                setCurrentDir(selectedFile.getAbsolutePath());
            }
    }
    private void serverActionSelected() {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        try {
            network.getOutputStream().writeObject(new ViewRequest(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void handleServerFileRenameOption(ActionEvent actionEvent) {
    }

    public void handleFileRenameOption(ActionEvent actionEvent) {
    }
}
