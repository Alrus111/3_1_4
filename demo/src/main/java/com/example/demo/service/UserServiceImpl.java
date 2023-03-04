package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    @PersistenceContext
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(EntityManager entityManager, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.entityManager = entityManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public void saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        Optional<User> userById = userRepository.findById(id);
        if (userById.isPresent())
            return userById.get();
        else
            throw new UsernameNotFoundException(String.format("User with %s not found", id));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.getUserByUsername(username);
    }

    @Transactional
    @Override
    public boolean updateUser(User updatedUser, Long id) {
        if (updatedUser.getPassword().hashCode() != getUserById(id).getPassword().hashCode())
            updatedUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        boolean checkUpdateUsername = false;
        if (userRepository.getUserByUsername(updatedUser.getUsername()) == null)
            checkUpdateUsername = true;
        userRepository.save(updatedUser);
        return checkUpdateUsername;
    }

    @Transactional
    @Override
    public void removeById(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.getUserWithRole(username);

        if (user == null) {
            throw new UsernameNotFoundException(String.format("User with %s not found", username));
        }
        return user;
    }

}
