package io.plyschik.springbootblog.controller.dashboard;

import io.plyschik.springbootblog.dto.PostDto;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.service.PostService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/dashboard/posts/create")
    public ModelAndView showCreateForm() {
        return new ModelAndView("dashboard/create", "post", new PostDto());
    }

    @PostMapping("/dashboard/posts/create")
    public ModelAndView processCreateForm(
        Authentication authentication,
        @ModelAttribute("post") @Valid PostDto postDto,
        BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("dashboard/create");
        }

        postService.createPost(postDto, (User) authentication.getPrincipal());

        return new ModelAndView("redirect:/");
    }
}
