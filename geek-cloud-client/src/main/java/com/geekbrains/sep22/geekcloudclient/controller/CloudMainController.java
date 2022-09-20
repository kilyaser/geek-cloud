package com.geekbrains.sep22.geekcloudclient.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import protocol.DaemonThreadFactory;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import static protocol.Constants.*;
import static protocol.FileUtils.readFileFromStream;
import static protocol.FileUtils.writeFileToStream;

public class CloudMainController implements Initializable{
    public ListView<String> clientView;
    public ListView<String> serverView;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket socket;
    private String currentDir;
    private boolean isReadMessages = true;
    private DaemonThreadFactory factory;

    public void sendToServer(ActionEvent actionEvent) {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        String filePath = currentDir + DELIMITER + fileName;
        writeFileToStream(dos, fileName, filePath);
    }

    private void readMassages() {
        try {
            while (isReadMessages) {
                String command = dis.readUTF();
                System.out.println("Received command: " + command);
                switch (command) {
                    case SEND_FILE_COMMAND -> {
                        readFileFromStream(dis, currentDir);
                        Platform.runLater(() -> fillView(clientView, getFiles(currentDir)));
                    }
                    case UPDATE_VIEW -> updateServerView();
                }
            }
        } catch (IOException e) {
            System.out.printf("Server off %s", e.getMessage());
        }
    }

    public void getFromServer(ActionEvent actionEvent) {
        String serverFileName = serverView.getSelectionModel().getSelectedItem();
            try {
                dos.writeUTF(GET_FILE);
                dos.writeUTF(serverFileName);
                dos.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }

    private void initNetwork()  {
        try {
            socket = new Socket("localhost", 8189);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

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

    private void updateServerView() throws IOException {
        List<String> files = new ArrayList<>();
        int size = dis.readInt();
        for (int i = 0; i < size; i++) {
            String file = dis.readUTF();
            files.add(file);
        }
        Platform.runLater(() -> fillView(serverView, files));
    }
}
