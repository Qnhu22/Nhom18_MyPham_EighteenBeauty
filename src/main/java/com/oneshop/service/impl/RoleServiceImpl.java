package com.oneshop.service.impl;

import com.oneshop.entity.Role;
import com.oneshop.repository.RoleRepository;
import com.oneshop.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository repository;

    public RoleServiceImpl(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Role> getAllRoles() {
        return repository.findAll();
    }

    @Override
    public Optional<Role> getRoleById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Role createRole(Role role) {
        return repository.save(role);
    }

    @Override
    public Role updateRole(Long id, Role role) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setRoleName(role.getRoleName()); // ✅ đổi name -> roleName
                    return repository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }

    @Override
    public void deleteRole(Long id) {
        repository.deleteById(id);
    }
}
