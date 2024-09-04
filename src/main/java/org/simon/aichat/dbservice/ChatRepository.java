package org.simon.aichat.dbservice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<ChatRecord, Long> {
    Optional<List<ChatRecord>> findByUserId(Long userId);
}
