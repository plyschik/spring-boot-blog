package io.plyschik.springbootblog.repository;

import io.plyschik.springbootblog.dto.PostCountByYearAndMonthDto;
import io.plyschik.springbootblog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    Optional<Post> findWithUserCategoryAndTagsById(Long id);

    @EntityGraph(attributePaths = {"category"})
    Page<Post> findAllWithCategoryByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category", "tags"})
    Page<Post> findAllWithAuthorCategoryAndTagsByPublishedIsTrueOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category", "tags"})
    Page<Post> findAllWithAuthorCategoryAndTagsByUserIdAndPublishedIsTrueOrderByCreatedAtDesc(
        Long userId,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"user", "category", "tags"})
    Page<Post> findAllWithAuthorCategoryAndTagsByCategoryIdAndPublishedIsTrueOrderByCreatedAtDesc(
        Long categoryId,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"user", "category", "tags"})
    Page<Post> findAllWithAuthorCategoryAndTagsByIdInAndPublishedIsTrueOrderByCreatedAtDesc(
        List<Long> postIds,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"user", "category", "tags"})
    Page<Post> findAllWithAuthorCategoryAndTagsByCreatedAtBetweenAndPublishedIsTrueOrderByCreatedAtDesc(
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );

    @Query("SELECT p.id " +
           "FROM Post p " +
           "JOIN p.tags t " +
           "WHERE t.id = ?1")
    List<Long> findPostIdsByTagId(Long tagId);

    @Query(name = "count_posts_by_year_and_month", nativeQuery = true)
    List<PostCountByYearAndMonthDto> findPostsCountByYearAndMonthDto();
}
