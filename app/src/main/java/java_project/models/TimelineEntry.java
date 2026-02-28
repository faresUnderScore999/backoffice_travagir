package java_project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimelineEntry {
    private String date;
    private int logins;

    public TimelineEntry() {}

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public int getLogins() { return logins; }
    public void setLogins(int logins) { this.logins = logins; }
}