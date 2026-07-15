package com.luxesalon.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.luxesalon.utils.MongoConnection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/queue")
public class QueueServlet extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();
        
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            
            // Calculate real-time queue statistics
            long totalWaiting = db.getCollection("appointments").countDocuments(Filters.eq("status", "waiting"));
            Document servingDoc = db.getCollection("appointments")
                .find(Filters.eq("status", "serving"))
                .sort(new Document("createdAt", -1))
                .first();
            
            jsonResponse.addProperty("nowServing", servingDoc != null ? servingDoc.getString("tokenNumber") : "NONE");
            jsonResponse.addProperty("totalWaiting", (int) totalWaiting);
            
            // Fetch live queue list (waiting and serving)
            List<Document> currentQueue = db.getCollection("appointments")
                .find(Filters.or(Filters.eq("status", "waiting"), Filters.eq("status", "serving")))
                .sort(new Document("createdAt", 1))
                .limit(20)
                .into(new ArrayList<>());
            
            JsonArray queueArray = new JsonArray();
            for (Document doc : currentQueue) {
                JsonObject obj = new JsonObject();
                obj.addProperty("token", doc.getString("tokenNumber"));
                obj.addProperty("userName", doc.getString("userName") != null ? doc.getString("userName") : "Anonymous");
                obj.addProperty("status", doc.getString("status"));
                obj.addProperty("time", doc.getString("appointmentTime"));
                queueArray.add(obj);
            }
            jsonResponse.add("queue", queueArray);
            
            // If user is logged in, get their active token status
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("userId") != null) {
                String userId = (String) session.getAttribute("userId");
                Document myAppointment = db.getCollection("appointments")
                    .find(Filters.and(
                        Filters.eq("userId", userId),
                        Filters.in("status", "waiting", "serving")
                    ))
                    .sort(new Document("createdAt", -1))
                    .first();
                
                if (myAppointment != null) {
                    JsonObject myStatus = new JsonObject();
                    myStatus.addProperty("token", myAppointment.getString("tokenNumber"));
                    myStatus.addProperty("status", myAppointment.getString("status"));
                    myStatus.addProperty("waitTime", myAppointment.getInteger("estimatedWaitTime"));
                    
                    // Count how many people are before this token
                    long beforeMe = db.getCollection("appointments").countDocuments(
                        Filters.and(
                            Filters.eq("status", "waiting"),
                            Filters.lt("createdAt", myAppointment.getDate("createdAt"))
                        )
                    );
                    myStatus.addProperty("position", beforeMe + 1);
                    jsonResponse.add("myStatus", myStatus);
                }
            }
            
            out.print(gson.toJson(jsonResponse));
            
        } catch (Exception e) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", e.getMessage());
            out.print(gson.toJson(jsonResponse));
        } finally {
            out.flush();
        }
    }
}
