package com.seal.seal_hackathon_fpt.features.mentor.repository;

import com.seal.seal_hackathon_fpt.features.mentor.entity.MentorRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MentorRoomRepository extends JpaRepository<MentorRoom, Long> {
    Optional<MentorRoom> findByTeamId(Long teamId);
    long countByMentorId(Long mentorId);

    // Thêm hàm này để tìm trực tiếp danh sách phòng của Mentor dưới DB
    List<MentorRoom> findByMentorId(Long mentorId);
}