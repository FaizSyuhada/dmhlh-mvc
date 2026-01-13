package com.dmhlh.repository;

import com.dmhlh.entity.ForumDraft;
import com.dmhlh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumDraftRepository extends JpaRepository<ForumDraft, Long> {
    List<ForumDraft> findByUserOrderByUpdatedAtDesc(User user);
    List<ForumDraft> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
