package io.plyschik.springbootblog.repository;

import io.plyschik.springbootblog.entity.Comment;
import io.plyschik.springbootblog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = {"user"})
    Page<Comment> findAllByPostOrderByCreatedAtDesc(Post post, Pageable pageable);
}
