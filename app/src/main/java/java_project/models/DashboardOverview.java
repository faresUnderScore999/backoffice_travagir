package java_project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardOverview {
    private int totalOffers;
    private int totalUsers;
    private int totalVoyages;
    private int totalReservations;
    private int todayReservations;
    private double totalRevenue;
    private int todayLogins;
    private int activeUsersLastWeek;

    public DashboardOverview() {}

    // getters and setters
    public int getTotalOffers() { return totalOffers; }
    public void setTotalOffers(int totalOffers) { this.totalOffers = totalOffers; }
    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
    public int getTotalVoyages() { return totalVoyages; }
    public void setTotalVoyages(int totalVoyages) { this.totalVoyages = totalVoyages; }
    public int getTotalReservations() { return totalReservations; }
    public void setTotalReservations(int totalReservations) { this.totalReservations = totalReservations; }
    public int getTodayReservations() { return todayReservations; }
    public void setTodayReservations(int todayReservations) { this.todayReservations = todayReservations; }
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    public int getTodayLogins() { return todayLogins; }
    public void setTodayLogins(int todayLogins) { this.todayLogins = todayLogins; }
    public int getActiveUsersLastWeek() { return activeUsersLastWeek; }
    public void setActiveUsersLastWeek(int activeUsersLastWeek) { this.activeUsersLastWeek = activeUsersLastWeek; }
}