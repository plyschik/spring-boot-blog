package io.plyschik.springbootblog.repository;

import io.plyschik.springbootblog.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Override
    @EntityGraph(attributePaths = {"user"})
    Optional<Comment> findById(Long id);

    @EntityGraph(attributePaths = {"user"})
    Page<Comment> findAllByPostId(long postId, Pageable pageable);
}
