package modak.modakmodak.repository;

import modak.modakmodak.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 유저의 username으로 회고를 찾고, 작성 시간(CreatedAt) 내림차순(Desc)으로 정렬합니다.
    List<Review> findByUserUsernameOrderByCreatedAtDesc(String username);
}