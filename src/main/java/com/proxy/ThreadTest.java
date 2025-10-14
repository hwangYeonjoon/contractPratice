package com.proxy;

public class ThreadTest {

    // 테스트할 메서드
    public static void testMethod(String threadName) {
        for (int i = 1; i <= 5; i++) {
            System.out.println(threadName + " - count: " + i);
            try {
                Thread.sleep(500); // 0.5초 지연
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // 단일 스레드 실행
        System.out.println("=== Single Thread Test ===");
        testMethod("MainThread");

        // 다중 스레드 실행
        System.out.println("\n=== Multi Thread Test ===");
        Thread t1 = new Thread(() -> testMethod("Thread-1"));
        Thread t2 = new Thread(() -> testMethod("Thread-2"));
        Thread t3 = new Thread(() -> testMethod("Thread-3"));

        // 스레드 시작
        t1.start();
        t2.start();
        t3.start();
    }
}