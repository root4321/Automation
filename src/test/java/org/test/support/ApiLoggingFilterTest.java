package org.test.support;

import com.sun.net.httpserver.HttpServer;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiLoggingFilterTest {
    private HttpServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/echo", exchange -> {
            byte[] body = "{\"status\":\"OK\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.getResponseHeaders().add("X-Trace", "abc-123");
            exchange.sendResponseHeaders(201, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void logsRequestAndResponseHeadersAndBodiesWithSecretsMasked() {
        List<String> logs = new ArrayList<>();

        given().filter(new ApiLoggingFilter(logs::add))
                .baseUri("http://localhost:" + server.getAddress().getPort())
                .queryParam("key", "super-secret").queryParam("page", "2")
                .header("Authorization", "Bearer token-value").header("X-Custom", "yes")
                .contentType(ContentType.JSON).body("{\"name\":\"shop\"}")
                .post("/echo").then().statusCode(201);

        assertEquals(1, logs.size());
        String log = logs.get(0);
        assertTrue(log.contains("POST http://localhost:" + server.getAddress().getPort() + "/echo?key=****&page=2"), log);
        assertTrue(log.contains("X-Custom: yes"), log);
        assertTrue(log.contains("Authorization: ****"), log);
        assertTrue(log.contains("\"name\" : \"shop\""), log);
        assertTrue(log.contains("<<< RESPONSE HTTP/1.1 201"), log);
        assertTrue(log.toLowerCase().contains("x-trace: abc-123"), log);
        assertTrue(log.contains("\"status\" : \"OK\""), log);
        assertFalse(log.contains("super-secret") || log.contains("token-value"), log);
    }
}
