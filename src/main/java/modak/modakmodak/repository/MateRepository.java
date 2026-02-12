package modak.modakmodak.repository;

import modak.modakmodak.entity.Mate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MateRepository extends JpaRepository<Mate, Long> {

    // 양방향 메이트 관계 확인 (user1 <-> user2)
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Mate m " +
            "WHERE (m.user1.id = :userId1 AND m.user2.id = :userId2) " +
            "OR (m.user1.id = :userId2 AND m.user2.id = :userId1)")
    boolean existsMateRelationship(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
