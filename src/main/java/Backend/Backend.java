package Backend;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
public class Backend {

    
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        System.out.println("Server started on port " + port);

        server.createContext("/auto-complete", new AutoCompleteHandler());

        server.createContext("/search", new SearchHandler());

        server.setExecutor(null);
        server.start();
    }

    private static class AutoCompleteHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange arg0) throws IOException {
            String response = "This is autocomplete code";
            arg0.sendResponseHeaders(200, response.length());

            OutputStream os = arg0.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

    }

    private static class SearchHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange arg0) throws IOException {
            String response = "This is search code";
            arg0.sendResponseHeaders(200, response.length());

            OutputStream os = arg0.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

    }
}
