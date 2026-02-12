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
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "존재하지 않는 사용자입니다. ID: " + request.toUserId()));

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
                java.util.List<modak.modakmodak.dto.MateRequestListResponse.MateRequestDto> requestDtos = requests
                                .stream()
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

        @Transactional
        public modak.modakmodak.dto.MateApprovalResponse approveMateRequest(Long userId, Long requestId,
                        modak.modakmodak.dto.MateApprovalRequest request) {
                // 1. 메이트 요청 조회
                MateRequest mateRequest = mateRequestRepository.findById(requestId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요청입니다. ID: " + requestId));

                // 2. 요청 받은 사람이 맞는지 확인
                if (!mateRequest.getToUser().getId().equals(userId)) {
                        throw new IllegalArgumentException("요청을 처리할 권한이 없습니다.");
                }

                // 3. 이미 처리된 요청인지 확인
                if (mateRequest.getStatus() != MateRequestStatus.PENDING) {
                        throw new IllegalArgumentException("이미 처리된 요청입니다.");
                }

                // 4. 상태 업데이트
                MateRequestStatus newStatus = MateRequestStatus.valueOf(request.status());
                mateRequest.updateStatus(newStatus);

                String message;
                Long mateUserId = mateRequest.getFromUser().getId();

                // 5. ACCEPTED인 경우 Mate 관계 생성
                if (newStatus == MateRequestStatus.ACCEPTED) {
                        modak.modakmodak.entity.Mate mate = modak.modakmodak.entity.Mate.builder()
                                        .user1(mateRequest.getFromUser())
                                        .user2(mateRequest.getToUser())
                                        .build();
                        mateRepository.save(mate);
                        message = "메이트 요청을 승인했습니다.";
                } else {
                        message = "메이트 요청을 거절했습니다.";
                }

                return new modak.modakmodak.dto.MateApprovalResponse(message, mateUserId);
        }

        @Transactional(readOnly = true)
        public modak.modakmodak.dto.MateListResponse getMateList(Long userId) {
                // 내 메이트 목록 조회 (양방향)
                java.util.List<modak.modakmodak.entity.Mate> mates = mateRepository.findMatesByUserId(userId);

                // DTO로 변환 (상대방 정보만 추출)
                java.util.List<modak.modakmodak.dto.MateListResponse.MateDto> mateDtos = mates.stream()
                                .map(mate -> {
                                        // 내가 user1이면 user2를, user2이면 user1을 반환
                                        User mateUser = mate.getUser1().getId().equals(userId)
                                                        ? mate.getUser2()
                                                        : mate.getUser1();

                                        return new modak.modakmodak.dto.MateListResponse.MateDto(
                                                        mateUser.getId(),
                                                        mateUser.getNickname(),
                                                        mateUser.getProfileImage() != null
                                                                        ? mateUser.getProfileImage()
                                                                        : "https://modak-bucket.s3.amazonaws.com/default-profile.png",
                                                        mate.getCreatedAt().toString());
                                })
                                .toList();

                return new modak.modakmodak.dto.MateListResponse(mateDtos);
        }
}
