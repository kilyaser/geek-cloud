package com.geekbrains.sep22.geekcloudclient.controller;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import static protocol.Constants.*;

public class CloudMainController implements Initializable {
     public ListView<String> clientView;
    public ListView<String> serverView;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket socket;
    private String currentDir;

    public void sendToServer(ActionEvent actionEvent) {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        String filePath = currentDir + DELIMITER + fileName;
        File file = new File(filePath);
        if (file.isFile()) {
            try {
                dos.writeUTF(SEND_FILE_COMMAND);
                dos.writeUTF(fileName);
                dos.writeLong(file.length());

                try (FileInputStream fis = new FileInputStream(file);
                     BufferedInputStream bis = new BufferedInputStream(fis)) {
                    int i;
                    while ((i = bis.read()) != -1) {
                        dos.write(i);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                System.err.println("e = " + e.getMessage());
            }

        }
    }

    private void initNetwork()  {
        try {
            socket = new Socket("localhost", 8189);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (Exception ignored) {}
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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

    private void fillView(ListView<String> view, List<String> data) {
        view.getItems().clear();
        view.getItems().addAll(data);
    }

    private List<String> getFiles(String directory) {
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

}
