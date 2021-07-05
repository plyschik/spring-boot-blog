package io.plyschik.springbootblog.controller.dashboard;

import io.plyschik.springbootblog.dto.Alert;
import io.plyschik.springbootblog.dto.PostDto;
import io.plyschik.springbootblog.entity.Category;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.entity.Tag;
import io.plyschik.springbootblog.exception.CategoryNotFoundException;
import io.plyschik.springbootblog.exception.PostNotFoundException;
import io.plyschik.springbootblog.exception.TagNotFoundException;
import io.plyschik.springbootblog.service.CategoryService;
import io.plyschik.springbootblog.service.PostService;
import io.plyschik.springbootblog.service.TagService;
import io.plyschik.springbootblog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PostController {
    private final MessageSource messageSource;
    private final UserService userService;
    private final PostService postService;
    private final CategoryService categoryService;
    private final TagService tagService;

    @GetMapping("/dashboard/posts")
    public ModelAndView showList(
        @RequestParam(defaultValue = "0") int page,
        @Value("${pagination.items-per-page}") int itemsPerPage
    ) {
        Page<Post> posts = postService.getPaginatedPosts(PageRequest.of(page, itemsPerPage));

        return new ModelAndView("dashboard/post/list", "posts", posts);
    }

    @GetMapping("/dashboard/posts/create")
    public ModelAndView showCreateForm() {
        List<Category> categories = categoryService.getCategories();
        List<Tag> tags = tagService.getAll();

        ModelAndView modelAndView = new ModelAndView("dashboard/post/create");
        modelAndView.addObject("post", new PostDto());
        modelAndView.addObject("categories", categories);
        modelAndView.addObject("tags", tags);

        return modelAndView;
    }

    @PostMapping("/dashboard/posts/create")
    public ModelAndView processCreateForm(
        @ModelAttribute("post") @Valid PostDto postDto,
        BindingResult bindingResult,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("dashboard/post/create");
        }

        try {
            postService.createPost(postDto, userService.getUserByEmail(principal.getName()));
        } catch (CategoryNotFoundException exception) {
            bindingResult.rejectValue(
                "categoryId",
                "error.categoryId",
                messageSource.getMessage(
                    "message.category_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                )
            );

            return new ModelAndView("dashboard/post/create");
        } catch (TagNotFoundException exception) {
            bindingResult.rejectValue(
                "tagIds",
                "error.tagIds",
                messageSource.getMessage(
                    "message.category_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                )
            );

            return new ModelAndView("dashboard/post/create");
        }

        redirectAttributes.addFlashAttribute(
            "alert",
            new Alert("success", messageSource.getMessage(
                "message.post_has_been_successfully_created",
                null,
                LocaleContextHolder.getLocale()
            ))
        );

        return new ModelAndView("redirect:/dashboard/posts");
    }

    @GetMapping("/dashboard/posts/{id}/edit")
    public ModelAndView showEditForm(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            PostDto post = postService.getPostForEdit(id);
            List<Category> categories = categoryService.getCategories();
            List<Tag> tags = tagService.getAll();

            ModelAndView modelAndView = new ModelAndView("dashboard/post/edit");
            modelAndView.addObject("id", id);
            modelAndView.addObject("post", post);
            modelAndView.addObject("categories", categories);
            modelAndView.addObject("tags", tags);

            return modelAndView;
        } catch (PostNotFoundException exception) {
            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("danger", messageSource.getMessage(
                    "message.post_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView("redirect:/dashboard/posts");
        }
    }

    @PostMapping("/dashboard/posts/{id}/edit")
    public ModelAndView processEditForm(
        @PathVariable long id,
        @ModelAttribute("post") @Valid PostDto postDto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        try {
            if (bindingResult.hasErrors()) {
                return new ModelAndView("dashboard/post/edit");
            }

            postService.updatePost(id, postDto);

            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("success", messageSource.getMessage(
                    "message.post_has_been_successfully_updated",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView("redirect:/dashboard/posts");
        } catch (PostNotFoundException exception) {
            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("danger", messageSource.getMessage(
                    "message.post_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView("redirect:/dashboard/posts");
        } catch (CategoryNotFoundException exception) {
            bindingResult.rejectValue(
                "categoryId",
                "error.categoryId",
                messageSource.getMessage(
                    "message.category_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                )
            );

            return new ModelAndView("dashboard/post/edit");
        }
    }

    @GetMapping("/dashboard/posts/{id}/delete")
    public ModelAndView deleteConfirmation(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            Post post = postService.getById(id);

            return new ModelAndView("dashboard/post/delete", "post", post);
        } catch (PostNotFoundException exception) {
            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("danger", messageSource.getMessage(
                    "message.post_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView("redirect:/dashboard/posts");
        }
    }

    @PostMapping("/dashboard/posts/{id}/delete")
    public ModelAndView delete(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            postService.delete(id);

            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("success", messageSource.getMessage(
                    "message.post_has_been_successfully_deleted",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView("redirect:/dashboard/posts");
        } catch (PostNotFoundException postNotFound) {
            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("danger", messageSource.getMessage(
                    "message.post_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView("redirect:/dashboard/posts");
        }
    }
}
