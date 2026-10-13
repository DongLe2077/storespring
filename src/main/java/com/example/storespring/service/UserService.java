package com.example.storespring.service;

import com.example.storespring.dto.UserRegisterDTO;
import com.example.storespring.entity.User;
import com.example.storespring.repository.UserRepository;
import com.example.storespring.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User register(UserRegisterDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống!");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự!");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp!");
        }
        if (userRepository.existsByUsername(dto.getUsername().trim())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trên hệ thống!");
        }
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            if (userRepository.existsByEmail(dto.getEmail().trim())) {
                throw new IllegalArgumentException("Email này đã được sử dụng!");
            }
        }

        User user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setPassword(PasswordUtil.hashPassword(dto.getPassword()));
        user.setFullname(dto.getFullname());
        user.setEmail(dto.getEmail());
        user.setRole("USER"); // Mặc định là quyền USER thông thường

        return userRepository.save(user);
    }

    public User login(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập và mật khẩu!");
        }

        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản hoặc mật khẩu không chính xác!"));

        if (!PasswordUtil.checkPassword(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Tài khoản hoặc mật khẩu không chính xác!");
        }

        return user;
    }

    public Optional<User> findById(Integer id) {
        if (id == null) return Optional.empty();
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return userRepository.findByUsername(username.trim());
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public long countAllUsers() {
        return userRepository.count();
    }

    @Transactional
    public boolean changeRole(Integer userId, String newRole) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRole(newRole);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    // Tự động khởi tạo tài khoản mặc định nếu chưa có
    @Transactional
    public void initDefaultAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(PasswordUtil.hashPassword("admin123"));
            admin.setFullname("Quản Trị Viên");
            admin.setEmail("admin@storespring.com");
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }
        if (!userRepository.existsByUsername("dong1")) {
            User user = new User();
            user.setUsername("dong1");
            user.setPassword(PasswordUtil.hashPassword("123456"));
            user.setFullname("Đồng");
            user.setEmail("dong1@aura.vn");
            user.setRole("USER");
            userRepository.save(user);
        }
    }
}
