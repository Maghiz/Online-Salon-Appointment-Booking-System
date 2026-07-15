package com.luxesalon.utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Arrays;
import java.util.List;

@WebListener
public class DatabaseInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Initializing Smart Salon Database...");
        MongoDatabase db = MongoConnection.getDatabase();
        
        initializeServices(db);
        initializeQueue(db);
    }

    private void initializeServices(MongoDatabase db) {
        MongoCollection<Document> services = db.getCollection("services");
        
        // Reset services to INR if they were previously USD (detect by small price values)
        Document sample = services.find().first();
        if (sample != null && sample.getDouble("price") < 100) {
            services.drop();
            System.out.println("Resetting services to INR...");
        }

        if (services.countDocuments() == 0) {
            List<Document> defaultServices = Arrays.asList(
                new Document("name", "Haircut & Styling").append("price", 800.0).append("duration", 30).append("category", "Hair").append("description", "Professional haircut and styling.").append("icon", "💇‍♂️"),
                new Document("name", "Manicure").append("price", 500.0).append("duration", 20).append("category", "Nails").append("description", "Classic manicure for healthy hands.").append("icon", "💅"),
                new Document("name", "Pedicure").append("price", 600.0).append("duration", 30).append("category", "Nails").append("description", "Relaxing pedicure for tired feet.").append("icon", "👣"),
                new Document("name", "Facial Treatment").append("price", 1200.0).append("duration", 45).append("category", "Skincare").append("description", "Deep cleansing facial.").append("icon", "💆‍♀️"),
                new Document("name", "Hair Coloring").append("price", 2500.0).append("duration", 90).append("category", "Hair").append("description", "Full hair color or highlights.").append("icon", "🎨"),
                new Document("name", "Relaxing Massage").append("price", 2000.0).append("duration", 60).append("category", "Wellness").append("description", "Full body relaxation massage.").append("icon", "🧖‍♂️")
            );
            services.insertMany(defaultServices);
            System.out.println("Default services initialized with INR prices.");
        }
    }

    private void initializeQueue(MongoDatabase db) {
        MongoCollection<Document> queue = db.getCollection("queue_status");
        if (queue.countDocuments() == 0) {
            Document initialStatus = new Document("currentServingToken", "NONE")
                .append("totalWaiting", 0)
                .append("lastUpdated", new java.util.Date().toString());
            queue.insertOne(initialStatus);
            System.out.println("Queue status initialized.");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        MongoConnection.close();
    }
}
