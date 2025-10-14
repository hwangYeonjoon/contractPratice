package com.proxy;

interface Animal{
    void sound();
}


class Dog implements Animal{
    public void sound() {
        System.out.println("woof");
    }
}

class Cat implements Animal{
   public void sound() {
        System.out.println("meow");
    }
}

public class Testinterface {
    public static void main(String[] args) {
        Dog dog = new Dog();
        Cat cat = new Cat();
        System.out.println("직접 호출 : ");
        dog.sound();
        cat.sound();
        System.out.println("인터페이스 호출 : ");
        makeSound(dog);
        makeSound(cat);

    }

    static void makeSound(Animal a){
        a.sound();
    }

}
