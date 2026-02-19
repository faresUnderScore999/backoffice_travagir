package java_project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VoyageImage {
    private Integer id;
    private Integer voyageId;
    private String imageUrl;
    private String cloudinaryPublicId;
    private String createdAt;
    private String updatedAt;

    public VoyageImage() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getVoyageId() { return voyageId; }
    public void setVoyageId(Integer voyageId) { this.voyageId = voyageId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCloudinaryPublicId() { return cloudinaryPublicId; }
    public void setCloudinaryPublicId(String cloudinaryPublicId) { this.cloudinaryPublicId = cloudinaryPublicId; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
