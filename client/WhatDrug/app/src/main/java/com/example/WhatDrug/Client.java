package com.example.WhatDrug;
import java.net.Socket;

public class Client {
        Socket socket = null;
        String serverIp = "34.84.62.242";
        int serverPort = 5000;
        String fileName;
        String result;
        String[] array;

    public Client(String fileName) {
        this.fileName = fileName;

        try {
            // 서버 연결
            socket = new Socket(serverIp, serverPort); // socket(),connect();
            System.out.println("서버에 연결되었습니다.");
            System.out.println(serverIp + " : " + serverPort);

            FileSender fileSender = new FileSender(socket, fileName);
            fileSender.start();
            fileSender.join();
            result = fileSender.getResult();
            array = result.split("///");//출력
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String[] getResult() { return array; }

}