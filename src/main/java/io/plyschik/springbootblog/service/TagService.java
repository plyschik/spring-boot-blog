package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.TagDto;
import io.plyschik.springbootblog.entity.Tag;
import io.plyschik.springbootblog.exception.TagAlreadyExists;
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

    public void createTag(TagDto tagDto) throws TagAlreadyExists {
        if (tagRepository.existsByName(tagDto.getName())) {
            throw new TagAlreadyExists();
        }

        Tag tag = new Tag();
        tag.setName(tagDto.getName());

        tagRepository.save(tag);
    }
}
