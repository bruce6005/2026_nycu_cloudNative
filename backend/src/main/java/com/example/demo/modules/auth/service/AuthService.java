package com.example.demo.modules.auth.service;

import com.example.demo.modules.user.model.User;
import com.example.demo.modules.user.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AuthService {

    @Value("${google.client.id:YOUR_CLIENT_ID_HERE}")
    private String clientId;

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User verifyGoogleToken(String idTokenString) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                // Specify the CLIENT_ID of the app that accesses the backend:
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            String userId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            return userRepository.findByGoogleId(userId)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setGoogleId(userId);
                        newUser.setEmail(email);
                        newUser.setName(name);
                        newUser.setAvatarUrl(pictureUrl);
                        return userRepository.save(newUser);
                    });
        } else {
            throw new Exception("Invalid ID token.");
        }
    }
}
