package com.seal.seal_hackathon_fpt.features.mentor.controller;

import com.seal.seal_hackathon_fpt.features.mentor.entity.Mentor;
import com.seal.seal_hackathon_fpt.features.mentor.service.MentorService;
import com.seal.seal_hackathon_fpt.features.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;

    // API 1: Tạo mới Mentor (Chỉ cho phép tài khoản mang quyền Admin hoặc Coordinator tạo)
    @PostMapping
    @PreAuthorize("principal.role == 'Admin' or principal.role == 'Coordinator'")
    public ResponseEntity<Mentor> createMentor(
            @AuthenticationPrincipal User currentUser, // Lấy thông tin User đang thao tác cấp quyền
            @RequestParam String specialty,
            @RequestParam String organization
    ) {
        return ResponseEntity.ok(mentorService.createMentor(currentUser, specialty, organization));
    }

    // API 2: Lấy toàn bộ danh sách Mentor (Thí sinh 'Participant' hay ai cũng được xem để chọn lựa)
    @GetMapping
    public ResponseEntity<List<Mentor>> getAllMentors() {
        return ResponseEntity.ok(mentorService.getAllMentors());
    }
}
