package com.oneshop.repository;

import com.oneshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
	//Object findByUsernameFetchRoles(String usernameOrEmail);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = :roleName")
    List<User> findByRoles(@Param("roleName") String roleName);

	
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameFetchRoles(@Param("username") String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailFetchRoles(@Param("email") String email);
}
