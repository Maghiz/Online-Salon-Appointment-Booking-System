package com.luxesalon.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.luxesalon.utils.MongoConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

@WebServlet("/api/bookings")
public class BookingServlet extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "User must be logged in to book an appointment");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(jsonResponse));
            return;
        }

        try {
            String userId = (String) session.getAttribute("userId");
            String userName = (String) session.getAttribute("userName");
            
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) { sb.append(line); }

            JsonObject jsonObject = gson.fromJson(sb.toString(), JsonObject.class);
            // Support single serviceId or multiple serviceIds
            List<String> serviceIds = new ArrayList<>();
            if (jsonObject.has("serviceIds")) {
                jsonObject.getAsJsonArray("serviceIds").forEach(e -> serviceIds.add(e.getAsString()));
            } else if (jsonObject.has("serviceId")) {
                serviceIds.add(jsonObject.get("serviceId").getAsString());
            }
            
            String date = jsonObject.get("date").getAsString();
            String time = jsonObject.get("time").getAsString();
            String notes = jsonObject.has("notes") ? jsonObject.get("notes").getAsString() : "";

            MongoDatabase db = MongoConnection.getDatabase();
            
            // Generate Token
            String token = generateToken(db);
            
            // For now, simple simulation of wait time: 15 mins per person currently waiting
            long currentlyWaiting = db.getCollection("appointments").countDocuments(new Document("status", "waiting"));
            int estimatedWaitTime = (int) (currentlyWaiting * 15);

            MongoCollection<Document> appointmentsCollection = db.getCollection("appointments");
            
            Document newAppointment = new Document("userId", userId)
                .append("userName", userName)
                .append("tokenNumber", token)
                .append("serviceIds", serviceIds)
                .append("appointmentDate", date)
                .append("appointmentTime", time)
                .append("status", "waiting")
                .append("estimatedWaitTime", estimatedWaitTime)
                .append("notes", notes)
                .append("createdAt", new Date());

            appointmentsCollection.insertOne(newAppointment);
            
            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("token", token);
            jsonResponse.addProperty("waitTime", estimatedWaitTime);
            jsonResponse.addProperty("message", "Booking successful! Your token is " + token);
            out.print(gson.toJson(jsonResponse));
            
        } catch (Exception e) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", e.getMessage());
            out.print(gson.toJson(jsonResponse));
        } finally {
            out.flush();
        }
    }

    private String generateToken(MongoDatabase db) {
        MongoCollection<Document> counters = db.getCollection("counters");
        Document counter = counters.findOneAndUpdate(
            Filters.eq("_id", "appointmentToken"),
            new Document("$inc", new Document("seq", 1)),
            new com.mongodb.client.model.FindOneAndUpdateOptions().upsert(true).returnDocument(com.mongodb.client.model.ReturnDocument.AFTER)
        );
        int seq = counter.getInteger("seq");
        return String.format("TK-%03d", seq);
    }
}
