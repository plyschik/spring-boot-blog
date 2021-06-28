package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.PostDto;
import io.plyschik.springbootblog.entity.Category;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.exception.CategoryNotFound;
import io.plyschik.springbootblog.exception.PostNotFound;
import io.plyschik.springbootblog.repository.CategoryRepository;
import io.plyschik.springbootblog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    public Optional<Post> getById(long id) {
        return postRepository.findById(id);
    }

    public Page<Post> getPaginatedPosts(Pageable pageable) {
        return postRepository.findAllByOrderByIdDesc(pageable);
    }

    public void createPost(PostDto postDto, User user) throws CategoryNotFound {
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

        postRepository.save(post);
    }

    public void delete(long id) throws PostNotFound {
        Post post = postRepository.findById(id).orElseThrow(PostNotFound::new);

        postRepository.delete(post);
    }
}
