package fr.insee.eno.ws.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class PassePlatController {


    private final WebClient webClient;

    public PassePlatController(@Value("${test.url}") String baseUrl, WebClient.Builder builder) {
        webClient = builder.baseUrl(baseUrl).build();
    }

    @GetMapping("/**")
    public Mono<ResponseEntity<Flux<DataBuffer>>> passePlat(ServerHttpRequest serverRequest){
        return this.webClient.get()
                .uri(serverRequest.getURI().getPath())
                .headers(httpHeaders -> {
                    httpHeaders.clear();
                    httpHeaders.addAll(serverRequest.getHeaders());
                })
                .retrieve()//exchange() : to access to the full server respsonse
                .toEntityFlux(DataBuffer.class);
    }

}
