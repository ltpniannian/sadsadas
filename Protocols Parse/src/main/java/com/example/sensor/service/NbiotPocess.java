package com.example.sensor.service;

import com.example.sensor.SensorApplication;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.logging.log4j.LogManager;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;


@Component
public class NbiotPocess {
    private int SensorID;
    private static final Logger logger = LogManager.getLogger(NbiotPocess.class.getName());

    @Autowired
    public NbiotPocess() throws IOException {
        ServerSocket server = new ServerSocket(5568);
        while(true){
         Socket socket = server.accept();
         new Thread(new NewPocessThread(socket)).start();
        }
    }

}

class NewPocessThread implements Runnable{
    private Socket socket;
    NewPocessThread(Socket socket){
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
        byte[] readIn = new byte[38];
        InputStream in = socket.getInputStream();
        boolean result = true;
        while(in.read(readIn)!=-1&&result){
            logger.info(socket.getPort()+" "+Arrays.toString(readIn));
//            System.out.println(socket.getPort()+" "+Arrays.toString(readIn));
            result = dataAnalysis(readIn);
        }
        logger.info("Socket("+socket.getPort()+") is closed.");
//        System.out.println("Socket("+socket.getPort()+") is closed.");
        socket.close();

    }
    private boolean dataAnalysis(byte[] readIn){
        int sensorType =readIn[0];
        Map<String,String> result = new HashMap<>();
        boolean re = true;
        //21到26是声音传感器，13,14位是声音的值，前为是整数，后为小数
        if(sensorType>=21&&sensorType<=26){
//            temp = readIn[14]+readIn[15];
            float voice = (float) byteConvertToInt(new byte[]{readIn[13],readIn[14]})/100;
            result.put("voice",String.valueOf(voice));
            System.out.println("Voice(" +sensorType + ") is "+voice+"dB");
            re =  true;
        }
        //27 28 29是光线传感器，
        if(sensorType>=27&&sensorType<=29){
            int light = byteConvertToInt(new byte[]{readIn[11],readIn[12]});
            System.out.println("Light(" +sensorType + ")is "+light+"Lux");
            result.put("light",String.valueOf(light));
            re = true;
        }
        //30 31是7合1
        if(sensorType==30||sensorType==31){
            float temperature = byteConvertToInt(new byte[]{readIn[3],readIn[4]})/10;
            System.out.println("Temperature(" +sensorType + ")is "+temperature+"C");
            result.put("temperature",String.valueOf(temperature));
            float wet = byteConvertToInt(new byte[]{readIn[5],readIn[6]})/10;
            System.out.println("Wet-percent(" +sensorType + ")is "+wet+"%RH");
            result.put("wet",String.valueOf(wet)+"%");
            int PM2P5 = byteConvertToInt(new byte[]{readIn[7],readIn[8]});
            System.out.println("PM2P5(" +sensorType + ")is "+PM2P5+"ug/m3");
            result.put("PM2P5",String.valueOf(PM2P5));
            int PM10 = byteConvertToInt(new byte[]{readIn[9],readIn[10]});
            System.out.println("PM10(" +sensorType + ")is "+PM10+"ug/m3");
            result.put("PM10",String.valueOf(PM10));
            float NO2 = ((float)byteConvertToInt(new byte[]{readIn[11],readIn[12]}))/100;
            System.out.println("NO2(" +sensorType + ")is "+NO2+"PPM");
            result.put("NO2",String.valueOf(NO2));
            float SO2 = ((float)byteConvertToInt(new byte[]{readIn[15],readIn[16]}))/100;
            System.out.println("SO2(" +sensorType + ")is "+SO2+"PPM");
            result.put("SO2",String.valueOf(SO2));
            float O3 = ((float)byteConvertToInt(new byte[]{readIn[17],readIn[18]}))/100;
            System.out.println("O3(" +sensorType + ")is "+O3+"PPM");
            result.put("O3",String.valueOf(O3));
            re =  true;
        }
        if(sensorType==32||sensorType==33){
            float CH4 = byteConvertToInt(new byte[]{readIn[23],readIn[24]});
            System.out.println("CH4(" +sensorType + ")is "+CH4+"PPM");
            result.put("CH4",String.valueOf(CH4));
            re =  true;
        }
        if(sensorType==35){
            float CO2 = byteConvertToInt(new byte[]{readIn[13],readIn[14]});
            System.out.println("CH4(" +sensorType + ")is "+CO2+"PPM");
            result.put("CO2",String.valueOf(CO2));
            re =  true;
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        result.put("time",df.format(new Date()));
        result.put("Location","[135,545]");
        JSONObject jsonObject = new JSONObject(result);
        System.out.println(jsonObject.toString());
//        getTestData(jsonObject);
        return re;
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
