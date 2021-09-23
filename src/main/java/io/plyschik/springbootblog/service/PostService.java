package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.PostCountByYearAndMonthDto;
import io.plyschik.springbootblog.dto.PostDto;
import io.plyschik.springbootblog.dto.PostWithRelationshipsCount;
import io.plyschik.springbootblog.dto.YearArchiveEntry;
import io.plyschik.springbootblog.entity.Category;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.entity.Tag;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.exception.CategoryNotFoundException;
import io.plyschik.springbootblog.exception.PostNotFoundException;
import io.plyschik.springbootblog.exception.TagNotFoundException;
import io.plyschik.springbootblog.repository.CategoryRepository;
import io.plyschik.springbootblog.repository.PostRepository;
import io.plyschik.springbootblog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final ModelMapper modelMapper;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public boolean existsById(long id) {
        return postRepository.existsById(id);
    }

    public Post getPostById(long id) {
        return postRepository.findById(id).orElseThrow(PostNotFoundException::new);
    }

    public Post getPostWithAuthorCategoryAndTags(long id) {
        return postRepository.findWithUserCategoryAndTagsByIdAndPublishedIsTrue(id)
            .orElseThrow(PostNotFoundException::new);
    }

    public Page<PostWithRelationshipsCount> getPostsWithCategory(
        String query,
        Long userId,
        Long categoryId,
        Long tagId,
        Pageable pageable
    ) {
        if (userId != null) {
            return postRepository.findAllByTitleContainsAndUserIdEquals(query, userId, pageable);
        }

        if (categoryId != null) {
            return postRepository.findAllByTitleContainsAndCategoryIdEquals(query, categoryId, pageable);
        }

        if (tagId != null) {
            return postRepository.findAllByTitleContainsAndTagIdEquals(query, tagId, pageable);
        }

        return postRepository.findAllByTitleContains(query, pageable);
    }

    public Page<Post> getPostsWithAuthorCategoryAndTags(Pageable pageable) {
        Page<Long> publishedPostIds = postRepository.findAllPublishedIds(pageable);
        List<Post> postsWithAuthorCategoryAndTags = postRepository.findAllPublishedWithAuthorCategoryAndTagsByIdIn(
            publishedPostIds.getContent(),
            pageable.getSort()
        );

        return new PageImpl<>(postsWithAuthorCategoryAndTags, pageable, publishedPostIds.getTotalElements());
    }

    public Page<Post> getPostsWithAuthorCategoryAndTagsWhereTitleOrContentContains(String query, Pageable pageable) {
        return postRepository.findByTitleOrContentContainsWithUserCategoryAndTags(query, pageable);
    }

    public Page<Post> getPostsWithAuthorCategoryAndTagsByUserId(long userId, Pageable pageable) {
        Page<Long> publishedPostIds = postRepository.findAllPublishedByUserIds(userId, pageable);
        List<Post> postsWithAuthorCategoryAndTags = postRepository.findAllPublishedWithAuthorCategoryAndTagsByIdIn(
            publishedPostIds.getContent(),
            pageable.getSort()
        );

        return new PageImpl<>(postsWithAuthorCategoryAndTags, pageable, publishedPostIds.getTotalElements());
    }

    public Page<Post> getPostsWithAuthorCategoryAndTagsByCategoryId(long categoryId, Pageable pageable) {
        Page<Long> publishedPostIds = postRepository.findAllPublishedByCategoryIds(categoryId, pageable);
        List<Post> postsWithAuthorCategoryAndTags = postRepository.findAllPublishedWithAuthorCategoryAndTagsByIdIn(
            publishedPostIds.getContent(),
            pageable.getSort()
        );

        return new PageImpl<>(postsWithAuthorCategoryAndTags, pageable, publishedPostIds.getTotalElements());
    }

    public Page<Post> getPostsWithAuthorCategoryAndTagsByTagId(long tagId, Pageable pageable) {
        Page<Long> publishedPostIds = postRepository.findAllPublishedByTagIds(tagId, pageable);
        List<Post> postsWithAuthorCategoryAndTags = postRepository.findAllPublishedWithAuthorCategoryAndTagsByIdIn(
            publishedPostIds.getContent(),
            pageable.getSort()
        );

        return new PageImpl<>(postsWithAuthorCategoryAndTags, pageable, publishedPostIds.getTotalElements());
    }

    public Page<Post> getPostsWithAuthorCategoryAndTagsFromDateRange(
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    ) {
        Page<Long> publishedPostIds = postRepository.findAllPublishedFromDateRangeIds(
            startDate,
            endDate,
            pageable
        );
        List<Post> postsWithAuthorCategoryAndTags = postRepository.findAllPublishedWithAuthorCategoryAndTagsByIdIn(
            publishedPostIds.getContent(),
            pageable.getSort()
        );

        return new PageImpl<>(postsWithAuthorCategoryAndTags, pageable, publishedPostIds.getTotalElements());
    }

    public List<YearArchiveEntry> getPostsArchive() {
        List<PostCountByYearAndMonthDto> postsCountByYearAndMonth = postRepository.findPostsCountByYearAndMonthDto();
        HashMap<Integer, List<PostCountByYearAndMonthDto>> years = postsCountByYearAndMonth.stream()
            .collect(Collectors.groupingBy(
                PostCountByYearAndMonthDto::getYear,
                LinkedHashMap::new,
                Collectors.toList()
            ));

        List<YearArchiveEntry> yearArchiveEntries = new ArrayList<>();
        for (Map.Entry<Integer, List<PostCountByYearAndMonthDto>> entry: years.entrySet()) {
            Integer postsCount = entry.getValue().stream()
                .map(PostCountByYearAndMonthDto::getCount)
                .mapToInt(Integer::intValue)
                .sum();

            List<YearArchiveEntry.Month> monthList = entry.getValue().stream()
                .map(item -> new YearArchiveEntry.Month(item.getMonth(), item.getCount()))
                .collect(Collectors.toList());

            yearArchiveEntries.add(new YearArchiveEntry(entry.getKey(), postsCount, monthList));
        }

        return yearArchiveEntries;
    }

    public void createPost(PostDto postDto, User user) throws CategoryNotFoundException, TagNotFoundException {
        Post post = modelMapper.map(postDto, Post.class);
        post.setUser(user);
        post.setContent(MarkdownToHTMLParser.parse(postDto.getContentRaw()));

        if (postDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(postDto.getCategoryId())
                .orElseThrow(CategoryNotFoundException::new);

            post.setCategory(category);
        }

        for (Long tagId: postDto.getTagIds()) {
            Tag tag = tagRepository.findById(tagId).orElseThrow(TagNotFoundException::new);

            post.addTag(tag);
        }

        postRepository.save(post);
    }

    public PostDto getPostByIdForEdit(long id) throws PostNotFoundException {
        Post post = postRepository.findWithCategoryAndTagsById(id).orElseThrow(PostNotFoundException::new);
        PostDto postDto = modelMapper.map(post, PostDto.class);;

        if (post.getCategory() != null) {
            postDto.setCategoryId(post.getCategory().getId());
        }

        post.getTags().forEach(tag -> postDto.getTagIds().add(tag.getId()));

        return postDto;
    }

    public void updatePost(long id, PostDto postDto) throws PostNotFoundException, CategoryNotFoundException {
        Post post = postRepository.findWithCategoryAndTagsById(id).orElseThrow(PostNotFoundException::new);
        modelMapper.map(postDto, post);
        post.setContent(MarkdownToHTMLParser.parse(postDto.getContentRaw()));

        if (postDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(postDto.getCategoryId())
                .orElseThrow(CategoryNotFoundException::new);

            post.setCategory(category);
        } else {
            post.setCategory(null);
        }

        Set<Tag> currentTags = post.getTags();
        List<Tag> selectedTags = tagRepository.findAllById(postDto.getTagIds());

        List<Tag> tagsToAdd = selectedTags.stream()
            .filter(tag -> !currentTags.contains(tag))
            .collect(Collectors.toList());

        for (Tag tag: tagsToAdd) {
            post.addTag(tag);
        }

        List<Tag> tagsToRemove = currentTags.stream()
            .filter(tag -> !selectedTags.contains(tag))
            .collect(Collectors.toList());

        for (Tag tag: tagsToRemove) {
            post.removeTag(tag);
        }

        postRepository.save(post);
    }

    public void deletePost(long id) {
        postRepository.deleteById(id);
    }
}
