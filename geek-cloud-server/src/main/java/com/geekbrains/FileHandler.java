package com.geekbrains;

import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static protocol.Constants.*;
import static protocol.FileUtils.readFileFromStream;
import static protocol.FileUtils.writeFileToStream;

public class FileHandler implements Runnable {
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss: ");
    private static final String SERVER_DIR = "server_files";
    private final Socket socket;
    private final DataInputStream dis;
    private final DataOutputStream dos;

    public FileHandler(Socket socket) throws IOException {
        this.socket = socket;
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        File file = new File(SERVER_DIR);
        if (!file.exists()) {
            file.mkdir();
        }
        sendServerFiles();
        System.out.println(DATE_FORMAT.format(System.currentTimeMillis()) + "client accepted...");
    }

    @Override
    public void run() {
        try {
            System.out.println(DATE_FORMAT.format(System.currentTimeMillis()) + "Start listening...");
            while (true) {
                String command = dis.readUTF();
                System.out.println("Received command: " + command);
                switch (command) {
                    case SEND_FILE_COMMAND -> {
                        readFileFromStream(dis, SERVER_DIR);
                        sendServerFiles();
                    }
                    case GET_FILE -> writeFileToStream(dis, dos, SERVER_DIR);
                    default -> System.out.println("Unknown command received: " + command);
                }
            }
        } catch (Exception ignored) {
            System.out.println(DATE_FORMAT.format(System.currentTimeMillis()) + "Client disconnected...");
        }
    }

    private void sendServerFiles() throws IOException {
        File dir = new File(SERVER_DIR);
        if (dir.isDirectory()) {
            String[] files = dir.list();
            if (files != null) {
                List<String> listFiles = new ArrayList<>(Arrays.asList(files));
                dos.writeUTF(UPDATE_VIEW);
                listFiles.add(0, "..");
                dos.writeInt(listFiles.size());
                for (String file : listFiles) {
                    dos.writeUTF(file);
                }
            }
        }

    }
}
