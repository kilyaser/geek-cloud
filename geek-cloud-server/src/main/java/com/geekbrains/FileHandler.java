package com.geekbrains;

import java.io.*;
import java.net.Socket;
import static protocol.Constants.*;

public class FileHandler implements Runnable{
    private static final Integer BATCH_SIZE = 256;
    private static final String SERVER_DIR = "server_files";
    private byte[] batch;
    //private final Socket socket;
    private final DataInputStream dis;
    private final DataOutputStream dos;

    public FileHandler(Socket socket) throws IOException {
      //  this.socket = socket;
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        batch = new byte[BATCH_SIZE];
        File file = new File(SERVER_DIR);
        if (!file.exists()) {
            file.mkdir();
        }
        System.out.println("client accepted...");
    }


    @Override
    public void run() {
        try {
            System.out.println("Start listening...");
            while (true) {
                String command = dis.readUTF();
                if (command.equals(SEND_FILE_COMMAND)) {
                    String fileName = dis.readUTF();
                    long size = dis.readLong();
                    try (FileOutputStream fos = new FileOutputStream(SERVER_DIR + DELIMITER + fileName)) {
                        for (int i = 0; i < (size + BATCH_SIZE - 1) / BATCH_SIZE; i++) {
                            int read = dis.read(batch);
                            fos.write(batch, 0, read);
                        }
                    } catch (Exception ignored) {}
                } else {
                    System.out.println("Unknown command received: " + command);
                }
            }
        } catch (Exception ignored) {
            System.out.println("Client disconnected...");
        }

    }
}
