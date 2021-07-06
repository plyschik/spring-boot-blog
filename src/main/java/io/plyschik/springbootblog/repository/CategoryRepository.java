package io.plyschik.springbootblog.repository;

import io.plyschik.springbootblog.dto.CategoryWithPostsCount;
import io.plyschik.springbootblog.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByOrderByName();

    Page<Category> findAllByOrderByIdDesc(Pageable pageable);

    boolean existsByName(String name);

    @Query("SELECT new io.plyschik.springbootblog.dto.CategoryWithPostsCount(c.id, c.name, COUNT(p.id) AS postsCount) " +
           "FROM Category c " +
           "LEFT JOIN c.posts p " +
           "GROUP BY c.id " +
           "ORDER BY postsCount DESC")
    List<CategoryWithPostsCount> findCategoriesWithPostsCountOrderedByPostsCount(Pageable pageable);
}
