package org.example.backend.enums;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User roles for the warehouse management system
 */
public enum Role {
    ADMIN(Set.of(
            Permission.PRODUCT_READ, Permission.PRODUCT_WRITE,
            Permission.INVENTORY_READ, Permission.INVENTORY_WRITE,
            Permission.LOCATION_READ, Permission.LOCATION_WRITE,
            Permission.OPERATION_READ, Permission.OPERATION_WRITE,
            Permission.USER_READ, Permission.USER_WRITE,
            Permission.DASHBOARD_READ)),
    SUPERVISOR(Set.of(
            Permission.PRODUCT_READ, Permission.PRODUCT_WRITE,
            Permission.INVENTORY_READ, Permission.INVENTORY_WRITE,
            Permission.LOCATION_READ, Permission.LOCATION_WRITE,
            Permission.OPERATION_READ, Permission.OPERATION_WRITE,
            Permission.USER_READ,
            Permission.DASHBOARD_READ)),
    EMPLOYEE(Set.of(
            Permission.PRODUCT_READ,
            Permission.INVENTORY_READ,
            Permission.OPERATION_READ,
            Permission.OPERATION_WRITE,
            Permission.DASHBOARD_READ));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}