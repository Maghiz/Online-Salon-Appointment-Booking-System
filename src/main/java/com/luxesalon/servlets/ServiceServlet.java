package com.luxesalon.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.luxesalon.utils.MongoConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/services")
public class ServiceServlet extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> servicesCollection = db.getCollection("services");
            
            List<Document> services = servicesCollection.find().into(new ArrayList<>());
            
            JsonArray jsonArray = new JsonArray();
            for (Document doc : services) {
                JsonObject serviceObj = new JsonObject();
                serviceObj.addProperty("id", doc.getObjectId("_id").toString());
                serviceObj.addProperty("name", doc.getString("name"));
                
                // Robustly handle price (Number)
                Object priceObj = doc.get("price");
                double price = (priceObj instanceof Number) ? ((Number) priceObj).doubleValue() : 0.0;
                serviceObj.addProperty("price", price);
                
                // Robustly handle duration (Number/Integer)
                Object durationObj = doc.get("duration");
                int duration = (durationObj instanceof Number) ? ((Number) durationObj).intValue() : 0;
                serviceObj.addProperty("duration", duration);
                
                serviceObj.addProperty("category", doc.getString("category"));
                serviceObj.addProperty("description", doc.getString("description"));
                serviceObj.addProperty("icon", doc.getString("icon"));
                jsonArray.add(serviceObj);
            }
            
            out.print(gson.toJson(jsonArray));
        } catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("status", "error");
            error.addProperty("message", e.getMessage());
            out.print(gson.toJson(error));
        } finally {
            out.flush();
        }
    }
}
