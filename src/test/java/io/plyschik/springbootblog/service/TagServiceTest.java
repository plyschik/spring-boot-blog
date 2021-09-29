package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.TagDto;
import io.plyschik.springbootblog.entity.Tag;
import io.plyschik.springbootblog.exception.TagAlreadyExistsException;
import io.plyschik.springbootblog.exception.TagNotFoundException;
import io.plyschik.springbootblog.repository.TagRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {
    @Mock
    private ModelMapper modelMapper;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    public void getTagByIdShouldCallFindByIdMethodFromTagRepositoryAndReturnTagWhenExists() {
        Mockito.when(tagRepository.findById(1L)).thenReturn(Optional.of(new Tag()));

        Tag tag = tagService.getTagById(1);

        Assertions.assertNotNull(tag);
        Mockito.verify(tagRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    public void getTagByIdShouldCallFindByIdMethodFromTagRepositoryAndThrowTagNotFoundExceptionWhenNotExists() {
        Mockito.when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(
            TagNotFoundException.class,
            () -> tagService.getTagById(1)
        );

        Mockito.verify(tagRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    public void getTagsShouldReturnSortedTagList() {
        Mockito.when(tagRepository.findAll(Mockito.any(Sort.class))).thenReturn(new ArrayList<>());

        tagService.getTags(Sort.unsorted());

        Mockito.verify(tagRepository, Mockito.times(1)).findAll(Sort.unsorted());
    }

    @Test
    public void getTagsWithPostsCountShouldReturnSortedTagsListWithPostsCount() {
        Mockito.when(tagRepository.findAllWithPostsCount(Mockito.any(Sort.class))).thenReturn(new ArrayList<>());

        tagService.getTagsWithPostsCount(Sort.unsorted());

        Mockito.verify(tagRepository, Mockito.times(1)).findAllWithPostsCount(Sort.unsorted());
    }

    @Test
    public void getTagsWithPostsCountAndWithQueryShouldReturnSortedTagsListWithPostsCount() {
        Mockito.when(tagRepository.findAllWithPostsCount("Test", Pageable.unpaged()))
            .thenReturn(new PageImpl<>(new ArrayList<>()));

        tagService.getTagsWithPostsCount("Test", Pageable.unpaged());

        Mockito.verify(tagRepository, Mockito.times(1))
            .findAllWithPostsCount("Test", Pageable.unpaged());
    }

    @Test
    public void getTagsWithPostsCountAndWithoutQueryShouldReturnSortedTagsListWithPostsCount() {
        Mockito.when(tagRepository.findAllWithPostsCount(Pageable.unpaged()))
            .thenReturn(new PageImpl<>(new ArrayList<>()));

        tagService.getTagsWithPostsCount("", Pageable.unpaged());

        Mockito.verify(tagRepository, Mockito.times(1))
            .findAllWithPostsCount(Pageable.unpaged());
    }

    @Test
    public void createTagShouldThrowTagAlreadyExistsExceptionWhenTagNameIsNotUnique() {
        TagDto tagDto = new TagDto();
        tagDto.setName("Test");

        Mockito.when(tagRepository.existsByName(tagDto.getName())).thenReturn(true);

        Assertions.assertThrows(
            TagAlreadyExistsException.class,
            () -> tagService.createTag(tagDto)
        );

        Mockito.verify(tagRepository, Mockito.times(1)).existsByName(tagDto.getName());
    }

    @Test
    public void createTagShouldSaveTagWhenTagNameIsUnique() {
        TagDto tagDto = new TagDto();
        tagDto.setName("Test");

        Tag tag = new Tag();

        Mockito.when(tagRepository.existsByName(tagDto.getName())).thenReturn(false);
        Mockito.when(modelMapper.map(tagDto, Tag.class)).thenReturn(tag);
        Mockito.when(tagRepository.save(Mockito.any(Tag.class))).thenAnswer(i -> i.getArguments()[0]);

        tagService.createTag(tagDto);

        Mockito.verify(tagRepository, Mockito.times(1)).existsByName(tagDto.getName());
        Mockito.verify(tagRepository, Mockito.times(1)).save(tag);
    }

    @Test
    public void getTagForEditShouldCallFindByIdMethodFromTagRepositoryAndReturnTagDtoWhenExists() {
        Tag tag = new Tag();

        Mockito.when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        Mockito.when(modelMapper.map(tag, TagDto.class)).thenReturn(new TagDto());

        TagDto tagDto = tagService.getTagForEdit(1);

        Assertions.assertNotNull(tagDto);
        Mockito.verify(tagRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    public void getTagForEditShouldCallFindByIdMethodFromTagRepositoryAndThrowTagNotFoundExceptionWhenNotExists() {
        Mockito.when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(
            TagNotFoundException.class,
            () -> tagService.getTagForEdit(1)
        );

        Mockito.verify(tagRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    public void updateTagShouldThrowTagAlreadyExistsExceptionWhenTagNameIsNotUnique() {
        TagDto tagDto = new TagDto();
        tagDto.setName("Test");

        Mockito.when(tagRepository.existsByName(tagDto.getName())).thenReturn(true);

        Assertions.assertThrows(
            TagAlreadyExistsException.class,
            () -> tagService.updateTag(1, tagDto)
        );

        Mockito.verify(tagRepository, Mockito.times(1)).existsByName(tagDto.getName());
    }

    @Test
    public void updateTagShouldThrowTagNotExistsExceptionWhenTagNotExists() {
        TagDto tagDto = new TagDto();
        tagDto.setName("Test");

        Mockito.when(tagRepository.existsByName(tagDto.getName())).thenReturn(false);
        Mockito.when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(
            TagNotFoundException.class,
            () -> tagService.updateTag(1, tagDto)
        );

        Mockito.verify(tagRepository, Mockito.times(1)).existsByName(tagDto.getName());
        Mockito.verify(tagRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    public void updateCategoryUpdateCategoryWhenNameIsUnique() {
        TagDto tagDto = new TagDto();
        tagDto.setName("Test");

        Tag tag = new Tag();
        tag.setName("Test");

        Mockito.when(tagRepository.existsByName(tagDto.getName())).thenReturn(false);
        Mockito.when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        Mockito.when(tagRepository.save(Mockito.any(Tag.class))).thenAnswer(i -> i.getArguments()[0]);

        tagService.updateTag(1, tagDto);

        Mockito.verify(tagRepository, Mockito.times(1)).existsByName(tagDto.getName());
        Mockito.verify(tagRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(tagRepository, Mockito.times(1)).save(tag);
    }

    @Test
    public void deleteTagShouldDeleteTagById() {
        tagService.deleteTag(1);

        Mockito.verify(tagRepository, Mockito.times(1)).deleteById(1L);
    }
}
