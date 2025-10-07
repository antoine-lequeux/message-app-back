package fr.utc.sr03.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.utc.sr03.services.ServicesRequest;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Handler WebSocket dynamique pour la gestion des salons de discussion (channels).
// Permet de gérer des connexions WebSocket sur des salons créés dynamiquement.
public class DynamicWebSocketHandler extends TextWebSocketHandler {

    // Service d'accès à la base de données (membres, canaux, etc.).
    private final ServicesRequest servicesRequest;

    // Map des sessions actives, classées par ID de canal.
    private final Map<Integer, List<WebSocketSession>> sessionsByChannel = new ConcurrentHashMap<>();

    // Utilisé pour sérialiser/désérialiser les messages JSON.
    private final ObjectMapper mapper = new ObjectMapper();

    // Constructeur avec injection du service.
    public DynamicWebSocketHandler(ServicesRequest servicesRequest) {
        this.servicesRequest = servicesRequest;
    }

    // Lorsqu'une nouvelle connexion WebSocket est établie.
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Integer channelId = extractChannelId(session.getUri());
        Integer userId = extractUserId(session.getUri());

        // Si les infos sont manquantes, on ferme la session.
        if (channelId == null || userId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        // On stocke les infos dans les attributs de la session.
        session.getAttributes().put("channelId", channelId);
        session.getAttributes().put("userId", userId);

        // On ajoute la session à la liste des sessions du canal.
        sessionsByChannel
                .computeIfAbsent(channelId, id -> Collections.synchronizedList(new ArrayList<>()))
                .add(session);
    }

    // Lorsqu'un message est reçu depuis une session WebSocket.
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Integer channelId = extractChannelId(session.getUri());
        if (channelId == null) return;

        // On parse le message en objet Java
        MessageSocket msg = mapper.readValue(message.getPayload(), MessageSocket.class);

        // On le renvoie aux autres utilisateurs du même canal
        String json = mapper.writeValueAsString(msg);
        broadcastToChannel(channelId, json);
    }

    // Lorsqu'une session se ferme.
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Integer channelId = extractChannelId(session.getUri());
        if (channelId != null) {
            List<WebSocketSession> sessions = sessionsByChannel.get(channelId);
            if (sessions != null) {
                sessions.remove(session);
            }
        }
    }

    // Diffuse un message à toutes les sessions d'un canal, uniquement aux membres valides.
    private void broadcastToChannel(int channelId, String jsonMessage) throws IOException {
        List<WebSocketSession> sessions = sessionsByChannel.get(channelId);
        if (sessions != null) {
            List<WebSocketSession> toRemove = new ArrayList<>();

            for (WebSocketSession s : sessions) {
                if (!s.isOpen()) {
                    toRemove.add(s);
                    continue;
                }

                Integer sessionUserId = (Integer) s.getAttributes().get("userId");
                Integer sessionChannelId = (Integer) s.getAttributes().get("channelId");

                // Vérifie que la session est bien liée au bon canal.
                if (sessionUserId == null || sessionChannelId == null || !sessionChannelId.equals(channelId)) {
                    toRemove.add(s);
                    continue;
                }

                // Vérifie si l'utilisateur est encore membre du canal.
                boolean stillMember = servicesRequest.isUserInChannel(sessionUserId, channelId);
                if (stillMember) {
                    s.sendMessage(new TextMessage(jsonMessage));
                } else {
                    s.close(CloseStatus.NORMAL);
                    toRemove.add(s);
                }
            }

            // On nettoie les sessions invalides.
            sessions.removeAll(toRemove);
        }
    }

    // Extrait le channelId depuis l'URI WebSocket.
    private Integer extractChannelId(URI uri) {
        if (uri == null) return null;

        try {
            String path = uri.getPath();
            String[] segments = path.split("/");
            return Integer.parseInt(segments[segments.length - 1]);
        } catch (Exception e) {
            System.err.println("Impossible d'extraire le channelId depuis l'URL: " + uri);
            return null;
        }
    }

    // Extrait le userId depuis les paramètres de requête WebSocket.
    private Integer extractUserId(URI uri) {
        if (uri == null || uri.getQuery() == null) return null;

        try {
            String[] params = uri.getQuery().split("&");
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair.length == 2 && pair[0].equals("userId")) {
                    return Integer.parseInt(pair[1]);
                }
            }
        } catch (Exception e) {
            System.err.println("Impossible d'extraire le userId depuis l'URL: " + uri);
        }

        return null;
    }
}
