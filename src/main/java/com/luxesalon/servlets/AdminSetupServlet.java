package com.luxesalon.servlets;

import com.luxesalon.utils.MongoConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/setup-admin")
public class AdminSetupServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> users = db.getCollection("users");
            
            // Upsert initial admin
            Document admin = new Document("email", "admin@salon.com")
                .append("name", "Salon Admin")
                .append("password", "admin123")
                .append("phone", "9876543210")
                .append("role", "admin");
            
            users.updateOne(
                Filters.eq("email", "admin@salon.com"),
                new Document("$set", admin),
                new UpdateOptions().upsert(true)
            );
            
            response.getWriter().write("Admin setup successful! Login with admin@salon.com / admin123");
        } catch (Exception e) {
            response.getWriter().write("Setup failed: " + e.getMessage());
        }
    }
}
