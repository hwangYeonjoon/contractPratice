package com.proxy;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SimpleThreadTest {
    public static void main(String[] args) throws Exception {
        int fileCount = 5;
        int linesPerFile = 200_000; // 필요하면 조절
        createTestFiles(fileCount, linesPerFile); // test1..test5 생성

        List<File> files = List.of(
                new File("test1.txt"),
                new File("test2.txt"),
                new File("test3.txt"),
                new File("test4.txt"),
                new File("test5.txt")
        );
        // --- Multi thread ---
        ExecutorService pool = Executors.newFixedThreadPool(5);
        long t2 = System.nanoTime();
        List<Future<Long>> futures = new ArrayList<>();
        for (File f : files) {
            futures.add(pool.submit(() -> readFileCountBytes(f)));
        }
        long total2 = 0;
        for (Future<Long> fu : futures) total2 += fu.get();
        long t3 = System.nanoTime();
        pool.shutdown();

        // --- Single thread ---
        long t0 = System.nanoTime();
        long total1 = 0;
        for (File f : files) {
            total1 += readFileCountBytes(f);
        }
        long t1 = System.nanoTime();


        System.out.printf("Multi  thread time: %d ms (bytes=%d)%n", (t3 - t2)/1_000_000, total2);
        System.out.printf("Single thread time: %d ms (bytes=%d)%n", (t1 - t0)/1_000_000, total1);
    }

    // 파일 전체를 읽되, 콘솔 출력 없이 읽은 바이트 수만 합산
    static long readFileCountBytes(File file) {
        long read = 0;
        char[] buf = new char[8192];
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            int n;
            while ((n = br.read(buf)) != -1) {
                read += n;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return read;
    }

    // test1..testN 생성 (이름 통일)
    static void createTestFiles(int n, int lines) {
        for (int i = 1; i <= n; i++) {
            File f = new File("test" + i + ".txt");
            if (f.exists()) continue;
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
                for (int j = 0; j < lines; j++) {
                    bw.write("Hello Thread Test ! " + i + " line=" + j);
                    bw.newLine();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}