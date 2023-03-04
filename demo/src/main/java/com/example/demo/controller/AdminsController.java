package com.example.demo.controller;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.service.RoleService;
import com.example.demo.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.xml.bind.ValidationException;
import java.security.Principal;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminsController {

    private final UserService userService;
    private final RoleService roleService;

    public AdminsController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping()
    public String getAdminPage(@ModelAttribute("user") User user,
                               Model model, Principal principal) {

        model.addAttribute("admin", userService.getUserByUsername(principal.getName()));
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("roles", roleService.getRoles());
        return "/admin";
    }

    @PostMapping("/createNew")
    public String createUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult,
                             @RequestParam(value = "nameRole") String nameRole) {

        if (!bindingResult.hasErrors()) {
            Role role = new Role(nameRole);
            roleService.saveRole(role);
            user.setRoles(Set.of(role));
            userService.saveUser(user);
        }
        return "redirect:/admin";
    }


    @PatchMapping(value = "/{id}/edit")
    public String updateUser(@ModelAttribute("user") @Valid User user, @PathVariable("id") Long id,
                             @RequestParam(value = "nameRole") String nameRole, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            Role role = new Role(nameRole);
            roleService.saveRole(role);
            user.setRoles(Set.of(role));
        }
        if (userService.updateUser(user, id))
            return "redirect:/login";
        else
            return "redirect:/admin";
    }

    @DeleteMapping("/{id}/delete")
    public String removeUserById(@PathVariable("id") Long id, Principal principal) {
        boolean checkDeletingUserIsCurrent = userService.getUserByUsername(principal.getName()).equals(userService.getUserById(id));

        roleService.removeRoleById(id);
        userService.removeById(id);

        if (checkDeletingUserIsCurrent)
            return "redirect:/login";
        else
            return "redirect:/admin";
    }

    @GetMapping("/user")
    public String getUserPage(Model model, Principal principal) {
        Long id = userService.getUserByUsername(principal.getName()).getId();
        model.addAttribute("admin", userService.getUserByUsername(principal.getName()));
        model.addAttribute("user", userService.getUserById(id));
        return "admin_show_user";
    }
}
