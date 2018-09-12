package org.eclipse.jetty.server.response1xx;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

public class JettyClientTest {

    private static final Server server = new Server(8080);

    @BeforeClass
    public static void before() throws Exception {

        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
                response.setContentType("text/html;charset=utf-8");

                switch (request.getMethod()) {
                    case "PUT":
                        if ("true".equals(request.getHeader("X-Execution-Continue"))) {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().println("<h1>Hello World</h1>");
                            baseRequest.setHandled(true);
                        } else {
                            response.setStatus(150);
                            baseRequest.setHandled(true);
                        }
                        break;
                }
            }

        });

        server.start();
    }

    @AfterClass
    public static void after() throws Exception {
        server.stop();
    }

    @Test
    public void testJettyClient() throws Exception {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        ContentResponse response = sendRequest(httpClient, false);
        System.out.printf("Got response code %d%n", response.getStatus());
        assertEquals(150, response.getStatus());

        response = sendRequest(httpClient, true);
        System.out.printf("Got response code %d%n", response.getStatus());
        assertEquals(200, response.getStatus());
    }

    private ContentResponse sendRequest(HttpClient httpClient, boolean execution) throws InterruptedException, TimeoutException, ExecutionException {
        final org.eclipse.jetty.client.api.Request request = httpClient.newRequest("http://localhost:8080/");
        request.method(HttpMethod.PUT)
                .header("X-Execution-Continue", String.valueOf(execution))
                .content(new InputStreamContentProvider(new ByteArrayInputStream("Hello".getBytes(StandardCharsets.UTF_8))), "text/plain");
        return request.send();
    }

}
