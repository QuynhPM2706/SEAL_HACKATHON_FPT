package com.seal.seal_hackathon_fpt.features.mentor.service;

import com.seal.seal_hackathon_fpt.features.mentor.entity.*;
import com.seal.seal_hackathon_fpt.features.mentor.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MentorChatService {

    private final MentorRequestRepository requestRepository;
    private final MentorRoomRepository roomRepository;
    private final ChatMessageRepository messageRepository;

    // QUY TRÌNH 1: Đội thi gửi lời mời đích danh cho Mentor
    public MentorRequest sendRequestToMentor(Long teamId, Long mentorId) {
        // Kiểm tra xem Team này đã có phòng chat/đã thuộc về Mentor nào chưa
        if (roomRepository.findByTeamId(teamId).isPresent()) {
            throw new RuntimeException("Đội của bạn đã có Mentor hướng dẫn rồi, không thể mời thêm.");
        }

        // Tạo mới hoặc cập nhật lại lời mời PENDING cũ
        MentorRequest request = requestRepository.findByTeamIdAndMentorId(teamId, mentorId)
                .orElse(new MentorRequest());

        request.setTeamId(teamId);
        request.setMentorId(mentorId);
        request.setStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        return requestRepository.save(request);
    }

    // QUY TRÌNH 2: Mentor xem danh sách lời mời đang chờ mình duyệt
    public List<MentorRequest> getPendingRequestsForMentor(Long mentorId) {
        return requestRepository.findByMentorIdAndStatus(mentorId, "PENDING");
    }

    // QUY TRÌNH 3: Xử lý quyết định của Mentor (ACCEPT / DENY)
    @Transactional
    public MentorRequest handleRequest(Long requestId, String decision) {
        MentorRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Yêu cầu này không tồn tại."));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Yêu cầu này đã được xử lý từ trước.");
        }

        if ("DENIED".equals(decision)) {
            request.setStatus("DENIED");
            request.setUpdatedAt(LocalDateTime.now());
            return requestRepository.save(request);
        }

        if ("ACCEPTED".equals(decision)) {
            // LUẬT 1: Một Mentor chỉ dẫn dắt tối đa 5 Team
            long currentTeamsCount = roomRepository.countByMentorId(request.getMentorId());
            if (currentTeamsCount >= 5) {
                throw new RuntimeException("Bạn đã đạt giới hạn hướng dẫn tối đa 5 đội thi!");
            }

            // LUẬT 2: Kiểm tra xem Team này có lỡ được Mentor khác nhận trước đó chưa
            if (roomRepository.findByTeamId(request.getTeamId()).isPresent()) {
                request.setStatus("DENIED"); // Chuyển thành từ chối luôn vì team đã có chủ
                request.setUpdatedAt(LocalDateTime.now());
                requestRepository.save(request);
                throw new RuntimeException("Đội thi này đã được một Mentor khác nhận hướng dẫn.");
            }

            // Cập nhật trạng thái lời mời thành ACCEPTED
            request.setStatus("ACCEPTED");
            request.setUpdatedAt(LocalDateTime.now());
            requestRepository.save(request);

            // TỰ ĐỘNG TẠO PHÒNG CHAT CHO MENTOR VÀ TEAM ĐÓ
            MentorRoom newRoom = MentorRoom.builder()
                    .teamId(request.getTeamId())
                    .mentorId(request.getMentorId())
                    .createdAt(LocalDateTime.now())
                    .build();
            roomRepository.save(newRoom);

            return request;
        }

        throw new RuntimeException("Quyết định không hợp lệ (Chỉ nhận ACCEPTED hoặc DENIED)");
    }

    // --- LOGIC CHAT (ĂN THEO ROOM ID ĐÃ TỰ ĐỘNG TẠO Ở TRÊN) ---

    public ChatMessage sendMessage(Long roomId, Long senderId, String senderName, String content) {
        roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng chat không tồn tại hoặc bạn chưa được chấp nhận kết nối."));

        ChatMessage message = ChatMessage.builder()
                .roomId(roomId)
                .senderId(senderId)
                .senderName(senderName)
                .messageContent(content)
                .createdAt(LocalDateTime.now())
                .build();

        return messageRepository.save(message);
    }

    public List<ChatMessage> getMessages(Long roomId) {
        return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
    }

    // Tiện ích bổ sung: Giúp Frontend kiểm tra nhanh xem Team mình đã có Room Chat thật chưa
    public MentorRoom getActiveRoomByTeam(Long teamId) {
        return roomRepository.findByTeamId(teamId)
                .orElseThrow(() -> new RuntimeException("Đội của bạn hiện chưa có phòng chat với Mentor nào."));
    }

    public List<MentorRoom> getActiveRoomsForMentor(Long mentorId) {
        return roomRepository.findByMentorId(mentorId);
    }
}
