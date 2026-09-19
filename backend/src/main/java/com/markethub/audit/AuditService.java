package com.markethub.audit;

import com.markethub.user.User;
import com.markethub.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditService {
    private final AuditLogRepository logs;
    private final UserRepository users;

    public AuditService(AuditLogRepository logs, UserRepository users) {
        this.logs = logs;
        this.users = users;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String resourceType, String resourceId, String result) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal()) ? authentication.getName() : null;
        User actor = email == null ? null : users.findByEmail(email).orElse(null);
        String role = actor == null ? null : actor.getRole().name();
        String requestId = currentRequestId();
        String metadata = "{\"operation\":\"" + action + "\"}";
        logs.save(new AuditLog(actor == null ? null : actor.getId(), email, role, action,
                resourceType, resourceId, requestId, result, metadata));
    }

    private String currentRequestId() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            Object requestId = request.getAttribute("requestId");
            return requestId == null ? null : requestId.toString();
        }
        return null;
    }
}