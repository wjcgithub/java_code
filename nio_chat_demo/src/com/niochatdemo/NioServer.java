package com.niochatdemo;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class NioServer implements Runnable {
    private Selector selector;
    private ByteBuffer writeBuf = ByteBuffer.allocate(1024);
    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    public static List<SocketChannel> userList = new ArrayList<>();

    public NioServer(int port) {
        try {
            //1 创建一个传送带
            selector = Selector.open();
            //2 创建一个管道
            ServerSocketChannel ssc = ServerSocketChannel.open();
            //3 设置服务器通道为非阻塞方式
            ssc.configureBlocking(false);
            //4 绑定TCP地址
            ssc.bind(new InetSocketAddress(port));
            //5 把管道放到传送带上，并在传送带上注册一个感兴趣事件，此处传送带感兴趣事件为连接事件
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server start, port：" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void run() {
        while (true) {
            try {
                //1 启动传送带，开始轮询
                selector.select();
                //2 所有感兴趣事件的keys
                java.util.Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                //3 遍历所有感兴趣事件集合
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (key.isValid()) { //如果key的状态是有效的
                        if (key.isAcceptable()) { //如果key是阻塞状态，则调用accept()方法
                            accept(key);
                        }

                        if (key.isReadable()) { //如果key是可读状态，则调用read()方法
                            read(key);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * jijiu
     * @param key
     */
    private void accept(SelectionKey key) {
        try {
            //1 获取服务器通道
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            //2 执行阻塞方法
            SocketChannel sc = ssc.accept();
            //3 设置阻塞模式为非阻塞
            sc.configureBlocking(false);
            //4 注册到多路复用选择器上，并设置读取标识
            sc.register(selector, SelectionKey.OP_READ);
            NioServer.userList.add(sc);
            String clientName = sc.getRemoteAddress().toString();
            String msg = "系统说：【" + clientName + "】来到了直播间！\r\n";
            ByteBuffer out = ByteBuffer.allocate(1024);
            sendOther(sc, msg);
            out.put(msg.getBytes());
            out.flip();
            sc.write(out);
            out.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * read info
     * @param key
     */
    private void read(SelectionKey key) {
        try {
            //1 清空缓冲区中的旧数据
            buffer.clear();
            //2 获取之前注册的SocketChannel通道
            SocketChannel sc = (SocketChannel) key.channel();
            //3 将sc中的数据放入buffer中
            int count = sc.read(buffer);
            if (count == -1) { // == -1表示通道中没有数据
                key.channel().close();
                key.cancel();
                userList.remove(sc);
                return;
            }

            //读取到了数据，将buffer的position复位到0
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            //将buffer中的数据写入byte[]中
            buffer.get(bytes);
            String body = new String(bytes).trim();
            if (body.equals("Bye")) {
                String clientName = sc.getRemoteAddress().toString();
                String quitString = clientName+" say: Bye\r\n";
                buffer.clear();
                sendOther(sc, quitString);
                key.channel().close();
                key.cancel();
                userList.remove(sc);
                return;
            }
            writeBuf.flip();
            sc.write(writeBuf);
            writeBuf.clear();
            this.sendOther(sc, body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send msg to all
     * @param sc
     * @param body
     * @throws IOException
     */
    private void sendOther(SocketChannel sc, String body) throws IOException {
        List<SocketChannel> userList = NioServer.userList;
        String clientName = sc.getRemoteAddress().toString();
        ByteBuffer sendOtherBuf = ByteBuffer.allocate(1024);
        String sendData = "接收到 "+clientName+" 的请求：" + body + "\r\n";
        for (SocketChannel channel : userList) {
            if (sc == channel)
                continue;

            sendOtherBuf.put(sendData.getBytes());
            sendOtherBuf.flip();
            channel.write(sendOtherBuf);
            sendOtherBuf.clear();
        }
    }

    public static void main(String[] args) {
        new Thread(new NioServer(8379)).start();
    }
}
