package com.dmhlh.repository;

import com.dmhlh.entity.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionTypeRepository extends JpaRepository<SessionType, Long> {
    List<SessionType> findByActiveTrueOrderByDisplayOrderAsc();
}
