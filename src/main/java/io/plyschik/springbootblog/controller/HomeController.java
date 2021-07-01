package io.plyschik.springbootblog.controller;

import io.plyschik.springbootblog.service.CategoryService;
import io.plyschik.springbootblog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

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
}
