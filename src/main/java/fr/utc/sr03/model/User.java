package fr.utc.sr03.model;

import jakarta.persistence.*;

// Cette classe représente un utilisateur de l'application et fait le lien avec la table Users de la BDD.
@Entity
@Table(name="Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="userID")
    private Integer usersID;

    @Column(name="firstName")
    private String firstName;

    @Column(name="lastName")
    private String lastName;

    @Column(name="mail")
    private String mail;

    @Column(name="password")
    private String password;

    @Column(name="admin")
    private boolean admin;

    @Column(name="avatar")
    private String avatar;

    // Getters & Setters :

    public int getUsersID() {
        return usersID;
    }

    public void setUsersID(int usersId) {
        this.usersID = usersId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstname) {
        this.firstName = firstname;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastname) {
        this.lastName = lastname;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public boolean isAdmin() { return admin; }

    public void setAdmin(boolean admin) { this.admin = admin; }

    public String getAvatar() { return avatar; }

    public void setAvatar(String avatar) { this.avatar = avatar; }

}
