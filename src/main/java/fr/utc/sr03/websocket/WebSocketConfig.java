package fr.utc.sr03.websocket;

import fr.utc.sr03.services.ServicesRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    // Objet ServiceRequest destiné à être injecté dans le DynamicWebSocketHandler.
    @Autowired
    private ServicesRequest servicesRequest;

    // Méthode pour enregistrer les handlers WebSocket dynamiquement.
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new DynamicWebSocketHandler(servicesRequest), "/message/{channelId}")
                .setAllowedOrigins("*");
    }
}
