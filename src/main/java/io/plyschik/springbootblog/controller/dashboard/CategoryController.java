package io.plyschik.springbootblog.controller.dashboard;

import io.plyschik.springbootblog.dto.Alert;
import io.plyschik.springbootblog.dto.CategoryDto;
import io.plyschik.springbootblog.dto.PostDto;
import io.plyschik.springbootblog.exception.CategoryAlreadyExists;
import io.plyschik.springbootblog.exception.CategoryNotFound;
import io.plyschik.springbootblog.exception.PostNotFound;
import io.plyschik.springbootblog.service.CategoryService;
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
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/dashboard/categories")
    public ModelAndView showList(
        @RequestParam(defaultValue = "0") int page,
        @Value("${pagination.items-per-page}") int itemsPerPage
    ) {
        return new ModelAndView(
            "dashboard/category/list",
            "categories",
            categoryService.getPaginatedCategories(PageRequest.of(page, itemsPerPage))
        );
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
        } catch (CategoryAlreadyExists exception) {
            bindingResult.rejectValue("name", "error.name", exception.getMessage());

            return new ModelAndView("dashboard/category/create");
        }

        redirectAttributes.addFlashAttribute(
            "alert",
            new Alert("success", "Category has been successfully created.")
        );
        
        return new ModelAndView("redirect:/dashboard/categories");
    }

    @GetMapping("/dashboard/categories/{id}/edit")
    public ModelAndView showEditForm(@PathVariable long id, RedirectAttributes redirectAttributes) {
        try {
            ModelAndView modelAndView = new ModelAndView("dashboard/category/edit");
            modelAndView.addObject("id", id);
            modelAndView.addObject("category", categoryService.getCategoryForEdit(id));

            return modelAndView;
        } catch (CategoryNotFound exception) {
            redirectAttributes.addFlashAttribute("alert", new Alert("danger", exception.getMessage()));

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
                new Alert("success", "Category has been successfully updated.")
            );

            return new ModelAndView("redirect:/dashboard/categories");
        } catch (CategoryNotFound exception) {
            redirectAttributes.addFlashAttribute("alert", new Alert("danger", exception.getMessage()));

            return new ModelAndView("redirect:/dashboard/categories");
        } catch (CategoryAlreadyExists exception) {
            bindingResult.rejectValue("name", "error.name", exception.getMessage());

            return new ModelAndView("dashboard/category/edit");
        }
    }
}
