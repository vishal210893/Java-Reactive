package com.reactive.java.corejava;

import reactor.core.publisher.Flux;

import java.io.IOException;

public class Exercise2 {

    public static void main(String[] args) throws IOException {

        Flux<Integer> intNumbersFlux = ReactiveSources.intNumbersFlux();
        Flux<User> userFlux = ReactiveSources.userFlux();
        System.out.println(Thread.currentThread().getName());

        // Print all numbers in the ReactiveSources.intNumbersFlux stream
        intNumbersFlux.subscribe(number -> {
            System.out.println(number);
            //System.out.println(Thread.currentThread().getName());
        });

        intNumbersFlux.subscribe(number -> {
            System.out.println(number * 10);
            //System.out.println(Thread.currentThread().getName());
        });


        /*// Print all users in the ReactiveSources.userFlux stream
        userFlux.subscribe(user -> {
            System.out.println(user);
            System.out.println(Thread.currentThread().getName());
        });*/

        System.out.println("Press a key to end");
        System.in.read();
    }

}