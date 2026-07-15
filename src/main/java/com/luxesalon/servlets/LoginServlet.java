package com.luxesalon.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.luxesalon.utils.MongoConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
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

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();
        
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) { sb.append(line); }

            JsonObject jsonObject = gson.fromJson(sb.toString(), JsonObject.class);
            String email = jsonObject.get("email").getAsString();
            String password = jsonObject.get("password").getAsString();

            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> usersCollection = db.getCollection("users");
            
            Document user = usersCollection.find(Filters.and(
                Filters.eq("email", email),
                Filters.eq("password", password)
            )).first();

            if (user != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("userId", user.getObjectId("_id").toString());
                session.setAttribute("userName", user.getString("name"));
                session.setAttribute("role", user.getString("role"));
                
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("role", user.getString("role"));
                jsonResponse.addProperty("userName", user.getString("name"));
                jsonResponse.addProperty("message", "Login successful");
            } else {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid credentials");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
