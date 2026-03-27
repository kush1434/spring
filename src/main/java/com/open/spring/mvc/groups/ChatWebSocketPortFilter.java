package com.open.spring.mvc.groups;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ChatWebSocketPortFilter extends OncePerRequestFilter {

    @Value("${socket.port:8589}")
    private int socketPort;

    private static final String CHAT_ENDPOINT = "/ws-chat";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        String path = requestUri.startsWith(contextPath)
                ? requestUri.substring(contextPath.length())
                : requestUri;

        boolean isChatWebSocketPath = path.equals(CHAT_ENDPOINT) || path.startsWith(CHAT_ENDPOINT + "/");
        boolean isOnSocketPort = request.getLocalPort() == socketPort;

        // Port 8589 is dedicated to the chat websocket handshake/SockJS routes.
        if (isOnSocketPort && !isChatWebSocketPath) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Keep websocket chat off the main application port.
        if (!isOnSocketPort && isChatWebSocketPath) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
