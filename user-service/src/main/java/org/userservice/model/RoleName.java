package org.userservice.model;

/**
 * Application-level roles.
 *
 * SUPER_ADMIN  – platform owner; crosses tenant boundaries
 * ADMIN        – tenant administrator
 * MANAGER      – team / department lead within a tenant
 * USER         – standard tenant user
 */
public enum RoleName {
    SUPER_ADMIN,
    ADMIN,
    MANAGER,
    USER
}