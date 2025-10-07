package fr.utc.sr03.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import fr.utc.sr03.model.Channel;
import fr.utc.sr03.model.Member;
import fr.utc.sr03.model.User;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

// Cette classe permet d'effectuer des opérations dans la BDD.
@Repository
@Transactional
public class ServicesRequest {

    @PersistenceContext
    private EntityManager em;

    // ------------------- AUTHENTIFICATION & UTILISATEURS -------------------

    // Vérifie un utilisateur via son mail et son mot de passe (hash comparé via BCrypt).
    // Retourne 'null' si les identifiants fournis ne correspondent à aucun utilisateur.
    public UserDTO verifyUser(String mail, String password) {
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.mail = :mail", User.class);
            query.setParameter("mail", mail);

            User usr = query.getSingleResult();

            boolean validPassword = BCrypt.verifyer()
                    .verify(password.toCharArray(), usr.getPassword())
                    .verified;

            // Si le mot de passe fourni correspond au vrai mot de passe, on crée un UserDTO pour le retourner au front.
            if (validPassword) {
                return new UserDTO(
                        usr.getUsersID(), usr.getFirstName(), usr.getLastName(),
                        usr.getMail(), "", usr.isAdmin(), usr.getAvatar());
            }
            return null;

        } catch (Exception e) {
            return null;
        }
    }

    // Recherche les utilisateurs dont le nom, prénom ou mail contient le texte fourni.
    public List<UserDTO> searchUsers(String query) {
        String searchPattern = "%" + query.toLowerCase() + "%";

        List<User> users = em.createQuery(
                        "SELECT u FROM User u WHERE LOWER(u.mail) LIKE :search " +
                                "OR LOWER(u.firstName) LIKE :search " +
                                "OR LOWER(u.lastName) LIKE :search", User.class)
                .setParameter("search", searchPattern)
                .getResultList();

        // Crée et renvoie une liste de UserDTO dont le nom ou le mail contient le texte fourni.
        return users.stream()
                .map(u -> new UserDTO(
                        u.getUsersID(), u.getFirstName(), u.getLastName(),
                        u.getMail(), "", u.isAdmin(), u.getAvatar()))
                .collect(Collectors.toList());
    }

    // Trouve un utilisateur par son email.
    public User findByEmail(String email) {
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.mail = :mail", User.class);
        query.setParameter("mail", email);
        return query.getSingleResult();
    }

    // Trouve un utilisateur par son ID.
    public User findUserById(int id) {
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.usersID = :id", User.class);
        query.setParameter("id", id);
        return query.getSingleResult();
    }

    // Vérifie si un email existe déjà dans la BDD.
    public boolean emailExists(String email) {
        try {
            findByEmail(email);
            return true;
        } catch (NoResultException e) {
            return false;
        }
    }

    // Vérifie si un mot de passe respecte les règles de sécurité.
    public boolean isPasswordValid(String password) {
        if (password.length() < 15) return false;

        int uppercaseCount = password.replaceAll("[^A-Z]", "").length();
        int digitCount = password.replaceAll("[^0-9]", "").length();
        int specialCount = password.replaceAll("[^!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]", "").length();

        return uppercaseCount >= 1 && digitCount >= 3 && specialCount >= 2;
    }

    // Hash un mot de passe avec BCrypt.
    public String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    // Ajoute un utilisateur en hashant son mot de passe, s’il n'existe pas déjà.
    public boolean addUser(User user) {
        // Si le mail n'est pas disponible, retourne false.
        if (emailExists(user.getMail())) return false;
        String password = user.getPassword();
        user.setPassword(hashPassword(password));
        em.persist(user);
        return true;
    }

    // Met à jour un utilisateur et rehash son mot de passe.
    public void updateUser(User user) {
        String password = user.getPassword();
        user.setPassword(hashPassword(password));
        em.merge(user);
    }

    // Supprime un utilisateur via sa clé primaire.
    public void deleteUserById(int id) {
        Query q = em.createQuery(
                "SELECT DISTINCT m.channelID FROM Member m WHERE m.userID = :userId");

        q.setParameter("userId", id);

        List<Integer> channelsIDs = q.getResultList();

        for (Integer channelId : channelsIDs) {
            removeMemberFromChannel(channelId, id);
        }


        User user = em.find(User.class, id);
        if (user != null) {
            em.remove(user);
        }
    }

    // Retourne tous les utilisateurs de la BDD.
    public List<User> getUsers() {
        Query q = em.createQuery("SELECT u FROM User u");
        return q.getResultList();
    }

    // ------------------- CHANNELS -------------------

    // Retourne tous les channels dont l’utilisateur est membre.
    public List<Channel> getUsersMemberships(int userId) {
        Calendar now = Calendar.getInstance();

        Query q = em.createQuery(
                "SELECT c FROM Channel c JOIN Member m ON c.channelId = m.channelID WHERE m.userID = :userId and m.creator = False and c.date < :now");

        q.setParameter("userId", userId);
        q.setParameter("now", now);

        List<Channel> results = q.getResultList();

        Iterator<Channel> iterator = results.iterator();
        while (iterator.hasNext()) {
            Channel c = iterator.next();
            if (c.getEndOfValidity().before(now)) {
                deleteChannelById(c.getChannelId());
                iterator.remove();
            }
        }

        return results;
    }

    // Retourne tous les channels dont l’utilisateur est propriétaire.
    public List<Channel> getUsersOwnerships(int userId) {
        Calendar now = Calendar.getInstance();

        Query q = em.createQuery(
                "SELECT c FROM Channel c JOIN Member m ON c.channelId = m.channelID WHERE m.userID = :userId and m.creator = True");

        q.setParameter("userId", userId);

        List<Channel> results = q.getResultList();

        Iterator<Channel> iterator = results.iterator();
        while (iterator.hasNext()) {
            Channel c = iterator.next();
            if (c.getEndOfValidity().before(now)) {
                deleteChannelById(c.getChannelId());
                iterator.remove();
            }
        }

        return results;
    }

    // Ajoute un canal.
    public void addChannel(Channel ch) {
        em.persist(ch);
    }

    // Récupère un canal par son ID.
    public Channel getChannelById(int id) {
        return em.find(Channel.class, id);
    }

    // Supprime un canal et ses membres associés.
    public void deleteChannelById(int id) {

        // On supprime les membres du canal.
        Query deleteMembers = em.createQuery("DELETE FROM Member m WHERE m.channelID = :channelId");
        deleteMembers.setParameter("channelId", id);
        deleteMembers.executeUpdate();

        // Ensuite, on supprime le canal.
        Channel channel = em.find(Channel.class, id);
        if (channel != null) {
            em.remove(channel);
        }
    }

    // Récupère tous les channels.
    public List<Channel> getChannels() {
        Query q = em.createQuery("SELECT c FROM Channel c");
        return q.getResultList();
    }

    // Récupère tous les membres (utilisateurs) d’un canal donné
    public List<User> getChannelsMembers(int channelId) {
        Query q = em.createQuery(
                "SELECT u FROM User u JOIN Member m ON m.userID = u.usersID WHERE m.channelID = :channelId");
        q.setParameter("channelId", channelId);
        return q.getResultList();
    }

    // ------------------- MEMBERS -------------------

    // Vérifie si un utilisateur est créateur d’un canal.
    public boolean isCreatorOfChannel(int userId, int channelId) {
        try {
            TypedQuery<Member> query = em.createQuery(
                    "SELECT m FROM Member m WHERE m.userID = :userId AND m.channelID = :channelId",
                    Member.class);
            query.setParameter("userId", userId);
            query.setParameter("channelId", channelId);

            Member member = query.getSingleResult();
            return member.isCreator();
        } catch (NoResultException e) {
            return false;
        }
    }

    // Ajoute un membre à un canal.
    public void addMember(Member member) {
        em.persist(member);
    }

    // Supprime un membre à partir de son userId et du canal.
    public void removeMemberFromChannel(int channelId, int userId) {
        try {
            TypedQuery<Member> query1 = em.createQuery(
                    "SELECT m FROM Member m WHERE m.channelID = :channelId AND m.userID = :userId",
                    Member.class);
            query1.setParameter("channelId", channelId);
            query1.setParameter("userId", userId);

            Member member = query1.getSingleResult();

            if (member.isCreator()) {
                TypedQuery<Member> query2 = em.createQuery(
                        "SELECT m FROM Member m WHERE m.channelID = :channelId and m.userID <> :userId",
                        Member.class);
                query2.setParameter("channelId", channelId);
                query2.setParameter("userId", userId);

                List<Member> members = query2.getResultList();
                // Changement de createur et suppression du membre.
                if (!members.isEmpty()) {
                    members.get(0).setCreator(true);
                    em.merge(members.get(0));
                    em.remove(member);
                }else{
                    deleteChannelById(channelId);
                }
            } else {
                // Si le membre n'est pas créateur, on le supprime sans vérification.
                em.remove(member);
            }
        } catch (NoResultException e) {
            System.out.println("Aucun membre trouvé pour suppression (channelId: " + channelId + ", userId: " + userId + ")");
        }
    }

    // Vérifie qu'un utilisateur est bien dans un canal.
    public boolean isUserInChannel(int userId, int channelId) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(m) FROM Member m WHERE m.userID = :userId AND m.channelID = :channelId",
                Long.class
        );
        query.setParameter("userId", userId);
        query.setParameter("channelId", channelId);

        Long count = query.getSingleResult();

        return count != null && count > 0;
    }
}
