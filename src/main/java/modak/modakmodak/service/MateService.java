package modak.modakmodak.service;

import lombok.RequiredArgsConstructor;
import modak.modakmodak.dto.MateRequestRequest;
import modak.modakmodak.dto.MateRequestResponse;
import modak.modakmodak.entity.MateRequest;
import modak.modakmodak.entity.MateRequestStatus;
import modak.modakmodak.entity.User;
import modak.modakmodak.repository.MateRepository;
import modak.modakmodak.repository.MateRequestRepository;
import modak.modakmodak.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MateService {

    private final MateRequestRepository mateRequestRepository;
    private final MateRepository mateRepository;
    private final UserRepository userRepository;

    public MateRequestResponse sendMateRequest(Long fromUserId, MateRequestRequest request) {
        // 1. 요청 받는 사용자 존재 확인
        User toUser = userRepository.findById(request.toUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. ID: " + request.toUserId()));

        // 2. 요청 보낸 사용자 조회
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + fromUserId));

        // 3. 이미 메이트인지 확인 (양방향)
        if (mateRepository.existsMateRelationship(fromUserId, request.toUserId())) {
            throw new IllegalArgumentException("이미 메이트입니다.");
        }

        // 4. 중복 신청 확인 (from -> to)
        if (mateRequestRepository.existsByFromUserIdAndToUserId(fromUserId, request.toUserId())) {
            throw new IllegalArgumentException("이미 메이트 신청을 보냈습니다.");
        }

        // 5. 메이트 요청 생성
        MateRequest mateRequest = MateRequest.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .status(MateRequestStatus.PENDING)
                .build();

        MateRequest savedRequest = mateRequestRepository.save(mateRequest);

        return new MateRequestResponse(
                savedRequest.getId(),
                "메이트 신청 완료");
    }

    @Transactional(readOnly = true)
    public modak.modakmodak.dto.MateRequestListResponse getMateRequestList(Long userId) {
        // 내가 받은 메이트 요청 목록 조회
        java.util.List<MateRequest> requests = mateRequestRepository.findByToUserId(userId);

        // DTO로 변환
        java.util.List<modak.modakmodak.dto.MateRequestListResponse.MateRequestDto> requestDtos = requests.stream()
                .map(request -> new modak.modakmodak.dto.MateRequestListResponse.MateRequestDto(
                        request.getId(),
                        request.getFromUser().getId(),
                        request.getFromUser().getNickname(),
                        request.getFromUser().getProfileImage() != null
                                ? request.getFromUser().getProfileImage()
                                : "https://modak-bucket.s3.amazonaws.com/default-profile.png",
                        request.getStatus().name(),
                        request.getCreatedAt().toString()))
                .toList();

        return new modak.modakmodak.dto.MateRequestListResponse(requestDtos);
    }
}
