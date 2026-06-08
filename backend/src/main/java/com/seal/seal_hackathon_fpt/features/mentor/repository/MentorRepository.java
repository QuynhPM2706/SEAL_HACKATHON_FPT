package com.seal.seal_hackathon_fpt.features.mentor.repository;

import com.seal.seal_hackathon_fpt.features.mentor.entity.Mentor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MentorRepository extends JpaRepository<Mentor, Long> {
    // Hàm phụ trợ hỗ trợ tìm kiếm Mentor dựa trên userId đăng nhập (rất cần cho các hàm Chat sau này)
    Optional<Mentor> findByUserId(Long userId);
}
