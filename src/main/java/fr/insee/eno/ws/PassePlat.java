package fr.insee.eno.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class PassePlat {

    @Autowired
    private WebClient webClient;

    public Mono<Void> passePlatGet(ServerHttpRequest serverRequest, ServerHttpResponse response) {
        return response.writeWith(this.webClient.get()
                .uri(serverRequest.getURI().getPath())
                .headers(httpHeaders -> {
                    httpHeaders.clear();
                    httpHeaders.addAll(serverRequest.getHeaders());
                })
                .retrieve()//exchange() : to access to the full server respsonse
                .toEntityFlux(DataBuffer.class)
                .flux()
                .flatMap(r -> {
                    response.setStatusCode(r.getStatusCode());
                    response.getHeaders().clear();
                    response.getHeaders().addAll(r.getHeaders());
                    return r.getBody();
                }));
    }

}