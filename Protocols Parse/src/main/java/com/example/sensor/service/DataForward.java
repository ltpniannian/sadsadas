package com.example.sensor.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataForward {
    private ServerSocket listenSocket;
    @Autowired
    public DataForward() throws IOException {
        listenSocket = new ServerSocket(5569);
        while(true){
            Socket socket = listenSocket.accept();
            new Thread(new NewPocessThread(socket)).start();
        }
    }
}

class ForwardData implements Runnable{
    private Socket socket;
    ForwardData(Socket socket){
        this.socket = socket;
    }
    private static final Logger logger = LogManager.getLogger(NewPocessThread.class.getName());
    @Override
    public void run() {
        try {
            runNewThread();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void runNewThread() throws IOException, InterruptedException {
        OutputStream outputStream =this.socket.getOutputStream();
        
    }
    private int byteConvertToInt(byte[] b){
        System.out.println(Arrays.toString(b));
        int value = 0;
        int length = b.length;
        for(int i=0;i<length;i++){
            int shift = (length-1-i)*8;
            value += (b[i]&0xFF)<<shift;
        }
        return value;
    }
    private void getTestData(JSONObject j){
        try {
            RandomAccessFile randomFile = new RandomAccessFile("test.txt", "rw");
            long fileLength = randomFile.length();
            randomFile.seek(fileLength);
            randomFile.writeBytes(j.toString()+"\r\n");
            randomFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}