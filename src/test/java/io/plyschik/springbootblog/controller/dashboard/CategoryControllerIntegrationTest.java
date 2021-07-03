package io.plyschik.springbootblog.controller.dashboard;

import io.plyschik.springbootblog.TestUtils;
import io.plyschik.springbootblog.dto.CategoryDto;
import io.plyschik.springbootblog.entity.Category;
import io.plyschik.springbootblog.exception.CategoryNotFound;
import io.plyschik.springbootblog.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @WithMockUser
    public void shouldReturnForbiddenWhenUserRoleIsUser() throws Exception {
        mockMvc.perform(get("/dashboard/categories"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/categories/create"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/dashboard/categories/create"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/categories/1/edit"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/dashboard/categories/1/edit"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/categories/1/delete"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/dashboard/categories/1/delete"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"MODERATOR"})
    public void shouldReturnForbiddenWhenUserRoleIsModerator() throws Exception {
        mockMvc.perform(get("/dashboard/categories"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/categories/create"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/dashboard/categories/create"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/categories/1/edit"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/dashboard/categories/1/edit"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/categories/1/delete"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/dashboard/categories/1/delete"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldReturnCategoriesList() throws Exception {
        mockMvc.perform(get("/dashboard/categories"))
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/category/list"))
            .andExpect(model().attributeExists("categories"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldReturnFormValidationErrorWhenNameFieldIsInvalid() throws Exception {
        mockMvc.perform(post("/dashboard/categories/create")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", "") // can not be blank
            .sessionAttr("category", new CategoryDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/category/create"))
            .andExpect(model().attributeHasFieldErrors("category", "name"));

        mockMvc.perform(post("/dashboard/categories/create")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", "a") // should be minimum 2 characters
            .sessionAttr("category", new CategoryDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/category/create"))
            .andExpect(model().attributeHasFieldErrors("category", "name"));

        mockMvc.perform(post("/dashboard/categories/create")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", "a".repeat(31)) // max 30 characters
            .sessionAttr("category", new CategoryDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/category/create"))
            .andExpect(model().attributeHasFieldErrors("category", "name"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldReturnFormValidationErrorWhenCategoryNameIsNotUnique() throws Exception {
        Category category = testUtils.createCategory("Category name");

        mockMvc.perform(post("/dashboard/categories/create")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", category.getName())
            .sessionAttr("category", new CategoryDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/category/create"))
            .andExpect(model().attributeHasFieldErrors("category", "name"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldPersistPostWhenFormFieldsAreValid() throws Exception {
        mockMvc.perform(post("/dashboard/categories/create")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", "Category name")
            .sessionAttr("category", new CategoryDto())
        )
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrl("/dashboard/categories"));

        Category category = categoryRepository.findAll().stream().findFirst().orElseThrow();

        assertEquals("Category name", category.getName());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldReturnFilledEditFormWhenCategoryExists() throws Exception {
        Category category = testUtils.createCategory("Category name");

        mockMvc.perform(get("/dashboard/categories/{id}/edit", category.getId()))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("id", "category"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldRedirectToCategoriesListWhenCategoryNotExists() throws Exception {
        mockMvc.perform(get("/dashboard/categories/{id}/edit", 1))
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrl("/dashboard/categories"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldReturnEditFormValidationErrorWhenNameFieldIsInvalid() throws Exception {
        Category category = testUtils.createCategory("Category name");

        mockMvc.perform(post("/dashboard/categories/{id}/edit", category.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", "") // can not be blank
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/category/edit"))
            .andExpect(model().attributeHasFieldErrors("category", "name"));

        mockMvc.perform(post("/dashboard/categories/{id}/edit", category.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", "a") // should be minimum 2 characters
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/category/edit"))
            .andExpect(model().attributeHasFieldErrors("category", "name"));

        mockMvc.perform(post("/dashboard/categories/{id}/edit", category.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", "a".repeat(31)) // max 30 characters
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/category/edit"))
            .andExpect(model().attributeHasFieldErrors("category", "name"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldReturnEditFormValidationErrorWhenCategoryNameIsNotUnique() throws Exception {
        Category category = testUtils.createCategory("Category name");
        Category secondCategory = testUtils.createCategory("Not unique");

        mockMvc.perform(post("/dashboard/categories/{id}/edit", category.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", secondCategory.getName()) // can not be blank
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/category/edit"))
            .andExpect(model().attributeHasFieldErrors("category", "name"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldUpdateCategoryWhenFormFieldsAreValid() throws Exception {
        Category category = testUtils.createCategory("Category name");

        mockMvc.perform(post("/dashboard/categories/{id}/edit", category.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", "Updated name")
        )
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrl("/dashboard/categories"));

        Category updatedCategory = categoryRepository.findById(category.getId()).orElseThrow(CategoryNotFound::new);

        assertEquals("Updated name", updatedCategory.getName());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldReturnDeleteConfirmViewWhenCategoryExists() throws Exception {
        Category category = testUtils.createCategory("Category name");

        mockMvc.perform(get("/dashboard/categories/{id}/delete", category.getId()))
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/category/delete"))
            .andExpect(model().attributeExists("category"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldRedirectToCategoriesListWhenCategoryNotExistsAfterDeleteAttempt() throws Exception {
        mockMvc.perform(get("/dashboard/categories/{id}/delete", 1))
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrl("/dashboard/categories"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldRedirectToCategoriesListWhenCategoryNotExistsAfterConfirmation() throws Exception {
        mockMvc.perform(post("/dashboard/categories/{id}/delete", 1).with(csrf()))
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrl("/dashboard/categories"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldDeletePostWhenConfirmed() throws Exception {
        Category category = testUtils.createCategory("Category name");

        mockMvc.perform(post("/dashboard/categories/{id}/delete", category.getId()).with(csrf()))
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrl("/dashboard/categories"));

        assertFalse(categoryRepository.existsById(category.getId()));
    }
}
