package org.userservice.service;

import lombok.RequiredArgsConstructor;
import org.userservice.model.Role;
import org.userservice.model.RoleName;
import org.userservice.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleResolverService {

    private final RoleRepository roleRepository;

    @Transactional
    public Set<Role> resolveRoles(Set<RoleName> roleNames) {
        return roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseGet(() -> roleRepository.save(
                                Role.builder().name(name).build())))
                .collect(Collectors.toSet());
    }
}