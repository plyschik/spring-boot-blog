package io.plyschik.springbootblog.controller.dashboard;

import io.plyschik.springbootblog.dto.Alert;
import io.plyschik.springbootblog.dto.PostDto;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.exception.PostNotFound;
import io.plyschik.springbootblog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping("/dashboard/posts")
    public ModelAndView showList(
        @RequestParam(defaultValue = "0") int page,
        @Value("${pagination.items-per-page}") int itemsPerPage
    ) {
        return new ModelAndView(
            "dashboard/post/list",
            "posts",
            postService.getPaginatedPosts(PageRequest.of(page, itemsPerPage))
        );
    }

    @GetMapping("/dashboard/posts/create")
    public ModelAndView showCreateForm() {
        return new ModelAndView("dashboard/post/create", "post", new PostDto());
    }

    @PostMapping("/dashboard/posts/create")
    public ModelAndView processCreateForm(
        @ModelAttribute("post") @Valid PostDto postDto,
        BindingResult bindingResult,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("dashboard/post/create");
        }

        postService.createPost(postDto, (User) authentication.getPrincipal());

        redirectAttributes.addFlashAttribute(
            "alert",
            new Alert("success", "Post has been successfully created.")
        );

        return new ModelAndView("redirect:/dashboard/posts");
    }

    @GetMapping("/dashboard/posts/{id}/edit")
    public ModelAndView showEditForm(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            ModelAndView modelAndView = new ModelAndView("dashboard/post/edit");
            modelAndView.addObject("id", id);
            modelAndView.addObject("post", postService.getPostForEdit(id));

            return modelAndView;
        } catch (PostNotFound exception) {
            redirectAttributes.addFlashAttribute("alert", new Alert("danger", exception.getMessage()));

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
                new Alert("success", "Post has been successfully updated.")
            );

            return new ModelAndView("redirect:/dashboard/posts");
        } catch (PostNotFound exception) {
            redirectAttributes.addFlashAttribute("alert", new Alert("danger", exception.getMessage()));

            return new ModelAndView("redirect:/dashboard/posts");
        }
    }

    @GetMapping("/dashboard/posts/{id}/delete")
    public ModelAndView deleteConfirmation(@PathVariable long id, RedirectAttributes redirectAttributes) {
        Optional<Post> post = postService.getById(id);

        if (post.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("danger", "Post not found.")
            );

            return new ModelAndView("redirect:/dashboard/posts");
        }

        return new ModelAndView("dashboard/post/delete", "post", post.get());
    }

    @PostMapping("/dashboard/posts/{id}/delete")
    public ModelAndView delete(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            postService.delete(id);

            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("success", "Post has been successfully deleted.")
            );

            return new ModelAndView("redirect:/dashboard/posts");
        } catch (PostNotFound postNotFound) {
            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("danger", "Post not found.")
            );

            return new ModelAndView("redirect:/dashboard/posts");
        }
    }
}
