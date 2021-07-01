package io.plyschik.springbootblog.controller;

import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.service.CategoryService;
import io.plyschik.springbootblog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
class HomeController {
    private final PostService postService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public ModelAndView index(
        @RequestParam(defaultValue = "0") int page,
        @Value("${pagination.post.blog}") int itemsPerPage
    ) {
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject(
            "posts",
            postService.getPaginatedPostsWithAuthorCategoryAndTags(PageRequest.of(page, itemsPerPage))
        );
        modelAndView.addObject("categories", categoryService.getCategoriesWithPostsCount());

        return modelAndView;
    }

    @GetMapping("/posts/{id}")
    public ModelAndView singlePost(@PathVariable Long id) {
        Optional<Post> post = postService.getSinglePostWithAuthorCategoryAndTags(id);

        if (post.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return new ModelAndView("single", "post", post.get());
    }
}
