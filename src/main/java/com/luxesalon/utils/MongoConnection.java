package com.luxesalon.utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {
    // MongoDB typically defaults to localhost:27017
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "luxesaloon_db";
    
    private static MongoClient mongoClient = null;

    private MongoConnection() {}

    public static MongoDatabase getDatabase() {
        if (mongoClient == null) {
            try {
                mongoClient = MongoClients.create(CONNECTION_STRING);
                System.out.println("MongoDB connected successfully to base: " + DATABASE_NAME);
            } catch (Exception e) {
                System.err.println("Database connection failed: " + e.getMessage());
            }
        }
        return mongoClient.getDatabase(DATABASE_NAME);
    }

    public static void close() {
        if (mongoClient != null) {
            mongoClient. close();
            mongoClient = null;
        }
    }
}
