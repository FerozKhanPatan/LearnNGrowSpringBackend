package com.elearning.controller;

import com.elearning.model.User;
import com.elearning.service.JwtService;
import com.elearning.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> registrationRequest) {
        try {
            System.out.println("=== REGISTER REQUEST START ===");
            System.out.println("REQUEST BODY = " + registrationRequest);
            logger.info("=== REGISTER REQUEST START ===");
            logger.info("Registration request: {}", registrationRequest);

            String name = registrationRequest.get("name");
            String email = registrationRequest.get("email");
            String password = registrationRequest.get("password");

            logger.info("Registering user: name={}, email={}", name, email);
            System.out.println("Registering user: name=" + name + ", email=" + email);

            if (userRepository.existsByEmail(email)) {
                logger.warn("Email already exists: {}", email);
                System.out.println("ERROR: Email already exists: " + email);
                return ResponseEntity.badRequest().body("Email already exists!");
            }

            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);
            user.setRole(User.UserRole.STUDENT);

            User savedUser = userRepository.save(user);

            logger.info("User registered successfully: id={}, email={}", savedUser.getId(), savedUser.getEmail());
            System.out.println("User registered successfully: id=" + savedUser.getId() + ", email=" + savedUser.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully!");
            response.put("user", savedUser);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage(), e);
            System.err.println("Registration failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginRequest) {
        try {
            System.out.println("=== LOGIN REQUEST START ===");
            System.out.println("REQUEST BODY = " + loginRequest);
            logger.info("=== LOGIN REQUEST START ===");
            logger.info("Login request: {}", loginRequest);

            String email = loginRequest.get("email");
            String password = loginRequest.get("password");

            logger.info("Attempting login for email: {}", email);
            System.out.println("Attempting login for email: " + email);

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                logger.warn("User not found with email: {}", email);
                System.out.println("ERROR: User not found with email: " + email);
                return ResponseEntity.badRequest().body("User not found!");
            }

            User user = userOpt.get();
            logger.info("Found user: id={}, email={}", user.getId(), user.getEmail());
            System.out.println("Found user: id=" + user.getId() + ", email=" + user.getEmail());
            System.out.println("Stored password: " + user.getPassword());
            System.out.println("Provided password: " + password);

            if (!user.getPassword().equals(password)) {
                logger.warn("Invalid credentials for email: {}", email);
                System.out.println("ERROR: Invalid credentials - password mismatch");
                return ResponseEntity.badRequest().body("Invalid credentials!");
            }

            String token = jwtService.generateToken(email);
            logger.info("JWT token generated successfully for email: {}", email);
            System.out.println("JWT token generated successfully for email: " + email);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);

            logger.info("Login successful for email: {}", email);
            System.out.println("=== LOGIN SUCCESSFUL ===");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login failed: {}", e.getMessage(), e);
            System.err.println("Login failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }
}
