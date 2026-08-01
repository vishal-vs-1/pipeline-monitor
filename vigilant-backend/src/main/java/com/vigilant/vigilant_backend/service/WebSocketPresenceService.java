package com.vigilant.vigilant_backend.service;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketPresenceService {

    // Maps User Email -> Set of Session IDs (in case user has multiple tabs open)
    private final ConcurrentHashMap<String, Set<String>> activeUsers = new ConcurrentHashMap<>();

    // Tracks users who just connected and need a historical backfill on their first poll cycle
    private final Set<String> newlyConnectedUsers = ConcurrentHashMap.newKeySet();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headers.getUser();
        if (user != null && user.getName() != null) {
            String email = user.getName();
            String sessionId = headers.getSessionId();
            
            boolean isFirstSession = !activeUsers.containsKey(email);
            activeUsers.computeIfAbsent(email, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
            if (isFirstSession) {
                newlyConnectedUsers.add(email);
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = headers.getUser();
        if (user != null && user.getName() != null) {
            String email = user.getName();
            String sessionId = headers.getSessionId();
            
            Set<String> userSessions = activeUsers.get(email);
            if (userSessions != null) {
                userSessions.remove(sessionId);
                if (userSessions.isEmpty()) {
                    activeUsers.remove(email);
                }
            }
        }
    }

    public Set<String> getOnlineUserEmails() {
        return Collections.unmodifiableSet(activeUsers.keySet());
    }

    /**
     * Returns and clears the set of users who just came online.
     * BuildPoller calls this to determine who needs a backfill fetch.
     */
    public Set<String> consumeNewlyConnectedUsers() {
        Set<String> snapshot = Set.copyOf(newlyConnectedUsers);
        newlyConnectedUsers.clear();
        return snapshot;
    }
}
