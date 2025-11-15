package com.receipthub.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.receipthub.model.User;
import com.receipthub.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    public void createUser(User user) {
        userRepository.save(user);
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    
    public Optional<User> getUserByTelegramChatId(Long chatId) {
        return userRepository.findByTelegramChatId(chatId);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }
    
    public void linkTelegramChatId(Long userId, Long chatId) {
        User user = getUserById(userId);
        user.setTelegramChatId(chatId);
        userRepository.save(user);
    }
    
    public List<User> getAdminUsers() {
        return userRepository.findByRole(User.UserRole.ADMIN);
    }
    
    public void updateUser(Long id, User updatedUser) {
        User user = getUserById(id);
        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());
        user.setRole(updatedUser.getRole());
        userRepository.save(user);
    }
    
    public Page<User> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return userRepository.findAll(pageable);
    }
    
    public Page<User> searchUsers(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageable);
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
