package io.plyschik.springbootblog.service;

import io.plyschik.springbootblog.dto.PostDto;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.exception.PostNotFound;
import io.plyschik.springbootblog.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public PostDto getPostForEdit(long id) throws PostNotFound {
        Post post = postRepository.findById(id).orElseThrow(PostNotFound::new);

        PostDto postDto = new PostDto();
        postDto.setTitle(post.getTitle());
        postDto.setContent(post.getContent());

        return postDto;
    }

    public void updatePost(long id, PostDto postDto) throws PostNotFound {
        Post post = postRepository.findById(id).orElseThrow(PostNotFound::new);
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());

        postRepository.save(post);
    }

    public Page<Post> list(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public void createPost(PostDto postDto, User user) {
        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setCreatedAt(new Date());
        post.setUser(user);

        postRepository.save(post);
    }
}
