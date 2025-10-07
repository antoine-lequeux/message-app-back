package fr.utc.sr03.controller;

import fr.utc.sr03.model.Channel;
import fr.utc.sr03.model.Member;
import fr.utc.sr03.model.User;
import fr.utc.sr03.services.*;
import jakarta.annotation.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;

// La classe APIController a pour but de permettre au front React d'interagir avec la base de données.
@RestController
public class ApiController {

    @Resource
    private ServicesRequest servicesRequest;

    // Vérifie les identifiants de connexion d'un utilisateur.
    @PostMapping("/api/users/login")
    public UserDTO verifyUser(@RequestBody LoginDTO login) {
        return servicesRequest.verifyUser(login.getMail(), login.getPassword());
    }

    // Crée un nouvel utilisateur après vérification des données.
    // Renvoie l'utilisateur créé en cas de succès, et une erreur sinon.
    @PostMapping("/api/users/self-signup")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) throws IOException {

        // Vérification du mot de passe.
        if (!servicesRequest.isPasswordValid(userDTO.getPassword())) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Le mot de passe ne respecte pas les règles."));
        }

        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setMail(userDTO.getMail());
        user.setPassword(userDTO.getPassword());
        user.setAdmin(userDTO.isAdmin());

        // Ajout d'un avatar par défaut si non fourni.
        if (userDTO.getAvatarBase64() != null && !userDTO.getAvatarBase64().isEmpty()) {
            user.setAvatar(userDTO.getAvatarBase64());
        } else {
            ClassPathResource imgFile = new ClassPathResource("static/img/avatar_base.jpg");
            if (!imgFile.exists()) {
                return ResponseEntity
                        .status(500)
                        .body(new ErrorResponse("Fichier avatar par défaut introuvable."));
            }
            try (InputStream inputStream = imgFile.getInputStream()) {
                byte[] imageBytes = inputStream.readAllBytes();
                user.setAvatar(Base64.getEncoder().encodeToString(imageBytes));
            }
        }

        // Si l'utilisateur ne peut pas être ajouté, c'est que l'email est déjà utilisé.
        if (!servicesRequest.addUser(user)) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Email déjà utilisé."));
        }

        User createdUser = servicesRequest.findByEmail(user.getMail());

        UserDTO responseDTO = new UserDTO(
                createdUser.getUsersID(),
                createdUser.getFirstName(),
                createdUser.getLastName(),
                createdUser.getMail(),
                "",
                createdUser.isAdmin(),
                createdUser.getAvatar()
        );

        return ResponseEntity.ok(responseDTO);
    }

    // Récupère un canal par son ID.
    @GetMapping("/api/users/{userId}")
    public User getUser(@PathVariable int userId) {
        return servicesRequest.findUserById(userId);
    }

    // Recherche des utilisateurs qui contiennent une certaine chaîne de caractères dans leur nom complet.
    @GetMapping("/api/users/search")
    public List<UserDTO> searchUsers(@RequestParam String query) {
        return servicesRequest.searchUsers(query);
    }

    // Liste des canaux auxquels l'utilisateur appartient.
    @GetMapping("/api/users/channels/membership")
    public List<Channel> getUsersMemberships(@RequestParam("userId") int userId){
        return servicesRequest.getUsersMemberships(userId);
    }

    // Liste des canaux créés par l'utilisateur.
    @GetMapping("/api/users/channels/ownership")
    public List<Channel> getUsersOwnerships(@RequestParam("userId") int userId) {
        return servicesRequest.getUsersOwnerships(userId);
    }

    // Crée un nouveau canal et ajoute l'utilisateur en tant que créateur.
    @PostMapping("/api/channels")
    public void createChannel(@RequestBody ChannelDTO channelDTO, @RequestParam("idUser") int idUser) throws ParseException {
        Channel ch = new Channel();
        ch.setTitle(channelDTO.getTitle());
        ch.setDescription(channelDTO.getDescription());
        ch.setDate(channelDTO.getDate());
        ch.setEndOfValidity(channelDTO.getEndOfValidity());
        servicesRequest.addChannel(ch);

        // L'utilisateur qui crée le canal est immédiatement ajouté aux membres en tant que créateur.
        Member member = new Member();
        member.setCreator(true);
        member.setUserID(idUser);
        member.setJoinDate(Calendar.getInstance());
        member.setChannelID(ch.getChannelId());

        servicesRequest.addMember(member);
    }

    // Récupère tous les canaux existants.
    @GetMapping("/api/channels")
    public List<Channel> getChannels() {
        return servicesRequest.getChannels();
    }

    // Récupère un canal par son ID.
    @GetMapping("/api/channels/{channelId}")
    public Channel getChannel(@PathVariable int channelId) {
        return servicesRequest.getChannelById(channelId);
    }

    // Vérifie si un utilisateur est le créateur d’un canal.
    @GetMapping("/api/channels/is-creator")
    @ResponseBody
    public boolean isCreatorOfChannel(@RequestParam int channelID, @RequestParam int userID) {
        return servicesRequest.isCreatorOfChannel(userID, channelID);
    }

    // Ajoute une liste de membres à un canal.
    @PostMapping("/api/channels/members")
    public void addMembers(@RequestBody List<MemberDTO> members) {
        for (MemberDTO memberDTO : members) {
            Member member = new Member();
            member.setUserID(memberDTO.getUserID());
            member.setChannelID(memberDTO.getChannelID());
            member.setJoinDate(Calendar.getInstance());
            member.setCreator(memberDTO.isCreator());
            servicesRequest.addMember(member);
        }
    }

    // Liste les membres d’un canal donné.
    @GetMapping("/api/channels/members")
    public List<User> getMembers(@RequestParam("idChannel") int idChannel) {
        return servicesRequest.getChannelsMembers(idChannel);
    }

    // Supprime un membre d’un canal.
    @DeleteMapping("/api/channels/members")
    public ResponseEntity<?> removeMember(@RequestBody Map<String, Object> body) {

        // Récupère les champs du json envoyé.
        Object userIdObj = body.get("userID");
        Object channelIdObj = body.get("channelID");

        // Renvoie une erreur si un champ est vide.
        if (userIdObj == null || channelIdObj == null) {
            return ResponseEntity.badRequest().body("userID et channelID requis");
        }

        // Parsing des paramètres.
        int userId = (userIdObj instanceof Number)
                ? ((Number) userIdObj).intValue()
                : Integer.parseInt(userIdObj.toString());

        int channelId = (channelIdObj instanceof Number)
                ? ((Number) channelIdObj).intValue()
                : Integer.parseInt(channelIdObj.toString());

        // Appel de la requête de suppression du membre.
        servicesRequest.removeMemberFromChannel(channelId, userId);
        return ResponseEntity.ok().build();
    }

    // Supprime un canal par son ID.
    @DeleteMapping("/api/channels")
    public ResponseEntity<?> deleteChannel(@RequestBody Map<String, Object> body) {

        // Récupère le champ du json envoyé.
        Object channelIdObj = body.get("channelID");

        // Le champ doit être rempli.
        if (channelIdObj == null) {
            return ResponseEntity.badRequest().body("channelID requis");
        }

        // Parsing du paramètre.
        int channelId = (channelIdObj instanceof Number)
                ? ((Number) channelIdObj).intValue()
                : Integer.parseInt(channelIdObj.toString());

        // Appel de la requête de suppression du canal.
        servicesRequest.deleteChannelById(channelId);
        return ResponseEntity.ok().build();
    }
}
