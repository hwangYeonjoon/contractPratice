package com.proxy;

import java.io.*;

public class StreamCopyTest {
    public static void main(String[] args) {
        File source = new File("testbu.txt");
        File target1 = new File("copy1_no_buffer.txt");
        File target2 = new File("copy2_with_buffer.txt");

        long start1 = System.currentTimeMillis();
        copyWithoutBuffer(source,target1);
        long end1 = System.currentTimeMillis();


        long start2 =System.currentTimeMillis();
        copyWithBuffer(source,target2);
        long end2 = System.currentTimeMillis();


        System.out.println("Without buffer: " + (end1 - start1) + "ms");
        System.out.println("With buffer   : " + (end2 - start2) + "ms");

    }

    static void copyWithoutBuffer(File src, File dest) {
        try {

            FileInputStream fis = new FileInputStream(src);
            FileOutputStream fos = new FileOutputStream(dest);
            int read;
            while ((read = fis.read()) != -1) {
                fos.write(read);
            }
            fis.close();
            fos.close();

        }catch (Exception e){
            e.printStackTrace();
        }


    }


    static void copyWithBuffer(File src, File dest) {
        try {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(src));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest));

        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = bis.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        bos.close();

        }catch (Exception e){
            e.printStackTrace();
        }


    }





}
