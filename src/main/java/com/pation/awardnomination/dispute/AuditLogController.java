package com.pation.awardnomination.dispute;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for querying the Immutable Audit & Transparency Log.
 */
@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = "*")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Autowired
    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditTransparencyLog>> getAllLogs() {
        return ResponseEntity.ok(auditLogService.getAllAuditLogs());
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<?> getLogForTicket(@PathVariable Long ticketId) {
        return auditLogService.getAuditLogForTicket(ticketId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
