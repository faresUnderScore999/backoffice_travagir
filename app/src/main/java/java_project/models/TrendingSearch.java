package java_project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TrendingSearch {
    private String query;
    private int count;

    public TrendingSearch() {}

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}