package io.plyschik.springbootblog.repository;

import io.plyschik.springbootblog.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findAllByOrderByName();

    Page<Tag> findAllByOrderByIdDesc(Pageable pageable);

    boolean existsByName(String name);
}
