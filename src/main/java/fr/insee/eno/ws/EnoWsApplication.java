package fr.insee.eno.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.all;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class EnoWsApplication {

    @Value("${test.url}")
    private String baseUrl;
    @Autowired
    private WebClient.Builder builder;

    public static void main(String[] args) {
        SpringApplication.run(EnoWsApplication.class, args);
    }

    //@Bean
    public RouterFunction<ServerResponse> monoRouterFunction() {
        return route()
                .GET(all(), this::passePlatGet)
                .build();
    }

    private Mono<ServerResponse> passePlatGet(ServerRequest serverRequest) {
        var webClient = this.builder.baseUrl(this.baseUrl).build();
        return webClient.get()
                .uri(serverRequest.path())
                .headers(httpHeaders -> {
                    httpHeaders.clear();
                    httpHeaders.addAll(serverRequest.headers().asHttpHeaders());
                })
                .retrieve()//exchange() : to access to the full server respsonse
                .toEntityFlux(DataBuffer.class)
                .flatMap(r -> ServerResponse.status(r.getStatusCode())
                        .headers(httpHeaders -> {
                            httpHeaders.clear();
                            httpHeaders.addAll(r.getHeaders());
                        })
                        .body(r.getBody(), DataBuffer.class)
                );
    }


}
