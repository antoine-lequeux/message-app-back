package fr.utc.sr03.model;

import jakarta.persistence.*;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

// Cette classe représente l'appartenance d'un utilisateur à un salon et fait le lien avec la table Members de la BDD.
@Entity
@Table(name = "Members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membershipID")
    private Integer membershipID;

    @Column(name = "userID")
    private Integer userID;

    @Column(name = "channelID")
    private Integer channelID;

    @Column(name = "creator")
    private boolean creator;

    @Column(name = "joinDate")
    private Calendar joinDate;

    // Getters & Setters :

    public int getMembershipID() {
        return membershipID;
    }

    public void setMembershipID(Integer membershipID) {
        this.membershipID = membershipID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public int getChannelID() {
        return channelID;
    }

    public void setChannelID(Integer channelID) {
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

    // Définit la date d'adhésion à partir d'une chaîne formatée "yyyy-MM-dd'T'HH:mm".
    public void setJoinDate(String strDate) throws ParseException {
        this.joinDate = parseCalendarFromString(strDate);
    }

    // Méthodes privées :

    // Convertit une chaîne ISO en objet Calendar
    private Calendar parseCalendarFromString(String strDate) throws ParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(strDate, formatter);
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }
}
