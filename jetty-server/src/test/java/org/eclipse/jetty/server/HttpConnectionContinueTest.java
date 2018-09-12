package org.eclipse.jetty.server;

import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HttpConnectionContinueTest {

    private Server server;
    private LocalConnector connector;

    @Before
    public void before() throws Exception {

        server = new Server();

        HttpConfiguration config = new HttpConfiguration();
        config.setRequestHeaderSize(1024);
        config.setResponseHeaderSize(1024);
        config.setSendDateHeader(true);
        HttpConnectionFactory http = new HttpConnectionFactory(config);

        connector = new LocalConnector(server,http,null);
        connector.setIdleTimeout(5000);
        server.addConnector(connector);

        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                response.setContentType("text/html;charset=utf-8");

                switch (request.getMethod()) {
                    case "PUT":
                        if ("true".equals(request.getHeader("X-Execution-Continue"))) {
                            response.setStatus(HttpServletResponse.SC_OK);
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

    @After
    public void after() throws Exception {
        server.stop();
        server.join();
    }

    @Test
    public void testContinue() throws Exception {
        final String response = connector.getResponse("PUT / HTTP/1.1\r\n");
        System.out.println(response);
    }

}
