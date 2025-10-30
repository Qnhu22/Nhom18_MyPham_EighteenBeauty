package com.oneshop.repository;

import com.oneshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    List<User> findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(String username, String fullName);
    long countByActive(boolean active);

    long countByRoles_RoleName(String roleName);

    @Query("""
        SELECT MONTH(u.createAt) AS month, COUNT(u) AS count
        FROM User u
        WHERE YEAR(u.createAt) = YEAR(CURRENT_DATE)
        GROUP BY MONTH(u.createAt)
    """)
    List<Object[]> countUsersByMonthThisYear();
	Object findByUsernameFetchRoles(String usernameOrEmail);
	Object findByRoles(String string);

}
