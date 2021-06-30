package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.entity.Tag;
import io.plyschik.springbootblog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    public Page<Tag> getPaginatedTags(Pageable pageable) {
        return tagRepository.findAllByOrderByIdDesc(pageable);
    }
}
