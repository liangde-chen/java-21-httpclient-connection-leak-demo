package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/hello")
public class HelloController {

    private List<Hello> hellos = new ArrayList<>();
    private final Random random = new Random();


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Flux<Hello> create(@RequestBody Hello hello){
        hellos.add(hello);
        if (hellos.size() > 10000000) {
            hellos = new ArrayList<>();
        }


        int randomValue = random.nextInt(10);
        if (randomValue == 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Random error");
        } else if (randomValue == 1){
            throw new ResponseStatusException(HttpStatus.valueOf(415), "Random error");
        } else if (randomValue == 2){
            throw new ResponseStatusException(HttpStatus.valueOf(418), "Random error");
        } else if (randomValue == 3){
            throw new ResponseStatusException(HttpStatus.valueOf(429), "Random error");
        } else if (randomValue == 4){
            return Flux.fromStream(new ArrayList<>(hellos).stream().limit(12));
//                    .delaySequence(java.time.Duration.ofSeconds(1));
        }


        return Flux.just(hello)
                // Add delay to simulate a slow service
//                .delayElements(java.time.Duration.ofSeconds(1))
                .filter(h -> false);
    }

    @GetMapping
    public Flux<Hello> list(){
        return Flux.fromStream(new ArrayList<>(hellos).stream())
                .delaySequence(java.time.Duration.ofSeconds(1));

    }
}