package com.luxesalon.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.luxesalon.utils.MongoConnection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/admin")
public class AdminServlet extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();
        
        HttpSession session = request.getSession(false);
        if (session == null || !"admin".equals(session.getAttribute("role"))) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Unauthorized access");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print(gson.toJson(jsonResponse));
            return;
        }

        try {
            MongoDatabase db = MongoConnection.getDatabase();
            
            // 1. Stats
            long totalBookings = db.getCollection("appointments").countDocuments();
            long waitingCount = db.getCollection("appointments").countDocuments(Filters.eq("status", "waiting"));
            long servingCount = db.getCollection("appointments").countDocuments(Filters.eq("status", "serving"));
            long completedCount = db.getCollection("appointments").countDocuments(Filters.eq("status", "completed"));
            
            jsonResponse.addProperty("totalBookings", totalBookings);
            jsonResponse.addProperty("waitingCount", waitingCount);
            jsonResponse.addProperty("servingCount", servingCount);
            jsonResponse.addProperty("completedCount", completedCount);
            
            // 1.5 Fetch Services for mapping
            List<Document> services = db.getCollection("services").find().into(new ArrayList<>());
            java.util.Map<String, String> serviceMap = new java.util.HashMap<>();
            for (Document s : services) {
                serviceMap.put(s.getObjectId("_id").toString(), s.getString("name"));
            }

            // 2. Recent Queue
            List<Document> recentAppointments = db.getCollection("appointments")
                .find(Filters.or(Filters.eq("status", "waiting"), Filters.eq("status", "serving")))
                .sort(new Document("createdAt", 1))
                .limit(10)
                .into(new ArrayList<>());
            
            JsonArray queueArray = new JsonArray();
            for (Document doc : recentAppointments) {
                JsonObject obj = new JsonObject();
                obj.addProperty("token", doc.getString("tokenNumber"));
                obj.addProperty("userName", doc.getString("userName"));
                obj.addProperty("status", doc.getString("status"));
                obj.addProperty("time", doc.getString("appointmentTime"));
                obj.addProperty("notes", doc.getString("notes") != null ? doc.getString("notes") : "");
                
                List<String> sIds = (List<String>) doc.get("serviceIds");
                java.util.List<String> sNames = new ArrayList<>();
                if (sIds != null) {
                    for (String id : sIds) {
                        sNames.add(serviceMap.getOrDefault(id, "Unknown Service"));
                    }
                }
                obj.addProperty("services", String.join(", ", sNames));
                queueArray.add(obj);
            }
            jsonResponse.add("queue", queueArray);
            
            // 3. All Bookings
            List<Document> allAppts = db.getCollection("appointments")
                .find()
                .sort(new Document("createdAt", -1))
                .limit(100)
                .into(new ArrayList<>());
            
            JsonArray bookingsArray = new JsonArray();
            for (Document doc : allAppts) {
                JsonObject obj = new JsonObject();
                obj.addProperty("token", doc.getString("tokenNumber"));
                obj.addProperty("userName", doc.getString("userName"));
                obj.addProperty("status", doc.getString("status"));
                obj.addProperty("date", doc.getString("appointmentDate"));
                obj.addProperty("time", doc.getString("appointmentTime"));
                obj.addProperty("notes", doc.getString("notes") != null ? doc.getString("notes") : "");

                List<String> sIds = (List<String>) doc.get("serviceIds");
                java.util.List<String> sNames = new ArrayList<>();
                if (sIds != null) {
                    for (String id : sIds) {
                        sNames.add(serviceMap.getOrDefault(id, "Unknown Service"));
                    }
                }
                obj.addProperty("services", String.join(", ", sNames));
                bookingsArray.add(obj);
            }
            jsonResponse.add("allBookings", bookingsArray);

            // 4. All Users (Registered Customers)
            List<Document> allUsers = db.getCollection("users")
                .find(Filters.eq("role", "customer"))
                .sort(new Document("name", 1))
                .into(new ArrayList<>());
            
            JsonArray usersArray = new JsonArray();
            for (Document doc : allUsers) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", doc.getObjectId("_id").toString());
                obj.addProperty("name", doc.getString("name"));
                obj.addProperty("email", doc.getString("email"));
                obj.addProperty("phone", doc.getString("phone") != null ? doc.getString("phone") : "");
                usersArray.add(obj);
            }
            jsonResponse.add("users", usersArray);
            
            out.print(gson.toJson(jsonResponse));
            
        } catch (Exception e) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", e.getMessage());
            out.print(gson.toJson(jsonResponse));
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();
        
        HttpSession session = request.getSession(false);
        if (session == null || !"admin".equals(session.getAttribute("role"))) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Unauthorized access");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print(gson.toJson(jsonResponse));
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) { sb.append(line); }

            JsonObject jsonObject = gson.fromJson(sb.toString(), JsonObject.class);
            String action = jsonObject.get("action").getAsString(); // "serve" or "complete"
            String token = jsonObject.get("token").getAsString();

            MongoDatabase db = MongoConnection.getDatabase();
            
            if ("serve".equals(action)) {
                // Update appointment
                db.getCollection("appointments").updateOne(
                    Filters.eq("tokenNumber", token),
                    Updates.set("status", "serving")
                );
                // Update queue_status
                db.getCollection("queue_status").updateOne(
                    new Document(),
                    Updates.combine(
                        Updates.set("currentServingToken", token),
                        Updates.inc("totalWaiting", -1),
                        Updates.set("lastUpdated", new java.util.Date().toString())
                    )
                );
            } else if ("complete".equals(action)) {
                db.getCollection("appointments").updateOne(
                    Filters.eq("tokenNumber", token),
                    Updates.set("status", "completed")
                );
                // Reset serving token if it was this one
                db.getCollection("queue_status").updateOne(
                    Filters.eq("currentServingToken", token),
                    Updates.set("currentServingToken", "NONE")
                );
            }
            
            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("message", "Status updated successfully");
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
