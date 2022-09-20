package com.geekbrains;

import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static protocol.Constants.*;

public class FileHandler implements Runnable {
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss: ");
    private static final String SERVER_DIR = "server_files";
    private byte[] batch;
    private final Socket socket;
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private List<String> serverView;

    public FileHandler(Socket socket) throws IOException {
        this.socket = socket;
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        serverView = new ArrayList<>();
        batch = new byte[BATCH_SIZE];
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
                    case SEND_FILE_COMMAND -> copyToServer();
                    case GET_FILE -> sendToClient();
                    default -> System.out.println("Unknown command received: " + command);
                }
            }
        } catch (Exception ignored) {
            System.out.println(DATE_FORMAT.format(System.currentTimeMillis()) + "Client disconnected...");
        }

    }

    private void sendToClient() {
        System.out.println("запрос на копирование от клиента");
        try {
            String fileName = dis.readUTF();
            String filePath = SERVER_DIR + DELIMITER + fileName;
            File file = new File(filePath);
            if (file.isFile()) {
                dos.writeUTF(SEND_FILE_COMMAND);
                dos.writeUTF(fileName);
                dos.writeLong(file.length());
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] bytes = fis.readAllBytes();
                    dos.write(bytes);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendServerFiles() throws IOException {
        File dir = new File(SERVER_DIR);
        String[] files = dir.list();
        assert files != null;
        dos.writeUTF(UPDATE_VIEW);
        dos.writeInt(files.length);
        for (String file : files) {
            dos.writeUTF(file);
        }
        System.out.println(files.length + " files sent to client");
    }

    private void copyToServer() throws IOException {
        String fileName = dis.readUTF();
        long size = dis.readLong();
        try (FileOutputStream fos = new FileOutputStream(SERVER_DIR + DELIMITER + fileName)) {
            for (int i = 0; i < (size + BATCH_SIZE - 1) / BATCH_SIZE; i++) {
                int read = dis.read(batch);
                fos.write(batch, 0, read);
            }
        } catch (Exception ignored) {}
        sendServerFiles();
    }
}
