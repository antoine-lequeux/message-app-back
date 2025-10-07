package fr.utc.sr03.services;

// Cette classe permet de créer ou lire un JSON représentant un ensemble mail / mot de passe.
public class LoginDTO {
    private String mail;
    private String password;

    public String getMail() {
        return mail;
    }
    public void setMail(String mail) {
        this.mail = mail;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
