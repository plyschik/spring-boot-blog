package io.plyschik.springbootblog.controller;

import io.plyschik.springbootblog.dto.CategoryWithPostsCount;
import io.plyschik.springbootblog.dto.YearArchiveEntry;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.service.CategoryService;
import io.plyschik.springbootblog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {
    private final PostService postService;
    private final CategoryService categoryService;

    @GetMapping("/search")
    private ModelAndView search(
        @RequestParam String query,
        @RequestParam(required = false, defaultValue = "0") int page
    ) {
        if (query.isBlank()) {
            return new ModelAndView("redirect:/");
        }

        Page<Post> posts = postService.getPostsWithAuthorCategoryAndTagsWhereTitleOrContentContains(
            query,
            PageRequest.of(page, 5, Sort.by(Sort.Order.desc("id")))
        );
        List<CategoryWithPostsCount> categories = categoryService.getTop5CategoriesWithPostsCount();
        List<YearArchiveEntry> archive = postService.getPostsArchive();

        return new ModelAndView("blog/search")
            .addObject("posts", posts)
            .addObject("categories", categories)
            .addObject("archive", archive);
    }
}
