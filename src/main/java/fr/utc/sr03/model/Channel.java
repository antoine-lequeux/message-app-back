package fr.utc.sr03.model;

import jakarta.persistence.*;
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

// Cette classe représente un salon de discussion et fait le lien avec la table Channels de la BDD.
@Entity
@Table(name = "Channels")
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channelId")
    private Integer channelId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "date")
    private Calendar date;

    @Column(name = "endOfValidity")
    private Calendar endOfValidity;

    // Getters & Setters :

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

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

    public Calendar getDate() {
        return date;
    }

    // Définit la date de création du canal à partir d'un ensemble d'entiers.
    public void setDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, hour, minute, second);
        this.date = cal;
    }

    // Définit la date de création du canal à partir d'une chaîne ISO "yyyy-MM-dd'T'HH:mm".
    public void setDate(String strDate) throws ParseException {
        this.date = parseCalendarFromString(strDate);
    }

    public Calendar getEndOfValidity() {
        return endOfValidity;
    }

    // Définit la date de fin de validité du canal à partir d'une chaîne, en s'assurant qu'elle est postérieure à la date de création.
    public void setEndOfValidity(String strDate) throws ParseException {
        Calendar tmp = parseCalendarFromString(strDate);

        if (date != null && tmp.before(date)) {
            throw new InvalidParameterException("Invalid date, endOfValidity before channel's date.");
        }

        this.endOfValidity = tmp;
    }

    // Méthodes privées :

    // Convertit une chaîne ISO-8601 "yyyy-MM-dd'T'HH:mm" en Calendar.
    private Calendar parseCalendarFromString(String strDate) throws ParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(strDate, formatter);
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }
}
