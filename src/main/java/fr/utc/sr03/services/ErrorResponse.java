package fr.utc.sr03.services;

// Cette classe permet d'envoyer une erreur au front.
public class ErrorResponse {
    private String message;

    public ErrorResponse() {}
    public ErrorResponse(String message) { this.message = message; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
