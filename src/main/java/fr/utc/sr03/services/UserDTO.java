package fr.utc.sr03.services;

// Cette classe permet de créer ou lire un JSON représentant un utilisateur de l'application.
public class UserDTO {

    private Integer usersID;
    private String firstName;
    private String lastName;
    private String mail;
    private String password;
    private boolean admin;
    private String avatarBase64;

    public UserDTO(Integer usersID,String firstName, String lastName, String mail, String password, boolean admin, String avatarBase64) {
        this.usersID = usersID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mail = mail;
        this.password = password;
        this.admin = admin;
        this.avatarBase64 = avatarBase64;
    }

    public int getUsersID() {
        return usersID;
    }

    public void setUsersID(int usersID) {
        this.usersID = usersID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

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

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getAvatarBase64() {
        return avatarBase64;
    }

    public void setAvatarBase64(String avatarBase64) {
        this.avatarBase64 = avatarBase64;
    }
}
