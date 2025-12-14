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

import com.cufcuf.backend.model.Admin;
import com.cufcuf.backend.model.Session;
import com.cufcuf.backend.repository.AdminRepository;
import com.cufcuf.backend.repository.SessionRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminRepository adminRepository;
    private final SessionRepository sessionRepository;

    public AdminController(AdminRepository adminRepository, SessionRepository sessionRepository) {
        this.adminRepository = adminRepository;
        this.sessionRepository = sessionRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginAdmin(@RequestBody Map<String, String> loginRequest) {
        try {
            System.out.println("=== ADMIN LOGIN İSTEĞİ ===");
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            
            System.out.println("Username: " + username);
            
            Admin admin = adminRepository.findByUsername(username)
                    .orElse(null);
            
            if (admin == null) {
                System.out.println("Admin bulunamadı!");
                return ResponseEntity.status(401).body("Kullanıcı adı veya şifre hatalı!");
            }
            
            if (!admin.getPasswordHash().equals(password)) {
                System.out.println("Şifre yanlış!");
                return ResponseEntity.status(401).body("Kullanıcı adı veya şifre hatalı!");
            }
            
            System.out.println("Admin giriş başarılı!");
            
            // Eski admin session'larını sil
            sessionRepository.deleteByUserId(admin.getId());
            
            // Yeni session oluştur (admin için özel token)
            String sessionToken = "ADMIN_" + UUID.randomUUID().toString();
            Session session = new Session();
            session.setSessionToken(sessionToken);
            session.setUserId(admin.getId());
            sessionRepository.save(session);
            
            Map<String, Object> response = new HashMap<>();
            response.put("sessionToken", sessionToken);
            response.put("fullName", admin.getFullName());
            response.put("username", admin.getUsername());
            response.put("role", admin.getRole());
            response.put("message", "Admin girişi başarılı! Hoşgeldin " + admin.getFullName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Hata: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutAdmin(@RequestHeader("Session-Token") String sessionToken) {
        try {
            Session session = sessionRepository.findBySessionToken(sessionToken)
                    .orElseThrow(() -> new RuntimeException("Session bulunamadı!"));
            
            sessionRepository.delete(session);
            
            return ResponseEntity.ok("Admin çıkışı başarılı!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Hata: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentAdmin(@RequestHeader("Session-Token") String sessionToken) {
        try {
            Session session = sessionRepository.findBySessionToken(sessionToken)
                    .orElseThrow(() -> new RuntimeException("Geçersiz session!"));
            
            Admin admin = adminRepository.findById(session.getUserId())
                    .orElseThrow(() -> new RuntimeException("Admin bulunamadı!"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", admin.getId());
            response.put("username", admin.getUsername());
            response.put("fullName", admin.getFullName());
            response.put("email", admin.getEmail());
            response.put("role", admin.getRole());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body("Geçersiz veya süresi dolmuş session!");
        }
    }
}