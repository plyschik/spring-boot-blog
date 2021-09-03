package io.plyschik.springbootblog.controller.dashboard;

import io.plyschik.springbootblog.dto.Alert;
import io.plyschik.springbootblog.dto.CategoryDto;
import io.plyschik.springbootblog.dto.CategoryWithPostsCount;
import io.plyschik.springbootblog.entity.Category;
import io.plyschik.springbootblog.exception.CategoryAlreadyExistsException;
import io.plyschik.springbootblog.exception.CategoryNotFoundException;
import io.plyschik.springbootblog.service.CategoryService;
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
public class CategoryController {
    private final MessageSource messageSource;
    private final CategoryService categoryService;

    @GetMapping("/dashboard/categories")
    public ModelAndView showList(
        @RequestParam(defaultValue = "") String name,
        @SortDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<CategoryWithPostsCount> categories = categoryService.getCategories(name, pageable);

        return new ModelAndView("dashboard/category/list", "categories", categories);
    }

    @GetMapping("/dashboard/categories/create")
    public ModelAndView showCreateForm() {
        return new ModelAndView("dashboard/category/create", "category", new CategoryDto());
    }

    @PostMapping("/dashboard/categories/create")
    public ModelAndView processCreateForm(
        @ModelAttribute("category") @Valid CategoryDto categoryDto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("dashboard/category/create");
        }

        try {
            categoryService.createCategory(categoryDto);
        } catch (CategoryAlreadyExistsException exception) {
            bindingResult.rejectValue(
                "name",
                "error.name",
                messageSource.getMessage(
                    "message.category_already_exists",
                    null,
                    LocaleContextHolder.getLocale()
                )
            );

            return new ModelAndView("dashboard/category/create");
        }

        redirectAttributes.addFlashAttribute(
            "alert",
            new Alert("success", messageSource.getMessage(
                "message.category_has_been_successfully_created",
                null,
                LocaleContextHolder.getLocale()
            ))
        );
        
        return new ModelAndView("redirect:/dashboard/categories");
    }

    @GetMapping("/dashboard/categories/{id}/edit")
    public ModelAndView showEditForm(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            CategoryDto category = categoryService.getCategoryForEdit(id);

            ModelAndView modelAndView = new ModelAndView("dashboard/category/edit");
            modelAndView.addObject("id", id);
            modelAndView.addObject("category", category);

            return modelAndView;
        } catch (CategoryNotFoundException exception) {
            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("danger", messageSource.getMessage(
                    "message.category_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView("redirect:/dashboard/categories");
        }
    }

    @PostMapping("/dashboard/categories/{id}/edit")
    public ModelAndView processEditForm(
        @PathVariable long id,
        @ModelAttribute("category") @Valid CategoryDto categoryDto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        try {
            if (bindingResult.hasErrors()) {
                return new ModelAndView("dashboard/category/edit");
            }

            categoryService.updateCategory(id, categoryDto);

            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("success", messageSource.getMessage(
                    "message.category_has_been_successfully_updated",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView("redirect:/dashboard/categories");
        } catch (CategoryNotFoundException exception) {
            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("danger", messageSource.getMessage(
                    "message.category_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView("redirect:/dashboard/categories");
        } catch (CategoryAlreadyExistsException exception) {
            bindingResult.rejectValue(
                "name",
                "error.name",
                messageSource.getMessage(
                    "message.category_already_exists",
                    null,
                    LocaleContextHolder.getLocale()
                )
            );

            return new ModelAndView("dashboard/category/edit");
        }
    }

    @GetMapping("/dashboard/categories/{id}/delete")
    public ModelAndView deleteConfirmation(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.getCategoryById(id);

            return new ModelAndView("dashboard/category/delete", "category", category);
        } catch (CategoryNotFoundException exception) {
            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("danger", messageSource.getMessage(
                    "message.category_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView("redirect:/dashboard/categories");
        }
    }

    @PostMapping("/dashboard/categories/{id}/delete")
    public ModelAndView delete(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);

            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("success", messageSource.getMessage(
                    "message.category_has_been_successfully_deleted",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView("redirect:/dashboard/categories");
        } catch (EmptyResultDataAccessException exception) {
            redirectAttributes.addFlashAttribute(
                "alert",
                new Alert("danger", messageSource.getMessage(
                    "message.category_not_found",
                    null,
                    LocaleContextHolder.getLocale()
                ))
            );

            return new ModelAndView("redirect:/dashboard/categories");
        }
    }
}
