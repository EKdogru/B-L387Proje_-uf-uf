package com.cufcuf.backend.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cufcuf.backend.model.Session;
import com.cufcuf.backend.model.User;
import com.cufcuf.backend.repository.SessionRepository;
import com.cufcuf.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    public UserController(UserRepository userRepository, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        try {
            if (userRepository.existsByEmail(user.getEmail())) {
                return ResponseEntity.badRequest().body("Bu e-posta adresi zaten kullanımda!");
            }

            if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
                return ResponseEntity.badRequest().body("Şifre boş olamaz!");
            }

            userRepository.save(user);
            return ResponseEntity.ok("Kullanıcı başarıyla oluşturuldu.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Hata: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        try {
            System.out.println("=== LOGIN İSTEĞİ ===");
            System.out.println("Email: " + loginRequest.getEmail());
            
            User dbUser = userRepository.findByEmail(loginRequest.getEmail());
            
            if (dbUser == null) {
                System.out.println("Kullanıcı bulunamadı!");
                return ResponseEntity.status(401).body("E-posta veya şifre hatalı!");
            }

            if (!dbUser.getPasswordHash().equals(loginRequest.getPasswordHash())) {
                System.out.println("Şifreler eşleşmiyor!");
                return ResponseEntity.status(401).body("E-posta veya şifre hatalı!");
            }

            System.out.println("Giriş başarılı!");
            
            sessionRepository.deleteByUserId(dbUser.getUserId());
            
            String sessionToken = UUID.randomUUID().toString();
            Session session = new Session();
            session.setSessionToken(sessionToken);
            session.setUserId(dbUser.getUserId());
            sessionRepository.save(session);
            
            System.out.println("Session oluşturuldu: " + sessionToken);
            
            Map<String, Object> response = new HashMap<>();
            response.put("sessionToken", sessionToken);
            response.put("fullName", dbUser.getFullName());
            response.put("email", dbUser.getEmail());
            response.put("message", "Giriş Başarılı! Hoşgeldin " + dbUser.getFullName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Hata: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestHeader("Session-Token") String sessionToken) {
        try {
            Session session = sessionRepository.findBySessionToken(sessionToken)
                    .orElseThrow(() -> new RuntimeException("Session bulunamadı!"));
            
            sessionRepository.delete(session);
            
            return ResponseEntity.ok("Çıkış başarılı!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Hata: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Session-Token") String sessionToken) {
        try {
            System.out.println("=== KULLANICI BİLGİSİ İSTEĞİ ===");
            System.out.println("Session Token: " + sessionToken);
            
            Session session = sessionRepository.findBySessionToken(sessionToken)
                    .orElseThrow(() -> new RuntimeException("Geçersiz session!"));
            
            User user = userRepository.findById(session.getUserId())
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", user.getUserId());
            response.put("fullName", user.getFullName());
            response.put("email", user.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body("Geçersiz veya süresi dolmuş session!");
        }
    }
}