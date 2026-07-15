package com.luxesalon.models;

public class QueueState {
    private String id;
    private String currentServingToken;
    private int totalWaiting;
    private String lastUpdated;

    public QueueState() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCurrentServingToken() { return currentServingToken; }
    public void setCurrentServingToken(String currentServingToken) { this.currentServingToken = currentServingToken; }
    public int getTotalWaiting() { return totalWaiting; }
    public void setTotalWaiting(int totalWaiting) { this.totalWaiting = totalWaiting; }
    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
}
