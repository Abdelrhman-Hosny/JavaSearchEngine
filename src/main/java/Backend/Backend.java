package Backend;


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.sun.net.httpserver.HttpServer;

import Database.BaseDAO;
import Database.DocumentDAO;
import Database.QueryDAO;
import Database.ResponseObject;
import Preprocessing.Preprocessor;
import Ranker.Ranker;
import Ranker.Ranker.Entry;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class Backend {


    static QueryDAO queryManager = new QueryDAO();
    static DocumentDAO documentManager = new DocumentDAO();
    static Ranker rankerObj = new Ranker();
    static Preprocessor PreprocessorObj = new Preprocessor();
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
            
            // splitting url got to get url param
            String queryGot = arg0.getRequestURI().toString().split("&query=")[1].replace("%20", " ");
            // getting autocomplete suggestions from database
            List<String> autoCompletes = queryManager.Retrieve_query(queryGot);
            arg0.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            final String responseBody = "{\"list\" : ["+ String.join(", ", autoCompletes) + "]}";
            final Headers headers = arg0.getResponseHeaders();
            headers.set("Content-Type", String.format("application/json; charset=%s", StandardCharsets.UTF_8));
            final byte[] rawResponseBody = responseBody.getBytes(StandardCharsets.UTF_8);
            arg0.sendResponseHeaders(200, rawResponseBody.length);
            arg0.getResponseBody().write(rawResponseBody);
            arg0.close();
        }

    }

    private static class SearchHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange arg0) throws IOException {
            
            // splitting url got to get url param
            String queryGot = arg0.getRequestURI().toString().split("&query=")[1].replace("%20", " ");
            // insert the query got into the database
            queryManager.Insert_query(queryGot);
            // preprocessing on the query 
            queryGot = PreprocessorObj.stem(PreprocessorObj.removeStopwords(queryGot));
            // here you should return response objects in form of ResponseObject class made in this folder
            Gson gson = new Gson();
            // calling data base on array list
            ArrayList<ResponseObject> res = new ArrayList<>();
            try {
                Object[] entryArray = rankerObj.process(queryGot);
                String finalWords = "(";
                for (int i = 0; i < entryArray.length; i++) {
                    finalWords += "'"+ ((Entry) entryArray[i]).getKey() +"'"+ ",";
                }
                int index = finalWords.lastIndexOf(',');
                finalWords = finalWords.substring(0,index);
                finalWords += ")";
                ResultSet rs =documentManager.GetallDocumentswithUrls(finalWords);
                while(rs.next()){
                    res.add(new ResponseObject(rs.getString("document_name"), rs.getString("title"), rs.getString("snippet")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String json = "{}";
            if(res.size()!=0){
                json = gson.toJson(res);
                System.out.println("json" + json);
            }
            
            arg0.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            // will contain the search results after ranker
            final String responseBody = "{\"list\" : " + json + "}";
            final Headers headers = arg0.getResponseHeaders();
            headers.set("Content-Type", String.format("application/json; charset=%s", StandardCharsets.UTF_8));
            final byte[] rawResponseBody = responseBody.getBytes(StandardCharsets.UTF_8);
            arg0.sendResponseHeaders(200, rawResponseBody.length);
            arg0.getResponseBody().write(rawResponseBody);
            arg0.close();

            
        }

    }
}
