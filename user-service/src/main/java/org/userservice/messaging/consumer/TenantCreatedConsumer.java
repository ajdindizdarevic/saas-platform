package org.userservice.messaging.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.userservice.dto.request.CreateUserDto;
import org.userservice.messaging.event.TenantCreatedEvent;
import org.userservice.model.Role;
import org.userservice.model.RoleName;
import org.userservice.model.User;
import org.userservice.multitenancy.TenantContext;
import org.userservice.repository.RoleRepository;
import org.userservice.repository.UserRepository;
import org.userservice.service.UserService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantCreatedConsumer {

    private final UserService    userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Listens for TenantCreatedEvent and seeds the initial ADMIN user for the tenant.
     *
     * <p>If the contact email is already registered (e.g. the owner registered themselves),
     * their existing account is promoted to ADMIN for the new tenant instead of creating
     * a duplicate.
     */
    @RabbitListener(queues = "${rabbitmq.queue.tenant-created}",
            containerFactory = "rabbitListenerContainerFactory")
    public void handleTenantCreated(TenantCreatedEvent event) {
        log.info("Received TenantCreatedEvent: tenantId={}, schema={}, eventId={}",
                event.getTenantId(), event.getSchemaName(), event.getEventId());

        try {
            TenantContext.setCurrentSchema(event.getSchemaName());

            Optional<User> existingUser = userRepository.findByEmail(event.getContactEmail());
            if (existingUser.isPresent()) {
                // Promote the existing user to ADMIN and link them to this tenant
                promoteToAdmin(existingUser.get(), event);
            } else {
                createAdminUser(event);
            }

        } catch (Exception ex) {
            log.error("Failed to seed admin user for tenantId={}: {}",
                    event.getTenantId(), ex.getMessage(), ex);
            throw ex; // route to DLQ
        } finally {
            TenantContext.clear();
        }
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private void promoteToAdmin(User user, TenantCreatedEvent event) {
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));

        user.getRoles().add(adminRole);
        user.setTenantId(event.getTenantId() != null ? event.getTenantId().toString() : null);
        user.setTenantSchema(event.getSchemaName());
        if (event.getOnboardingRequestId() != null) {
            user.setOnboardingRequestId(event.getOnboardingRequestId());
        }
        userRepository.save(user);

        log.info("Promoted existing user '{}' to ADMIN for tenant={} in schema={}",
                user.getUsername(), event.getTenantId(), event.getSchemaName());
    }

    private void createAdminUser(TenantCreatedEvent event) {
        String username = nextAdminUsername();

        CreateUserDto dto = CreateUserDto.builder()
                .firstName(extractFirstName(event.getContactName()))
                .lastName(extractLastName(event.getContactName()))
                .email(event.getContactEmail())
                .username(username)
                .password("admin")          // default; admin can change after first login
                .tenantId(event.getTenantId() != null ? event.getTenantId().toString() : null)
                .tenantSchema(event.getSchemaName())
                .onboardingRequestId(event.getOnboardingRequestId())
                .roles(Set.of(RoleName.ADMIN))
                .build();

        userService.createUser(dto);
        log.info("Seeded admin user '{}' for tenant={} in schema={}",
                username, event.getTenantId(), event.getSchemaName());
    }

    /**
     * Returns the next sequential admin username: admin1, admin2, admin3, …
     */
    private String nextAdminUsername() {
        long adminCount = userRepository.countByRoleName(RoleName.ADMIN);
        String candidate = "admin" + (adminCount + 1);
        while (userRepository.existsByUsername(candidate)) {
            adminCount++;
            candidate = "admin" + (adminCount + 1);
        }
        return candidate;
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "Admin";
        String[] parts = fullName.trim().split("\\s+", 2);
        return parts[0];
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "";
        String[] parts = fullName.trim().split("\\s+", 2);
        return parts.length > 1 ? parts[1] : "";
    }
}