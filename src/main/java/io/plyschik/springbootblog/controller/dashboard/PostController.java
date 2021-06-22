package io.plyschik.springbootblog.controller.dashboard;

import io.plyschik.springbootblog.dto.PostDto;
import io.plyschik.springbootblog.entity.Post;
import io.plyschik.springbootblog.entity.User;
import io.plyschik.springbootblog.repository.PostRepository;
import io.plyschik.springbootblog.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

@Controller
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/dashboard/posts")
    public ModelAndView showList(@RequestParam(defaultValue = "0") int page) {
        Page<Post> posts = postService.list(PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "id")));

        ModelAndView modelAndView = new ModelAndView("dashboard/list");
        modelAndView.addObject("posts", posts);

        return modelAndView;
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

        return new ModelAndView("redirect:/dashboard/posts");
    }
}
