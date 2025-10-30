package com.oneshop.repository;

import com.oneshop.dto.UserLogCountProjection;
import com.oneshop.entity.UserActionLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserActionLogRepository extends JpaRepository<UserActionLog, Long> {
    List<UserActionLog> findByTargetUserUserId(Long userId);
    List<UserActionLog> findByTargetUser_UserIdOrderByActionTimeDesc(Long userId);
    
    @Query("""
            SELECT u.userId AS userId, u.username AS username, u.email AS email, COUNT(l) AS logCount
            FROM UserActionLog l
            JOIN l.targetUser u
            GROUP BY u.userId, u.username, u.email
            ORDER BY COUNT(l) DESC
            LIMIT 5
        """)
	List<UserLogCountProjection> findTop5UsersByLogCount();
}