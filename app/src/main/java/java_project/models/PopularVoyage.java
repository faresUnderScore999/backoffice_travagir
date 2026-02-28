package java_project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PopularVoyage {
    private int visitCount;
    private String destination;
    private int voyageId;
    private String title;

    public PopularVoyage() {}

    public int getVisitCount() { return visitCount; }
    public void setVisitCount(int visitCount) { this.visitCount = visitCount; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public int getVoyageId() { return voyageId; }
    public void setVoyageId(int voyageId) { this.voyageId = voyageId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}