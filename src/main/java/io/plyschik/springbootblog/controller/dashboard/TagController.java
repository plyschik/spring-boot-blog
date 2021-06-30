package io.plyschik.springbootblog.controller.dashboard;

import io.plyschik.springbootblog.dto.Alert;
import io.plyschik.springbootblog.dto.TagDto;
import io.plyschik.springbootblog.exception.TagAlreadyExists;
import io.plyschik.springbootblog.exception.TagNotFound;
import io.plyschik.springbootblog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @GetMapping("/dashboard/tags")
    public ModelAndView showList(
        @RequestParam(defaultValue = "0") int page,
        @Value("${pagination.items-per-page}") int itemsPerPage
    ) {
        return new ModelAndView(
            "dashboard/tag/list",
            "tags",
            tagService.getPaginatedTags(PageRequest.of(page, itemsPerPage))
        );
    }

    @GetMapping("/dashboard/tags/create")
    public ModelAndView showCreateForm() {
        return new ModelAndView("dashboard/tag/create", "tag", new TagDto());
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
        } catch (TagAlreadyExists exception) {
            bindingResult.rejectValue("name", "error.name", exception.getMessage());

            return new ModelAndView("dashboard/tag/create");
        }

        redirectAttributes.addFlashAttribute(
            "alert",
            new Alert("success", "Tag has been successfully created.")
        );

        return new ModelAndView("redirect:/dashboard/tags");
    }

    @GetMapping("/dashboard/tags/{id}/edit")
    public ModelAndView showEditForm(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            ModelAndView modelAndView = new ModelAndView("dashboard/tag/edit");
            modelAndView.addObject("id", id);
            modelAndView.addObject("tag", tagService.getTagForEdit(id));

            return modelAndView;
        } catch (TagNotFound exception) {
            redirectAttributes.addFlashAttribute("alert", new Alert("danger", exception.getMessage()));

            return new ModelAndView("redirect:/dashboard/tags");
        }
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
                new Alert("success", "Tag has been successfully updated.")
            );

            return new ModelAndView("redirect:/dashboard/tags");
        } catch (TagNotFound exception) {
            redirectAttributes.addFlashAttribute("alert", new Alert("danger", exception.getMessage()));

            return new ModelAndView("redirect:/dashboard/tags");
        } catch (TagAlreadyExists exception) {
            bindingResult.rejectValue("name", "error.name", exception.getMessage());

            return new ModelAndView("dashboard/tag/edit");
        }
    }
}
