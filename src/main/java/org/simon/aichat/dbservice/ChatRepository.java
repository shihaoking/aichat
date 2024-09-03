package org.simon.aichat.dbservice;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<ChatRecord, Long> {

}
