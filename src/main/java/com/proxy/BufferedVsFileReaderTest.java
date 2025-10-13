package com.proxy;


import java.io.*;

public class BufferedVsFileReaderTest {
    public static void main(String[] args) {

        File f = new File("testbu.txt");
        createTestFiles(f, 100_000);

        //파일리더
        long t1 = System.currentTimeMillis();
        try {
            FileReader fr = new FileReader(f);
            while (fr.read() !=-1);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        long t2 = System.currentTimeMillis();

        long t3 = System.currentTimeMillis();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
            while (bufferedReader.readLine() != null);
        }catch (Exception e) {
            e.printStackTrace();
        }
        long t4 = System.currentTimeMillis();

        System.out.println("File Reader time: " + (t2 - t1) + " ms");
        System.out.println("Buffered Reader time: " + (t4 - t3) + " ms");


    }
    static void createTestFiles(File f, int lines) {
        if (f.exists()) return;
        try {
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        for (int i = 0; i < lines; i++) {
            bw.write("Hello Thread Test ! " + i);
            bw.newLine();
        }
        bw.close();
        }catch (Exception e) {
            e.printStackTrace();
        }



    }


}
