package com.luxesalon.servlets;

import com.google.gson.Gson;
import com.luxesalon.utils.MongoConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple profile servlet to get and update user profile information.
 * Interacts with users collection in MongoDB.
 */
@WebServlet(name = "ProfileServlet", urlPatterns = {"/api/profile"})
public class ProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String userId = (String) session.getAttribute("userId");
        MongoDatabase db = MongoConnection.getDatabase();
        MongoCollection<Document> usersCollection = db.getCollection("users");
        
        Document user = usersCollection.find(Filters.eq("_id", new ObjectId(userId))).first();
        
        Map<String, String> userData = new HashMap<>();
        if (user != null) {
            userData.put("name", user.getString("name"));
            userData.put("email", user.getString("email"));
        }

        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(userData));
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = req.getReader().readLine()) != null) {
            sb.append(line);
        }
        Map<String, String> data = gson.fromJson(sb.toString(), Map.class);
        
        String userId = (String) session.getAttribute("userId");
        String newName = data.get("fullName");
        if (newName == null) newName = data.get("name");
        String newEmail = data.get("email");
        
        if (newName != null && newEmail != null) {
            MongoDatabase db = MongoConnection.getDatabase();
            MongoCollection<Document> usersCollection = db.getCollection("users");
            
            usersCollection.updateOne(
                Filters.eq("_id", new ObjectId(userId)),
                Updates.combine(
                    Updates.set("name", newName),
                    Updates.set("email", newEmail)
                )
            );
            session.setAttribute("userName", newName);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Profile updated");
        result.put("data", data);
        
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(result));
        out.flush();
    }
}
