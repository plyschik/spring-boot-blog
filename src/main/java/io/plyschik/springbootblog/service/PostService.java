package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.PostDto;
import io.plyschik.springbootblog.entity.Category;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.entity.Tag;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.exception.CategoryNotFound;
import io.plyschik.springbootblog.exception.PostNotFound;
import io.plyschik.springbootblog.exception.TagNotFound;
import io.plyschik.springbootblog.repository.CategoryRepository;
import io.plyschik.springbootblog.repository.PostRepository;
import io.plyschik.springbootblog.repository.TagRepository;
import io.plyschik.springbootblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public Optional<Post> getById(long id) {
        return postRepository.findById(id);
    }

    public Optional<Post> getSinglePostWithAuthorCategoryAndTags(Long id) {
        return postRepository.findByIdWithUserCategoryAndTags(id);
    }

    public Page<Post> getPostsByAuthorId(Long authorId, Pageable pageable) {
        return postRepository.findAllByUserIdOrderByCreatedAtDesc(authorId, pageable);
    }

    public Page<Post> getPostsByCategoryId(Long categoryId, Pageable pageable) {
        return postRepository.findAllByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
    }

    public Page<Post> getPostsByTagId(Long tagId, Pageable pageable) {
        return postRepository.findAllByIdInOrderByCreatedAtDesc(postRepository.findPostIdsByTagId(tagId), pageable);
    }

    public Page<Post> getPaginatedPostsWithAuthorCategoryAndTags(Pageable pageable) {
        return postRepository.findAllWithUserCategoryAndTags(pageable);
    }

    public Page<Post> getPaginatedPosts(Pageable pageable) {
        return postRepository.findAllByOrderByIdDesc(pageable);
    }

    public void createPost(PostDto postDto, User user) throws CategoryNotFound, TagNotFound {
        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setCreatedAt(new Date());
        post.setUser(user);

        if (postDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(postDto.getCategoryId())
                .orElseThrow(CategoryNotFound::new);

            post.setCategory(category);
        }

        for (Long tagId : postDto.getTagIds()) {
            Tag tag = tagRepository.findById(tagId).orElseThrow(TagNotFound::new);

            post.addTag(tag);
        }

        postRepository.save(post);
    }

    public PostDto getPostForEdit(long id) throws PostNotFound {
        Post post = postRepository.findById(id).orElseThrow(PostNotFound::new);

        PostDto postDto = new PostDto();
        postDto.setTitle(post.getTitle());
        postDto.setContent(post.getContent());

        if (post.getCategory() != null) {
            postDto.setCategoryId(post.getCategory().getId());
        }

        post.getTags().forEach(tag -> postDto.getTagIds().add(tag.getId()));

        return postDto;
    }

    public void updatePost(long id, PostDto postDto) throws PostNotFound, CategoryNotFound {
        Post post = postRepository.findById(id).orElseThrow(PostNotFound::new);
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());

        if (postDto.getCategoryId() == null) {
            post.setCategory(null);
        } else {
            Category category = categoryRepository.findById(postDto.getCategoryId()).orElseThrow(CategoryNotFound::new);
            post.setCategory(category);
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

    public void delete(long id) throws PostNotFound {
        Post post = postRepository.findById(id).orElseThrow(PostNotFound::new);

        postRepository.delete(post);
    }
}
