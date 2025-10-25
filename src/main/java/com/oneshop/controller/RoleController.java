package com.oneshop.controller;

import com.oneshop.entity.Role;
import com.oneshop.service.RoleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public String listRoles(Model model) {
        model.addAttribute("roles", roleService.getAllRoles());
        return "role/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("role", new Role());
        return "role/form";
    }

    @PostMapping
    public String createRole(@ModelAttribute("role") Role role) {
        roleService.createRole(role);
        return "redirect:/roles";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Role role = roleService.getRoleById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        model.addAttribute("role", role);
        return "role/form";
    }

    @PostMapping("/update/{id}")
    public String updateRole(@PathVariable Long id, @ModelAttribute("role") Role role) {
        roleService.updateRole(id, role);
        return "redirect:/roles";
    }

    @GetMapping("/delete/{id}")
    public String deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return "redirect:/roles";
    }
}
