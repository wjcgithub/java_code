package com.niochat.server;


import java.io.Closeable;
import java.io.IOException;

public class CloseUtil {
    public static void closeAll(Closeable... closeables){
        for (Closeable clo: closeables) {
            if (clo!=null) {
                try {
                    clo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}