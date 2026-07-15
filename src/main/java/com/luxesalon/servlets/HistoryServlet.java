package com.luxesalon.servlets;

import com.google.gson.Gson;
import com.luxesalon.utils.MongoConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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

/**
 * Returns the appointment history for the logged‑in user.
 * The response is a JSON array of appointment objects.
 */
@WebServlet(name = "HistoryServlet", urlPatterns = {"/api/history"})
public class HistoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(gson.toJson(new ErrorResponse("User not logged in")));
            return;
        }
        String userId = (String) session.getAttribute("userId");
        MongoDatabase db = MongoConnection.getDatabase();
        MongoCollection<Document> coll = db.getCollection("appointments");
        List<Document> docs = coll.find(new Document("userId", userId)).into(new ArrayList<>());
        out.print(gson.toJson(docs));
    }

    private static class ErrorResponse {
        String status = "error";
        String message;
        ErrorResponse(String message) { this.message = message; }
    }
}
