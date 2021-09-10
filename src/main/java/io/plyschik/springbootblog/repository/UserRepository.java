package io.plyschik.springbootblog.repository;

import io.plyschik.springbootblog.dto.UserWithPostsCount;
import io.plyschik.springbootblog.entity.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT new io.plyschik.springbootblog.dto.UserWithPostsCount(u.id, CONCAT(u.firstName, ' ', u.lastName) AS fullName, COUNT(p.id) AS postsCount) " +
           "FROM User u " +
           "LEFT JOIN u.posts p " +
           "WHERE u.role = 2 " +
           "GROUP BY u.id")
    List<UserWithPostsCount> findAllWithPostsCount(Sort sort);
}
