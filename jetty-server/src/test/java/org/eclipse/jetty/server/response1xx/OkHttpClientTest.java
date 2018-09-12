package org.eclipse.jetty.server.response1xx;

import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class OkHttpClientTest {

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
    public void test() throws IOException {

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient().newBuilder();
        okHttpClientBuilder.followRedirects(true);
        okHttpClientBuilder.connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES));


        final OkHttpClient client = okHttpClientBuilder.build();

        // Send the 1st request
        Call call = client.newCall(createRequest(false));
        Response response = call.execute();
        System.out.printf("Got response code %d%n", response.code());
        assertEquals(150, response.code());

        // Send the 2nd request
        call = client.newCall(createRequest(true));
        response = call.execute();
        System.out.printf("Got response code %d%n", response.code());
        assertEquals(200, response.code());

    }

    private okhttp3.Request createRequest(boolean execution) {
        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder();
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), "Hello");
        return requestBuilder.url("http://localhost:8080")
                .header("X-Execution-Continue", String.valueOf(execution))
                .method("PUT", body)
                .build();
    }

}
