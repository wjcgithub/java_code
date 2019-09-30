package com.niochat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainServer {

    private static int PORT = 7777;

    private static ServerSocket serverSocket;
    public static List<MyChannel> userList = new ArrayList<>();

    public static void start() throws IOException {
        start(PORT);
    }

    public synchronized static void start(int port) throws IOException {
        if (serverSocket != null) return;

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("服务器以及启动， 端口号：" + port);

            while (true) {
                Socket socket = serverSocket.accept();
                MyChannel channel = new MyChannel(socket);
                userList.add(channel);
                new Thread(channel).start();
            }
        } finally {
            if (serverSocket != null) {
                System.out.println("server has close");
                serverSocket.close();
                serverSocket = null;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        start();
    }
}
