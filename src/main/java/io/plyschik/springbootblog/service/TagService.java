package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.TagDto;
import io.plyschik.springbootblog.dto.TagWithPostsCount;
import io.plyschik.springbootblog.entity.Tag;
import io.plyschik.springbootblog.exception.TagAlreadyExistsException;
import io.plyschik.springbootblog.exception.TagNotFoundException;
import io.plyschik.springbootblog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {
    private final ModelMapper modelMapper;
    private final TagRepository tagRepository;

    public Tag getTagById(long id) {
        return tagRepository.findById(id).orElseThrow(TagNotFoundException::new);
    }

    public List<Tag> getTags(Sort sort) {
        return tagRepository.findAll(sort);
    }

    public List<TagWithPostsCount> getTagsWithPostsCount(Sort sort) {
        return tagRepository.findAllWithPostsCount(sort);
    }

    public Page<TagWithPostsCount> getTagsWithPostsCount(String query, Pageable pageable) {
        if (!query.isBlank()) {
            return tagRepository.findAllWithPostsCount(query, pageable);
        }

        return tagRepository.findAllWithPostsCount(pageable);
    }

    public void createTag(TagDto tagDto) throws TagAlreadyExistsException {
        if (tagRepository.existsByName(tagDto.getName())) {
            throw new TagAlreadyExistsException();
        }

        Tag tag = modelMapper.map(tagDto, Tag.class);

        tagRepository.save(tag);
    }

    public TagDto getTagForEdit(long id) throws TagNotFoundException {
        Tag tag = tagRepository.findById(id).orElseThrow(TagNotFoundException::new);

        return modelMapper.map(tag, TagDto.class);
    }

    public void updateTag(long id, TagDto tagDto) throws TagNotFoundException, TagAlreadyExistsException {
        if (tagRepository.existsByName(tagDto.getName())) {
            throw new TagAlreadyExistsException();
        }

        Tag tag = tagRepository.findById(id).orElseThrow(TagNotFoundException::new);
        modelMapper.map(tagDto, tag);

        tagRepository.save(tag);
    }

    public void deleteTag(long id) {
        tagRepository.deleteById(id);
    }
}
