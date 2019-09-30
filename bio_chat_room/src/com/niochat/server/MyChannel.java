package com.niochat.server;

import sun.applet.Main;

import java.io.*;
import java.net.Socket;
import java.util.List;


public class MyChannel implements Runnable {

    private BufferedReader dis;
    private PrintWriter dos;
    private String clientName;
    private boolean isStop = false;

    /**
     * 构造方法初始化一个输入输出通道
     *
     * @param socket
     */
    public MyChannel(Socket socket) {
        try {
            clientName = socket.getRemoteSocketAddress().toString();
            dis = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dos = new PrintWriter(socket.getOutputStream(), true);
            String msg = "系统说：欢迎【" + clientName + "】 来到直播间！";
            dos.println(msg);
        } catch (IOException e) {
            isStop = true;
            CloseUtil.closeAll(dos, dis, socket);
        }
    }

    /**
     * 接收消息
     *
     * @return
     */
    private String reciveMsg() {
        String s = "";
        try {
            s = dis.readLine();
            if (s.equals("Bye")) {
                isStop = true;
                CloseUtil.closeAll(dis, dos);
                MainServer.userList.remove(this);
                sendOther("【" + clientName + "】离开了房间！");
            }
            System.out.println("收到客户端【" + clientName + "】：" + s);
        } catch (IOException e) {
            isStop = true;
            CloseUtil.closeAll(dis, dos);
            MainServer.userList.remove(this);
        }

        return s;
    }

    /**
     * 发送消息
     *
     * @param s
     */
    private void sendMsg(String s) {
        if (s != null && !"".equals(s)) {
            dos.println(s);
        }
    }

    private void receiveMsg() {
        String s = this.reciveMsg();
        sendOther("【" + clientName + "】说: " + s);
    }

    private void sendOther(String s) {
        List<MyChannel> userList = MainServer.userList;
        for (MyChannel channel : userList) {
            if (this == channel)
                continue;

            channel.sendMsg(s);
        }
    }

    @Override
    public void run() {
        //发给其他人的消息
        String msg = "系统说：【" + clientName + "】来到了直播间！";
        System.out.println(msg);
        sendOther(msg);
        while (!isStop)
            receiveMsg();
    }
}
