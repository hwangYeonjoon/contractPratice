package com.proxy;


interface Payment {
    void pay(int amount);
}



class Kakao implements Payment {
    @Override
    public void pay(int amount) {
        System.out.println("💛 KakaoPay로 " + amount + "원 결제 완료!");
    }
}

class CreditCard implements Payment{

    @Override
    public void pay(int amount) {
        System.out.println("💳 신용카드로 " + amount + "원 결제 완료!");
    }
}

class NaverPay implements Payment{
    @Override
    public void pay(int amount) {
        System.out.println("💚 NaverPay로 " + amount + "원 결제 완료!");
    }
}

class TossPay implements Payment{

    @Override
    public void pay(int amount) {
        System.out.println("💚 TossPay로 " + amount + "원 결제 완료!");
    }
}


public class Testinterface2 {
    public static void main(String[] args) {
        System.out.println("=== 직접 호출 ===");
        Payment kakao = new Kakao();
        Payment creditCard = new CreditCard();
        Payment naverPay = new NaverPay();
        Payment tossPay = new TossPay();

        kakao.pay(1000);
        creditCard.pay(2000);
        naverPay.pay(3000);
        System.out.println("\n=== 인터페이스로 호출 ===");
        processPayment(kakao, 1000);
        processPayment(creditCard, 2000);
        processPayment(naverPay, 3000);
        System.out.println("\n=== 다형성 활용 ===");
        Payment payment = new Kakao(); // 나중에 어떤 결제수단으로 바꿔도 코드 수정 X
        payment.pay(7000);
        processPayment(tossPay, 7000);


    }

    static void processPayment(Payment payment, int amount) {
        payment.pay(amount);
    }
}

