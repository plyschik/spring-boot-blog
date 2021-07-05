package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.TagDto;
import io.plyschik.springbootblog.entity.Tag;
import io.plyschik.springbootblog.exception.TagAlreadyExistsException;
import io.plyschik.springbootblog.exception.TagNotFoundException;
import io.plyschik.springbootblog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    public Tag getById(long id) {
        return tagRepository.findById(id).orElseThrow(TagNotFoundException::new);
    }

    public List<Tag> getAll() {
        return tagRepository.findAll();
    }

    public Page<Tag> getPaginatedTags(Pageable pageable) {
        return tagRepository.findAllByOrderByIdDesc(pageable);
    }

    public void createTag(TagDto tagDto) throws TagAlreadyExistsException {
        if (tagRepository.existsByName(tagDto.getName())) {
            throw new TagAlreadyExistsException();
        }

        Tag tag = new Tag();
        tag.setName(tagDto.getName());

        tagRepository.save(tag);
    }

    public TagDto getTagForEdit(long id) throws TagNotFoundException {
        Tag tag = tagRepository.findById(id).orElseThrow(TagNotFoundException::new);

        TagDto tagDto = new TagDto();
        tagDto.setName(tag.getName());

        return tagDto;
    }

    public void updateTag(long id, TagDto tagDto) throws TagNotFoundException, TagAlreadyExistsException {
        if (tagRepository.existsByName(tagDto.getName())) {
            throw new TagAlreadyExistsException();
        }

        Tag tag = tagRepository.findById(id).orElseThrow(TagNotFoundException::new);
        tag.setName(tagDto.getName());

        tagRepository.save(tag);
    }

    public void deleteById(long id) throws TagNotFoundException {
        Tag tag = tagRepository.findById(id).orElseThrow(TagNotFoundException::new);

        tagRepository.delete(tag);
    }
}
