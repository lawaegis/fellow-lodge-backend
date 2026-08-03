package com.fellowlodge.api.controller;

import com.fellowlodge.api.common.ApiResponse;
import com.fellowlodge.api.common.PageResponse;
import com.fellowlodge.api.entity.AuditLog;
import com.fellowlodge.api.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_LOGS:READ')")
    public ApiResponse<List<AuditLog>> findAll(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               @RequestParam(required = false) UUID userId,
                                               @RequestParam(required = false) String tableName) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> result;
        if (userId != null) {
            result = auditLogRepository.findAll(
                    (root, query, cb) -> cb.equal(root.get("userId"), userId), pageable);
        } else if (tableName != null && !tableName.isBlank()) {
            result = auditLogRepository.findAll(
                    (root, query, cb) -> cb.equal(cb.lower(root.get("tableName")), tableName.toLowerCase()), pageable);
        } else {
            result = auditLogRepository.findAll(pageable);
        }
        return ApiResponse.ok(result.getContent(), PageResponse.from(result));
    }
}
