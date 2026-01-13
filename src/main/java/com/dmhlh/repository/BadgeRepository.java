package com.dmhlh.repository;

import com.dmhlh.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    Optional<Badge> findByCode(String code);
    List<Badge> findByActiveTrue();
    List<Badge> findByActiveTrueOrderByPointsValueDesc();
    List<Badge> findByCategory(Badge.Category category);
}
