package com.geekbrains.sep22.geekcloudclient.controller;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static protocol.Constants.*;

public class DisThread extends Thread {
    private CloudMainController controller;

    public DisThread(CloudMainController controller) {
        this.controller = controller;
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
                    default -> System.out.println("Unknown command received: " + command);
                }
            }
        } catch (IOException e) {

        }
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
