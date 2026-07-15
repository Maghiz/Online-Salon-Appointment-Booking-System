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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/register")
public class RegisterServlet extends HttpServlet {

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
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JsonObject jsonObject = gson.fromJson(sb.toString(), JsonObject.class);
            String name = jsonObject.get("name").getAsString();
            String email = jsonObject.get("email").getAsString();
            String phone = jsonObject.get("phone").getAsString();
            String password = jsonObject.get("password").getAsString();

            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> usersCollection = db.getCollection("users");
            
            // Check if email exists
            Document existingUser = usersCollection.find(Filters.eq("email", email)).first();
            
            if (existingUser != null) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Email already exists");
                out.print(gson.toJson(jsonResponse));
                return;
            }

            // Insert new user
            Document newUser = new Document("name", name)
                .append("email", email)
                .append("phone", phone)
                .append("password", password) // Should hash in production
                .append("role", "customer");

            usersCollection.insertOne(newUser);
            
            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("message", "Registration successful");
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
