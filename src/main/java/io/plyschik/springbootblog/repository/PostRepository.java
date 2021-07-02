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
    @EntityGraph(attributePaths = {"category"})
    Page<Post> findAllByOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category", "tags"})
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    Page<Post> findAllWithUserCategoryAndTags(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category", "tags"})
    @Query("SELECT p FROM Post p WHERE p.id = ?1")
    Optional<Post> findByIdWithUserCategoryAndTags(Long id);

    @EntityGraph(attributePaths = {"user", "category", "tags"})
    Page<Post> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category", "tags"})
    Page<Post> findAllByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category", "tags"})
    Page<Post> findAllByIdInOrderByCreatedAtDesc(List<Long> ids, Pageable pageable);

    @Query("SELECT p.id " +
           "FROM Post p " +
           "JOIN p.tags t " +
           "WHERE t.id = ?1")
    List<Long> findPostIdsByTagId(Long tagId);
}
