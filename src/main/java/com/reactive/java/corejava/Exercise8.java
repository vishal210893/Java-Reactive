package com.reactive.java.corejava;

import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

public class Exercise8 {

    public static void main(String[] args) throws IOException {

        // Use ReactiveSources.intNumbersFluxWithException()
        Flux<Integer> intNumbersFluxWithException = ReactiveSources.intNumbersFluxWithException();

        // Print values from intNumbersFluxWithException and print a message when error happens
        intNumbersFluxWithException.subscribe(System.out::println, System.out::println);

        // Print values from intNumbersFluxWithException and continue on errors
        intNumbersFluxWithException
                .onErrorContinue((exception, element) -> {
                })
                .subscribe(System.out::println, System.out::println);

        // Print values from intNumbersFluxWithException and when errors
        // happen, replace with a fallback sequence of -1 and -2
        intNumbersFluxWithException
                .onErrorResume(a -> Flux.just(-1, -2))
                .doFinally(signalType -> {
                    System.out.println(signalType);
                })
                .subscribe(System.out::println, System.out::println);

        System.out.println("Press a key to end");
        System.in.read();

        final List<String> list = IntStream.range(1, 10).boxed().map(i -> (i * 2) + "a").toList();
        System.out.println(list);

    }
}