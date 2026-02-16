package java_project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private int id;
    private String name;
    private String email;
    private String imageUrl;
    private String tel;

    // Default constructor for Jackson
    public User() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) {
    if (imageUrl == null || imageUrl.isBlank()) {
        // Fallback to a placeholder if the value is null or empty
        this.imageUrl = "https://i0.wp.com/newspack-washingtoncitypaper.s3.amazonaws.com/uploads/2009/04/contexts.org_socimages_files_2009_04_d_silhouette.png?fit=1920%2C1210&ssl=1";
    } else if (!imageUrl.startsWith("http") && !imageUrl.startsWith("file:")) {
        // Handle cases where the path might be a local string without a protocol
        this.imageUrl = "https://i0.wp.com/newspack-washingtoncitypaper.s3.amazonaws.com/uploads/2009/04/contexts.org_socimages_files_2009_04_d_silhouette.png?fit=1920%2C1210&ssl=1";
    } else {
        this.imageUrl = imageUrl.trim();
    }
}
    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }
}