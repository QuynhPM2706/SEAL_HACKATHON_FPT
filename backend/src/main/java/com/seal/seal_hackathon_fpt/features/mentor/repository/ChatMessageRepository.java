package com.seal.seal_hackathon_fpt.features.mentor.repository;

import com.seal.seal_hackathon_fpt.features.mentor.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // Lấy lịch sử chat sắp xếp theo thời gian
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);
}