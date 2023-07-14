package com.reactive.java.corejava;

import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

public class Exercise3 {

    public static void main(String[] args) throws IOException {

        Flux<Integer> intNumbersFlux = ReactiveSources.intNumbersFlux();

        // Get all numbers in the ReactiveSources.intNumbersFlux stream
        // into a List and print the list and its size
        List<Integer> numbers = intNumbersFlux.log().toStream().toList();

        System.out.println(numbers);
        System.out.println("Size: " + numbers.size());

        System.out.println("Press a key to end");
        System.in.read();
    }

}