package io.plyschik.springbootblog.controller.dashboard;

import io.plyschik.springbootblog.dto.Alert;
import io.plyschik.springbootblog.dto.TagDto;
import io.plyschik.springbootblog.dto.TagWithPostsCount;
import io.plyschik.springbootblog.entity.Tag;
import io.plyschik.springbootblog.exception.TagAlreadyExistsException;
import io.plyschik.springbootblog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class TagController {
    private final MessageSource messageSource;
    private final TagService tagService;

    @GetMapping("/dashboard/tags")
    public ModelAndView showList(
        @RequestParam(required = false, defaultValue = "") String query,
        @SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<TagWithPostsCount> tags = tagService.getTagsWithPostsCount(query, pageable);

        return new ModelAndView("dashboard/tag/list")
            .addObject("tags", tags);
    }

    @GetMapping("/dashboard/tags/create")
    public ModelAndView showCreateForm() {
        return new ModelAndView("dashboard/tag/create")
            .addObject("tag", new TagDto());
    }

    @PostMapping("/dashboard/tags/create")
    public ModelAndView processCreateForm(
        @ModelAttribute("tag") @Valid TagDto tagDto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("dashboard/tag/create");
        }

        try {
            tagService.createTag(tagDto);
        } catch (TagAlreadyExistsException exception) {
            bindingResult.rejectValue(
                "name",
                "error.name",
                messageSource.getMessage(
                    "message.tag_already_exists",
                    null,
                    LocaleContextHolder.getLocale()
                )
            );

            return new ModelAndView("dashboard/tag/create");
        }

        redirectAttributes.addFlashAttribute(
            "alert",
            new Alert("success", messageSource.getMessage(
                "message.tag_has_been_successfully_created",
                null,
                LocaleContextHolder.getLocale()
            ))
        );

        return new ModelAndView("redirect:/dashboard/tags");
    }

    @GetMapping("/dashboard/tags/{id}/edit")
    public ModelAndView showEditForm(@PathVariable long id) {
        TagDto tag = tagService.getTagForEdit(id);

        return new ModelAndView("dashboard/tag/edit")
            .addObject("id", id)
            .addObject("tag", tag);
    }

    @PostMapping("/dashboard/tags/{id}/edit")
    public ModelAndView processEditForm(
        @PathVariable long id,
        @ModelAttribute("tag") @Valid TagDto tagDto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        try {
            if (bindingResult.hasErrors()) {
                return new ModelAndView("dashboard/tag/edit");
            }

            tagService.updateTag(id, tagDto);

            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("success", messageSource.getMessage(
                    "message.tag_has_been_successfully_updated",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView("redirect:/dashboard/tags");
        } catch (TagAlreadyExistsException exception) {
            bindingResult.rejectValue(
                "name",
                "error.name",
                messageSource.getMessage(
                    "message.tag_already_exists",
                    null,
                    LocaleContextHolder.getLocale()
                )
            );

            return new ModelAndView("dashboard/tag/edit");
        }
    }

    @GetMapping("/dashboard/tags/{id}/delete")
    public ModelAndView deleteConfirmation(@PathVariable long id) {
        Tag tag = tagService.getTagById(id);

        return new ModelAndView("dashboard/tag/delete")
            .addObject("tag", tag);
    }

    @PostMapping("/dashboard/tags/{id}/delete")
    public ModelAndView delete(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            tagService.deleteTag(id);

            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("success", messageSource.getMessage(
                    "message.tag_has_been_successfully_deleted",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView("redirect:/dashboard/tags");
        } catch (EmptyResultDataAccessException exception) {
            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("danger", messageSource.getMessage(
                    "message.tag_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView("redirect:/dashboard/tags");
        }
    }
}
