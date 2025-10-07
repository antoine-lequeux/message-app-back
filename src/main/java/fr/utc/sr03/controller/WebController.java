package fr.utc.sr03.controller;

import fr.utc.sr03.model.User;
import fr.utc.sr03.services.ServicesRequest;
import fr.utc.sr03.services.UserDTO;
import jakarta.annotation.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Controller
public class WebController {

    @Resource
    private ServicesRequest servicesRequest;

    // Afficher le formulaire de connexion.
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    // Création d’un utilisateur depuis le formulaire d'inscription.
    @PostMapping("/home/users/create")
    public String createUser(
            @ModelAttribute UserDTO userDTO,
            RedirectAttributes redirectAttrs
    ) throws IOException {

        // Vérification de la validité du mot de passe.
        if (!servicesRequest.isPasswordValid(userDTO.getPassword())) {
            redirectAttrs.addFlashAttribute("error", "Le mot de passe ne respecte pas les règles.");
            return "redirect:/home";
        }

        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setMail(userDTO.getMail());
        user.setPassword(userDTO.getPassword());
        user.setAdmin(userDTO.isAdmin());

        // Utilise l'image de l'utilisateur si fournie, sinon image par défaut.
        if (userDTO.getAvatarBase64() != null && !userDTO.getAvatarBase64().isEmpty()) {
            user.setAvatar(userDTO.getAvatarBase64());
        } else {
            ClassPathResource imgFile = new ClassPathResource("static/img/avatar_base.jpg");
            if (!imgFile.exists()) {
                redirectAttrs.addFlashAttribute("error", "Fichier avatar par défaut introuvable !");
                return "redirect:/home";
            }
            try (InputStream inputStream = imgFile.getInputStream()) {
                byte[] imageBytes = inputStream.readAllBytes();
                user.setAvatar(Base64.getEncoder().encodeToString(imageBytes));
            }
        }

        // Si l'utilisateur ne peut pas être ajouté, c'est que l'email est déjà utilisé.
        if (!servicesRequest.addUser(user)) {
            redirectAttrs.addFlashAttribute("error", "Email déjà utilisé.");
            return "redirect:/home";
        }

        redirectAttrs.addFlashAttribute("message", "Utilisateur créé avec succès.");
        return "redirect:/home";
    }

    // Affiche le formulaire de modification d’un utilisateur.
    @GetMapping("/home/edit/{id}")
    public String editUserForm(@PathVariable("id") int id, Model model) {
        User user = servicesRequest.findUserById(id);
        model.addAttribute("user", user);
        return "edit";
    }

    // Traitement du formulaire de modification de l'utilisateur.
    @PostMapping("/home/edit/{id}/submit")
    public String editUserSubmit(
            @PathVariable("id") int id,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(defaultValue = "false") boolean admin,
            @RequestParam String avatarBase64,
            RedirectAttributes redirectAttributes
    ) throws IOException {

        // Vérification de la validité du nouveau mot de passe.
        if (!servicesRequest.isPasswordValid(password)) {
            redirectAttributes.addFlashAttribute("error", "Le mot de passe ne respecte pas toutes les règles.");
            return "redirect:/home";
        }

        // Récupère l'utilisateur existant pour pouvoir le modifier.
        User user = servicesRequest.findUserById(id);

        // On vérifie si l'email est déjà utilisé par un autre utilisateur (sans compter cet utilisateur
        // sinon il y aurait un problème quand l'email ne change pas).
        if (servicesRequest.emailExists(email) && !user.getMail().equals(email)) {
            redirectAttributes.addFlashAttribute("error", "Email déjà utilisé.");
            return "redirect:/home";
        }

        // Modification des champs de l'utilisateur.
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setMail(email);
        user.setPassword(password);
        user.setAdmin(admin);

        // Modification du champ 'avatar', avec une image par défaut si le champ est vide.
        if (avatarBase64 != null && !avatarBase64.isEmpty()) {
            user.setAvatar(avatarBase64);
        } else {
            ClassPathResource imgFile = new ClassPathResource("static/img/avatar_base.jpg");
            if (!imgFile.exists()) {
                redirectAttributes.addFlashAttribute("error", "Fichier avatar par défaut introuvable !");
                return "redirect:/home";
            }
            try (InputStream inputStream = imgFile.getInputStream()) {
                byte[] imageBytes = inputStream.readAllBytes();
                user.setAvatar(Base64.getEncoder().encodeToString(imageBytes));
            }
        }

        // Mise à jour de l'utilisateur dans la base de données.
        servicesRequest.updateUser(user);
        return "redirect:/home";
    }

    // Page d’accueil admin après la connexion.
    @GetMapping("/home")
    public String homePage(
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        // On récupère l'utilisateur qui tente de se connecter.
        String email = authentication.getName();
        User user = servicesRequest.findByEmail(email);

        // On vérifie si l'utilisateur est admin, et si ce n'est pas le cas ou revient sur /login.
        if (!user.isAdmin()) {
            redirectAttributes.addAttribute("accessDenied", true);
            return "redirect:/login";
        }

        // On récupère les utilisateurs pour pouvoir les afficher sur la page.
        model.addAttribute("users", servicesRequest.getUsers());

        return "home";
    }

    // Supprime un utilisateur et redirige vers la page d’accueil.
    @GetMapping("/home/delete/{id}")
    public String deleteUser(@PathVariable int id, Model model) {
        servicesRequest.deleteUserById(id);
        model.addAttribute("users", servicesRequest.getUsers());
        return "redirect:/home#usertable"; // rechargement de la section table
    }
}
