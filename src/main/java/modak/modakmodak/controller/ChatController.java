package modak.modakmodak.controller;

import lombok.RequiredArgsConstructor;
import modak.modakmodak.dto.ChatMessageDto;
import modak.modakmodak.entity.ChatMessage;
import modak.modakmodak.repository.ChatMessageRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;

    // 프론트엔드에서 /pub/chat/message 로 메시지를 발행하면 이 메서드가 실행됨
    @MessageMapping("/chat/message")
    public void message(ChatMessageDto messageDto) {
        // 1. DB에 메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .meetingId(messageDto.meetingId())
                .senderId(messageDto.senderId())
                .senderNickname(messageDto.senderNickname())
                .isHost(messageDto.isHost())
                .message(messageDto.message())
                .build();
        chatMessageRepository.save(chatMessage);

        // 2. 해당 팟(/sub/chat/room/{meetingId})을 구독 중인 사람들에게 메시지 전달
        ChatMessageDto responseDto = new ChatMessageDto(
                chatMessage.getMeetingId(),
                chatMessage.getSenderId(),
                chatMessage.getSenderNickname(),
                chatMessage.isHost(),
                chatMessage.getMessage(),
                chatMessage.getCreatedAt() != null ? chatMessage.getCreatedAt() : LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/sub/chat/room/" + messageDto.meetingId(), responseDto);
    }

    // (옵션) 팟에 처음 들어갔을 때 이전 채팅 내역 불러오기 API
    @GetMapping("/api/meetings/{meetingId}/chats")
    public List<ChatMessage> getChatHistory(@PathVariable Long meetingId) {
        return chatMessageRepository.findAllByMeetingIdOrderByCreatedAtAsc(meetingId);
    }
}