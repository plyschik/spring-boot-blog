package io.plyschik.springbootblog.controller;

import io.plyschik.springbootblog.dto.Alert;
import io.plyschik.springbootblog.dto.CommentDto;
import io.plyschik.springbootblog.exception.CommentNotFound;
import io.plyschik.springbootblog.exception.PostNotFound;
import io.plyschik.springbootblog.exception.UserNotFound;
import io.plyschik.springbootblog.service.CommentService;
import io.plyschik.springbootblog.service.PostService;
import io.plyschik.springbootblog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class CommentController {
    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;
    private final MessageSource messageSource;

    @PostMapping("/posts/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView processCommentCreateForm(
        @PathVariable("id") Long postId,
        @ModelAttribute("comment") @Valid CommentDto commentDto,
        BindingResult bindingResult,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("comment", commentDto);
            redirectAttributes.addFlashAttribute(
                "org.springframework.validation.BindingResult.comment",
                bindingResult
            );

            return new ModelAndView(String.format("redirect:/posts/%d", postId));
        }

        try {
            commentService.createComment(
                commentDto,
                userService.getByEmail(principal.getName()).orElseThrow(UserNotFound::new),
                postService.getById(postId).orElseThrow(PostNotFound::new)
            );

            redirectAttributes.addFlashAttribute("alert", new Alert(
                "success",
                messageSource.getMessage(
                    "message.comment_has_successfully_created",
                    null,
                    LocaleContextHolder.getLocale()
                )
            ));

            return new ModelAndView(String.format("redirect:/posts/%d", postId));
        } catch (UserNotFound exception) {
            redirectAttributes.addFlashAttribute("alert", new Alert(
                "danger",
                messageSource.getMessage(
                    "message.user_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                )
            ));

            return new ModelAndView("redirect:/");
        } catch (PostNotFound exception) {
            redirectAttributes.addFlashAttribute("alert", new Alert(
                "danger",
                messageSource.getMessage(
                    "message.post_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                )
            ));

            return new ModelAndView("redirect:/");
        }
    }

    @GetMapping("/posts/{postId}/comments/{commentId}/edit")
    @PreAuthorize("@AuthorizationComponent.canAuthenticatedUserEditComment(principal, #commentId)")
    public ModelAndView commentEditForm(@PathVariable Long postId, @PathVariable Long commentId, Model model) {
        try {
            ModelAndView modelAndView = new ModelAndView("comment/edit");
            modelAndView.addObject("postId", postId);
            modelAndView.addObject("commentId", commentId);

            if (!model.containsAttribute("comment")) {
                modelAndView.addObject("comment", commentService.getCommentForEdit(commentId));
            }

            return modelAndView;
        } catch (CommentNotFound exception) {
            return new ModelAndView(String.format("redirect:/posts/%d", postId));
        }
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/edit")
    @PreAuthorize("@AuthorizationComponent.canAuthenticatedUserEditComment(principal, #commentId)")
    public ModelAndView processEditForm(
        @PathVariable Long postId,
        @PathVariable Long commentId,
        @ModelAttribute("comment") @Valid CommentDto commentDto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("comment", commentDto);
                redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.comment",
                    bindingResult
                );

                return new ModelAndView(String.format("redirect:/posts/%d/comments/%d/edit", postId, commentId));
            }

            commentService.updateComment(commentId, commentDto);

            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("success", messageSource.getMessage(
                    "message.comment_has_been_successfully_updated",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView(String.format("redirect:/posts/%d", postId));
        } catch (CommentNotFound exception) {
            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("danger", messageSource.getMessage(
                    "message.comment_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView(String.format("redirect:/posts/%d", postId));
        }
    }
}
