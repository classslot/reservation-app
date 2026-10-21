package com.example.reservation.repository;

import com.example.reservation.domain.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUser_Id(Long userId);
    Page<Student> findByNameContaining(String name, Pageable pageable);
}
