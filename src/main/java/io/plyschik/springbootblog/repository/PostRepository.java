package io.plyschik.springbootblog.repository;

import io.plyschik.springbootblog.dto.PostCountByYearAndMonthDto;
import io.plyschik.springbootblog.dto.PostWithRelationshipsCount;
import io.plyschik.springbootblog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = {"category", "tags"})
    Optional<Post> findWithCategoryAndTagsById(Long id);

    @EntityGraph(attributePaths = {"user", "category", "tags"})
    Optional<Post> findWithUserCategoryAndTagsByIdAndPublishedIsTrue(long id);

    @Query("SELECT new io.plyschik.springbootblog.dto.PostWithRelationshipsCount(p.id, p.title, p.published, CONCAT(u.firstName, ' ', u.lastName) AS author, ca.name AS category, COUNT(DISTINCT t.id) AS tagsCount, COUNT(DISTINCT co.id) AS commentsCount, p.createdAt) " +
           "FROM Post p " +
           "LEFT JOIN p.user u " +
           "LEFT JOIN p.category ca " +
           "LEFT JOIN p.tags t " +
           "LEFT JOIN p.comments co " +
           "WHERE p.title LIKE %:query% " +
           "GROUP BY p.id")
    Page<PostWithRelationshipsCount> findAllByTitleContains(String query, Pageable pageable);

    @Query("SELECT new io.plyschik.springbootblog.dto.PostWithRelationshipsCount(p.id, p.title, p.published, CONCAT(u.firstName, ' ', u.lastName) AS author, ca.name AS category, COUNT(DISTINCT t.id) AS tagsCount, COUNT(DISTINCT co.id) AS commentsCount, p.createdAt) " +
            "FROM Post p " +
            "LEFT JOIN p.user u " +
            "LEFT JOIN p.category ca " +
            "LEFT JOIN p.tags t " +
            "LEFT JOIN p.comments co " +
            "WHERE p.title LIKE %:query% AND u.id = :userId " +
            "GROUP BY p.id")
    Page<PostWithRelationshipsCount> findAllByTitleContainsAndUserIdEquals(
        String query,
        Long userId,
        Pageable pageable
    );

    @Query("SELECT new io.plyschik.springbootblog.dto.PostWithRelationshipsCount(p.id, p.title, p.published, CONCAT(u.firstName, ' ', u.lastName) AS author, ca.name AS category, COUNT(DISTINCT t.id) AS tagsCount, COUNT(DISTINCT co.id) AS commentsCount, p.createdAt) " +
            "FROM Post p " +
            "LEFT JOIN p.user u " +
            "LEFT JOIN p.category ca " +
            "LEFT JOIN p.tags t " +
            "LEFT JOIN p.comments co " +
            "WHERE p.title LIKE %:query% AND ca.id = :categoryId " +
            "GROUP BY p.id")
    Page<PostWithRelationshipsCount> findAllByTitleContainsAndCategoryIdEquals(
        String query,
        Long categoryId,
        Pageable pageable
    );

    @Query("SELECT new io.plyschik.springbootblog.dto.PostWithRelationshipsCount(p.id, p.title, p.published, CONCAT(u.firstName, ' ', u.lastName) AS author, ca.name AS category, COUNT(DISTINCT t.id) AS tagsCount, COUNT(DISTINCT co.id) AS commentsCount, p.createdAt) " +
            "FROM Post p " +
            "LEFT JOIN p.user u " +
            "LEFT JOIN p.category ca " +
            "LEFT JOIN p.tags t " +
            "LEFT JOIN p.comments co " +
            "WHERE p.title LIKE %:query% AND t.id = :tagId " +
            "GROUP BY p.id")
    Page<PostWithRelationshipsCount> findAllByTitleContainsAndTagIdEquals(
        String query,
        Long tagId,
        Pageable pageable
    );

    @Query("SELECT p.id " +
           "FROM Post p " +
           "WHERE p.published = true")
    Page<Long> findAllPublishedIds(Pageable pageable);

    @Query("SELECT p.id " +
        "FROM Post p " +
        "LEFT JOIN p.user u " +
        "WHERE p.published = true AND u.id = :id")
    Page<Long> findAllPublishedByUserIds(long id, Pageable pageable);

    @Query("SELECT p.id " +
        "FROM Post p " +
        "LEFT JOIN p.category c " +
        "WHERE p.published = true AND c.id = :id")
    Page<Long> findAllPublishedByCategoryIds(long id, Pageable pageable);

    @Query("SELECT p.id " +
        "FROM Post p " +
        "LEFT JOIN p.tags t " +
        "WHERE p.published = true AND t.id = :id")
    Page<Long> findAllPublishedByTagIds(long id, Pageable pageable);

    @Query("SELECT p.id " +
        "FROM Post p " +
        "WHERE p.published = true AND p.createdAt BETWEEN :startDate AND :endDate")
    Page<Long> findAllPublishedFromDateRangeIds(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT p.id " +
        "FROM Post p " +
        "WHERE p.published = TRUE AND (p.title LIKE %:query% OR p.content LIKE %:query%)")
    Page<Long> findAllPublishedWhereTitleOrContentContainsIds(String query, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category", "tags"})
    List<Post> findAllPublishedWithAuthorCategoryAndTagsByIdIn(List<Long> ids, Sort sort);

    @Query(name = "Post.countPostsByYearAndMonth", nativeQuery = true)
    List<PostCountByYearAndMonthDto> findPostsCountByYearAndMonthDto();
}
