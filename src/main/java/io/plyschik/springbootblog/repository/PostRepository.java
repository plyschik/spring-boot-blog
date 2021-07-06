package io.plyschik.springbootblog.repository;

import io.plyschik.springbootblog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
    Page<Post> findAllWithAuthorCategoryAndTagsByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category", "tags"})
    Page<Post> findAllWithAuthorCategoryAndTagsByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category", "tags"})
    Page<Post> findAllWithAuthorCategoryAndTagsByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category", "tags"})
    Page<Post> findAllWithAuthorCategoryAndTagsByIdInOrderByCreatedAtDesc(List<Long> postIds, Pageable pageable);

    @Query("SELECT p.id " +
           "FROM Post p " +
           "JOIN p.tags t " +
           "WHERE t.id = ?1")
    List<Long> findPostIdsByTagId(Long tagId);
}
