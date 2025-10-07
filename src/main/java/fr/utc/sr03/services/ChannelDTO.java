package fr.utc.sr03.services;

// Cette classe permet de créer ou lire un JSON représentant un salon de discussion.
public class ChannelDTO {
    private String title;
    private String description;
    private String date;
    private String endOfValidity;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEndOfValidity() {
        return endOfValidity;
    }

    public void setEndOfValidity(String endOfValidity) {
        this.endOfValidity = endOfValidity;
    }
}
