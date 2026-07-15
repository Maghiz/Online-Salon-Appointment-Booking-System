package com.luxesalon.models;

import java.util.Date;
import java.util.List;

public class Appointment {
    private String id;
    private String userId;
    private String userName;
    private String tokenNumber;
    private List<String> serviceIds;
    private List<String> serviceNames;
    private double totalPrice;
    private int totalDuration;
    private String appointmentDate;
    private String appointmentTime;
    private String status; // upcoming, waiting, serving, completed, cancelled
    private int estimatedWaitTime;
    private Date createdAt;

    public Appointment() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }
    public List<String> getServiceIds() { return serviceIds; }
    public void setServiceIds(List<String> serviceIds) { this.serviceIds = serviceIds; }
    public List<String> getServiceNames() { return serviceNames; }
    public void setServiceNames(List<String> serviceNames) { this.serviceNames = serviceNames; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public int getTotalDuration() { return totalDuration; }
    public void setTotalDuration(int totalDuration) { this.totalDuration = totalDuration; }
    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }
    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getEstimatedWaitTime() { return estimatedWaitTime; }
    public void setEstimatedWaitTime(int estimatedWaitTime) { this.estimatedWaitTime = estimatedWaitTime; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
