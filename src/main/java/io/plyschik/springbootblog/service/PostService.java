package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.PostCountByYearAndMonthDto;
import io.plyschik.springbootblog.dto.PostDto;
import io.plyschik.springbootblog.dto.YearArchiveEntry;
import io.plyschik.springbootblog.entity.Category;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.entity.Tag;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.exception.CategoryNotFoundException;
import io.plyschik.springbootblog.exception.PostIsNotPublishedException;
import io.plyschik.springbootblog.exception.PostNotFoundException;
import io.plyschik.springbootblog.exception.TagNotFoundException;
import io.plyschik.springbootblog.repository.CategoryRepository;
import io.plyschik.springbootblog.repository.PostRepository;
import io.plyschik.springbootblog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
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

    public Post getPostByIdWithAuthorCategoryAndTags(long id) {
        Post post = postRepository.findWithUserCategoryAndTagsById(id).orElseThrow(PostNotFoundException::new);

        if (!post.isPublished()) {
            throw new PostIsNotPublishedException();
        }

        return post;
    }

    public Page<Post> getPostsWithCategory(Pageable pageable) {
        return postRepository.findAllWithCategoryByOrderByCreatedAtDesc(pageable);
    }

    public Page<Post> getPostsWithAuthorCategoryAndTags(Pageable pageable) {
        return postRepository.findAllWithAuthorCategoryAndTagsByPublishedIsTrueOrderByCreatedAtDesc(pageable);
    }

    public Page<Post> getPostsWithAuthorCategoryAndTagsWhereTitleOrContentContains(String query, Pageable pageable) {
        return postRepository.findByTitleOrContentContainsWithUserCategoryAndTags(query, pageable);
    }

    public Page<Post> getPostsByUserId(long userId, Pageable pageable) {
        return postRepository.findAllWithAuthorCategoryAndTagsByUserIdAndPublishedIsTrueOrderByCreatedAtDesc(
            userId,
            pageable
        );
    }

    public Page<Post> getPostsByCategoryId(long categoryId, Pageable pageable) {
        return postRepository.findAllWithAuthorCategoryAndTagsByCategoryIdAndPublishedIsTrueOrderByCreatedAtDesc(
            categoryId,
            pageable
        );
    }

    public Page<Post> getPostsByTagId(long tagId, Pageable pageable) {
        return postRepository.findAllWithAuthorCategoryAndTagsByIdInAndPublishedIsTrueOrderByCreatedAtDesc(
            postRepository.findPostIdsByTagId(tagId),
            pageable
        );
    }

    public Page<Post> getPostsFromDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return postRepository.findAllWithAuthorCategoryAndTagsByCreatedAtBetweenAndPublishedIsTrueOrderByCreatedAtDesc(
            startDate,
            endDate,
            pageable
        );
    }

    public List<YearArchiveEntry> getPostsArchive() {
        List<PostCountByYearAndMonthDto> postsCountByYearAndMonth = postRepository.findPostsCountByYearAndMonthDto();
        HashMap<Integer, List<PostCountByYearAndMonthDto>> years = postsCountByYearAndMonth.stream()
            .collect(Collectors.groupingBy(
                PostCountByYearAndMonthDto::getYear,
                LinkedHashMap::new,
                Collectors.toList()
            )
        );

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
