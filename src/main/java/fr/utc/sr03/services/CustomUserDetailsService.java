package fr.utc.sr03.services;

import fr.utc.sr03.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

// Service utilisé par Spring Security pour charger un utilisateur depuis la base de données
// en se basant sur son email lors de l'authentification.
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // Permet de faire des requêtes JPA avec Hibernate.
    @PersistenceContext
    private EntityManager em;

    // Méthode appelée automatiquement par Spring Security lors d'une tentative de connexion.
    // Elle récupère un utilisateur par son email et construit un objet UserDetails.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Requête pour récupérer l'utilisateur par son mail.
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.mail = :mail", User.class);
        query.setParameter("mail", email);

        List<User> users = query.getResultList();

        // Si aucun utilisateur trouvé, une exception est levée.
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("Aucun utilisateur avec cet email.");
        }

        // On prend le premier (et normalement seul) utilisateur trouvé.
        User user = users.get(0);

        // Construction d'un objet UserDetails à partir de l'utilisateur.
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getMail())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}
