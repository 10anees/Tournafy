package com.example.tournafy.domain.models.search;

/**
 * Model class representing a search result.
 * Can represent a Match, Tournament, or Series.
 */
public class SearchResult {
    
    public static final int TYPE_MATCH = 0;
    public static final int TYPE_TOURNAMENT = 1;
    public static final int TYPE_SERIES = 2;
    
    private String id;
    private int type;
    private String title;
    private String subtitle;
    private String info;
    private String status;
    private String code;
    private String sportId;
    
    public SearchResult() {
    }
    
    public SearchResult(String id, int type, String title, String subtitle) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.subtitle = subtitle;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getSubtitle() {
        return subtitle;
    }
    
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    
    public String getInfo() {
        return info;
    }
    
    public void setInfo(String info) {
        this.info = info;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getSportId() {
        return sportId;
    }
    
    public void setSportId(String sportId) {
        this.sportId = sportId;
    }
    
    /**
     * Get type as string for display
     */
    public String getTypeString() {
        switch (type) {
            case TYPE_MATCH:
                return "Match";
            case TYPE_TOURNAMENT:
                return "Tournament";
            case TYPE_SERIES:
                return "Series";
            default:
                return "Unknown";
        }
    }
}
