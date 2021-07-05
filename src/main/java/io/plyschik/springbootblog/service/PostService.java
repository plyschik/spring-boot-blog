package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.PostDto;
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
import io.plyschik.springbootblog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public Post getById(long id) {
        return postRepository.findById(id).orElseThrow(PostNotFoundException::new);
    }

    public Post getSinglePostWithAuthorCategoryAndTags(Long id) {
        return postRepository.findByIdWithUserCategoryAndTags(id).orElseThrow(PostNotFoundException::new);
    }

    public Page<Post> getPaginatedPosts(Pageable pageable) {
        return postRepository.findAllByOrderByIdDesc(pageable);
    }

    public Page<Post> getPaginatedPostsWithAuthorCategoryAndTags(Pageable pageable) {
        return postRepository.findAllWithUserCategoryAndTags(pageable);
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

    public void createPost(PostDto postDto, User user) throws CategoryNotFoundException, TagNotFoundException {
        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setCreatedAt(new Date());
        post.setUser(user);

        if (postDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(postDto.getCategoryId()).orElseThrow(CategoryNotFoundException::new);

            post.setCategory(category);
        }

        for (Long tagId : postDto.getTagIds()) {
            Tag tag = tagRepository.findById(tagId).orElseThrow(TagNotFoundException::new);

            post.addTag(tag);
        }

        postRepository.save(post);
    }

    public PostDto getPostForEdit(long id) throws PostNotFoundException {
        Post post = postRepository.findById(id).orElseThrow(PostNotFoundException::new);

        PostDto postDto = new PostDto();
        postDto.setTitle(post.getTitle());
        postDto.setContent(post.getContent());

        if (post.getCategory() != null) {
            postDto.setCategoryId(post.getCategory().getId());
        }

        post.getTags().forEach(tag -> postDto.getTagIds().add(tag.getId()));

        return postDto;
    }

    public void updatePost(long id, PostDto postDto) throws PostNotFoundException, CategoryNotFoundException {
        Post post = postRepository.findById(id).orElseThrow(PostNotFoundException::new);
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());

        if (postDto.getCategoryId() == null) {
            post.setCategory(null);
        } else {
            Category category = categoryRepository.findById(postDto.getCategoryId()).orElseThrow(CategoryNotFoundException::new);
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

    public void delete(long id) throws PostNotFoundException {
        Post post = postRepository.findById(id).orElseThrow(PostNotFoundException::new);

        postRepository.delete(post);
    }
}
