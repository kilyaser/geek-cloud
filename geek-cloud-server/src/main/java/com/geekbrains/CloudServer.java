package com.geekbrains;

import protocol.DaemonThreadFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class CloudServer {

    public static void main(String[] args) {
        DaemonThreadFactory serviceThreadFactory = new DaemonThreadFactory();

        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            while (true) {
                System.out.println("Server started!");
                Socket socket = serverSocket.accept();
                serviceThreadFactory.getThread(
                        new FileHandler(socket),
                        "file-handler-thread-%")
                        .start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
