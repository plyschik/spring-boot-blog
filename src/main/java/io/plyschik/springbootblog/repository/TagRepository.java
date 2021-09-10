package io.plyschik.springbootblog.repository;

import io.plyschik.springbootblog.dto.TagWithPostsCount;
import io.plyschik.springbootblog.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findAllByOrderByName();

    boolean existsByName(String name);

    @Query("SELECT new io.plyschik.springbootblog.dto.TagWithPostsCount(t.id, t.name, COUNT(p.id) AS postsCount) " +
           "FROM Tag t " +
           "LEFT JOIN t.posts p " +
           "GROUP BY t.id")
    List<TagWithPostsCount> findAllWithPostsCount(Sort sort);

    @Query("SELECT new io.plyschik.springbootblog.dto.TagWithPostsCount(t.id, t.name, COUNT(p.id) AS postsCount) " +
           "FROM Tag t " +
           "LEFT JOIN t.posts p " +
           "WHERE t.name LIKE %:name% " +
           "GROUP BY t.id")
    Page<TagWithPostsCount> findAllWithPostsCountWhereNameLike(@Param("name") String name, Pageable pageable);
}
