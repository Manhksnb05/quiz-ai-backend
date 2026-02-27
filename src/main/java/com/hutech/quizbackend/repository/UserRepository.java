package com.hutech.quizbackend.repository;

import com.hutech.quizbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Hàm này tự động sinh ra câu lệnh SQL tìm user theo email
    Optional<User> findByEmail(String email);
}