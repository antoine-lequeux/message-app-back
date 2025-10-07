package fr.utc.sr03.websocket;

// Représente un message échangé via WebSocket.
public class MessageSocket {

    private Integer userID;   // ID de l'utilisateur émetteur.
    private String message;   // Contenu du message.

    // Getters & Setters

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
