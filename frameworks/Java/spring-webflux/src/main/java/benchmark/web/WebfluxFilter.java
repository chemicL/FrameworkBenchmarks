package benchmark.web;

import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.HttpHeaderNames;

import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.netty.util.CharsetUtil;
import reactor.core.publisher.Mono;

@Component
public class WebfluxFilter implements WebFilter {

    private static final String SERVER_NAME = "spring-webflux";
    private static final String CONTENT_TYPE = "text/plain";

    private static final byte[] STATIC_PLAINTEXT = "Hello, World!".getBytes(CharsetUtil.UTF_8);
    private static final int STATIC_PLAINTEXT_LEN = STATIC_PLAINTEXT.length;

    private static final String PLAINTEXT_CL_HEADER_VALUE = String.valueOf(STATIC_PLAINTEXT_LEN);

    private String date;
    private NettyDataBufferFactory factory;

    public WebfluxFilter() {
        factory = new NettyDataBufferFactory(UnpooledByteBufAllocator.DEFAULT);
        updateDate();
    }

    @Scheduled(fixedRate = 1000)
    public void updateDate() {
        this.date = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME.format(java.time.ZonedDateTime.now());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HttpHeaders headers = exchange.getResponse().getHeaders();
        headers.add(HttpHeaderNames.SERVER.toString(), SERVER_NAME);
        headers.add(HttpHeaderNames.DATE.toString(), this.date);

        // short-circuit
        headers.add(HttpHeaderNames.CONTENT_TYPE.toString(), CONTENT_TYPE);
        headers.add(HttpHeaderNames.CONTENT_LENGTH.toString(), PLAINTEXT_CL_HEADER_VALUE);
        exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(200));
        return exchange.getResponse().writeWith(Mono.just(factory.wrap(STATIC_PLAINTEXT)));

        // end short-circuit

        // skip routing
//        return chain.filter(exchange);
    }
}
