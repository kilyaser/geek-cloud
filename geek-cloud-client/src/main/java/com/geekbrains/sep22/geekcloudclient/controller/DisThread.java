package com.geekbrains.sep22.geekcloudclient.controller;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static protocol.Constants.*;

public class DisThread extends Thread {
    private CloudMainController controller;
    private byte[] batch;
    public DisThread(CloudMainController controller) {
        this.controller = controller;
        batch = new byte[BATCH_SIZE];
    }

    @Override
    public void run() {
        while (true) {
            getServerData();
        }
    }

    private void getServerData(){
        try {
            while (true) {
                String command = controller.getDis().readUTF();
                System.out.println("command received: " + command);
                switch (command) {
                    case UPDATE_VIEW -> updateServerView();
                    case SEND_FILE_COMMAND -> copyFileFromServer();
                    default -> System.out.println("Unknown command received: " + command);
                }
            }
        } catch (IOException e) {

        }
    }

    private void copyFileFromServer() throws IOException {
        String fileName = controller.getDis().readUTF();
        long size = controller.getDis().readLong();
        try (FileOutputStream fos = new FileOutputStream(controller.getCurrentDir() + DELIMITER + fileName)) {
            for (int i = 0; i < (size + BATCH_SIZE - 1) / BATCH_SIZE; i++) {
                int read = controller.getDis().read(batch);
                fos.write(batch, 0, read);
            }
        } catch (Exception ignored) {}

        controller.fillView(controller.clientView, controller.getFiles(controller.getCurrentDir()));

    }

    private void updateServerView() throws IOException {
        List<String> fileNames = new ArrayList<>();
        String fileName;
        while (!(fileName = controller.getDis().readUTF()).equals(END)) {
            fileNames.add(fileName);
        }
        System.out.println(fileNames);
        controller.fillView(controller.serverView, fileNames);

    }
}
