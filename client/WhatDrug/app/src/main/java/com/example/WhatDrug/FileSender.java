package com.example.WhatDrug;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

class FileSender extends Thread {
    private Socket socket;
    private FileInputStream fis;
    private BufferedOutputStream bos;

    private String filename;

    private OutputStream os;

    private InputStream is;
    private BufferedInputStream bis;

    private int fileSize;
    private String result;


    public FileSender(Socket socket, String filestr) {
        this.socket = socket;
        this.filename = filestr;
        try {
            // 데이터 스트림 생성
            this.os = socket.getOutputStream();
            bos = new BufferedOutputStream(os);
            this.is = socket.getInputStream();
            //외부로 부터 읽어오는 역할
            bis = new BufferedInputStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean sendFileSize(String fileName) throws IOException {
        File imageFile = new File("/sdcard/Pictures/image.jpg");
        fileSize = (int) imageFile.length();
        fis = new FileInputStream(imageFile);
        bos.write(Integer.toString(fileSize).getBytes());
        bos.flush();
        System.out.println("send file size : " + fileSize);
        return true;
    }


    public void sendImage(int fileSize) throws IOException {

        File imageFile = new File("/sdcard/Pictures/image.jpg");
        byte[] data = new byte[(int) (fileSize)];
        fis = new FileInputStream(imageFile);
        bos.write(data, 0, fis.read(data));
        System.out.println("send image ... ");
        bos.flush();
        fis.close();
    }

    public String receiveData(int buffer_size,int check) throws IOException {
        byte[] tmp = new byte[buffer_size];
        int zz = bis.read(tmp);

        if(check == 0){
            System.out.println("server recv: "+new String(tmp, 0, zz));
            return new String(tmp,0,zz);
        }

        System.out.println("while run...");
        StringBuilder sb = new StringBuilder();
        while(zz != -1){
            String s = new  String(tmp,0,zz);
            sb.append(s);
            zz = bis.read(tmp);
        }
        return sb.toString();

    }

    public String getResult() {
        return result;
    }

    public void close() {
        try {
            bos.close();
            socket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {

        InetAddress local;
        try {
            local = InetAddress.getLocalHost();
            String ip = local.getHostAddress();
            System.out.println("local ip : "+ip);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        try {
            sendFileSize(filename);
            receiveData(100,0);//확인된 파일 사이즈값 다시 받기
            sendImage(fileSize);
            result = receiveData(1024,1);//문자파일 받기
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }// finally
    }// run


}