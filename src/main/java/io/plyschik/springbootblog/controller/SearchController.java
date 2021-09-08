package io.plyschik.springbootblog.controller;

import io.plyschik.springbootblog.dto.CategoryWithPostsCount;
import io.plyschik.springbootblog.dto.YearArchiveEntry;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.service.CategoryService;
import io.plyschik.springbootblog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
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
        @PageableDefault(size = 5) @SortDefault(value = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        if (query.isBlank()) {
            return new ModelAndView("redirect:/");
        }

        Page<Post> posts = postService.getPostsWithAuthorCategoryAndTagsWhereTitleOrContentContains(query, pageable);
        List<CategoryWithPostsCount> categories = categoryService.getCategoriesWithPostsCount();
        List<YearArchiveEntry> archive = postService.getPostsArchive();

        ModelAndView modelAndView = new ModelAndView("blog/search");
        modelAndView.addObject("posts", posts);
        modelAndView.addObject("categories", categories);
        modelAndView.addObject("archive", archive);

        return modelAndView;
    }
}
