package fr.utc.sr03.services;

import java.util.Calendar;

// Cette classe permet de créer ou lire un JSON représentant l'appartenance d'un utilisateur à un salon de discussion.
public class MemberDTO {
    private int userID;
    private int channelID;
    private boolean creator;
    private Calendar joinDate;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getChannelID() {
        return channelID;
    }

    public void setChannelID(int channelID) {
        this.channelID = channelID;
    }

    public boolean isCreator() {
        return creator;
    }

    public void setCreator(boolean creator) {
        this.creator = creator;
    }

    public Calendar getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Calendar joinDate) {
        this.joinDate = joinDate;
    }
}
