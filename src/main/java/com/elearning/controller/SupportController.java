package com.elearning.controller;

import com.elearning.model.SupportTicket;
import com.elearning.model.User;
import com.elearning.repository.SupportTicketRepository;
import com.elearning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = "*")
public class SupportController {

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/ticket")
    public ResponseEntity<?> createSupportTicket(@RequestBody Map<String, Object> ticketRequest) {
        try {
            Long userId = Long.valueOf(ticketRequest.get("userId").toString());
            String subject = ticketRequest.get("subject").toString();
            String message = ticketRequest.get("message").toString();

            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found!");
            }

            SupportTicket supportTicket = new SupportTicket();
            supportTicket.setSubject(subject);
            supportTicket.setMessage(message);
            supportTicket.setStatus(SupportTicket.TicketStatus.OPEN);
            supportTicket.setUser(userOpt.get());
            supportTicket.setCreatedDate(LocalDateTime.now());

            SupportTicket savedTicket = supportTicketRepository.save(supportTicket);

            return ResponseEntity.ok(savedTicket);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Support ticket creation failed: " + e.getMessage());
        }
    }

    @GetMapping("/tickets/{userId}")
    public ResponseEntity<List<SupportTicket>> getUserTickets(@PathVariable Long userId) {
        try {
            List<SupportTicket> tickets = supportTicketRepository.findByUserId(userId);
            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
