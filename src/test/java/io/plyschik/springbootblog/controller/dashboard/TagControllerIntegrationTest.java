package io.plyschik.springbootblog.controller.dashboard;

import io.plyschik.springbootblog.dto.TagDto;
import io.plyschik.springbootblog.entity.Tag;
import io.plyschik.springbootblog.exception.TagNotFound;
import io.plyschik.springbootblog.repository.TagRepository;
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
class TagControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TagRepository tagRepository;

    @Test
    @WithMockUser
    public void shouldReturnForbiddenWhenUserRoleIsUser() throws Exception {
        mockMvc.perform(get("/dashboard/tags"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/tags/create"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/dashboard/tags/create"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/tags/1/edit"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/dashboard/tags/1/edit"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/tags/1/delete"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/dashboard/tags/1/delete"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"MODERATOR"})
    public void shouldReturnForbiddenWhenUserRoleIsModerator() throws Exception {
        mockMvc.perform(get("/dashboard/tags"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/tags/create"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/dashboard/tags/create"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/tags/1/edit"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/dashboard/tags/1/edit"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/dashboard/tags/1/delete"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/dashboard/tags/1/delete"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldReturnCategoriesList() throws Exception {
        mockMvc.perform(get("/dashboard/tags"))
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/tag/list"))
            .andExpect(model().attributeExists("tags"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldReturnFormValidationErrorWhenNameFieldIsInvalid() throws Exception {
        mockMvc.perform(post("/dashboard/tags/create")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", "") // can not be blank
            .sessionAttr("tag", new TagDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/tag/create"))
            .andExpect(model().attributeHasFieldErrors("tag", "name"));

        mockMvc.perform(post("/dashboard/tags/create")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", "a") // should be minimum 2 characters
            .sessionAttr("tag", new TagDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/tag/create"))
            .andExpect(model().attributeHasFieldErrors("tag", "name"));

        mockMvc.perform(post("/dashboard/tags/create")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", "a".repeat(31)) // max 30 characters
            .sessionAttr("tag", new TagDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/tag/create"))
            .andExpect(model().attributeHasFieldErrors("tag", "name"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldReturnFormValidationErrorWhenTagNameIsNotUnique() throws Exception {
        Tag tag = createTag("Tag name");

        mockMvc.perform(post("/dashboard/tags/create")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", tag.getName())
            .sessionAttr("tag", new TagDto())
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/tag/create"))
            .andExpect(model().attributeHasFieldErrors("tag", "name"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldPersistTagWhenFormFieldsAreValid() throws Exception {
        mockMvc.perform(post("/dashboard/tags/create")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", "Tag name")
            .sessionAttr("tag", new TagDto())
        )
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrl("/dashboard/tags"));

        Tag tag = tagRepository.findAll().stream().findFirst().orElseThrow();

        assertEquals("Tag name", tag.getName());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldReturnFilledEditFormWhenTagExists() throws Exception {
        Tag tag = createTag("Tag name");

        mockMvc.perform(get("/dashboard/tags/{id}/edit", tag.getId()))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("id", "tag"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldRedirectToTagsListWhenTagNotExists() throws Exception {
        mockMvc.perform(get("/dashboard/tags/{id}/edit", 1))
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrl("/dashboard/tags"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldReturnEditFormValidationErrorWhenNameFieldIsInvalid() throws Exception {
        Tag tag = createTag("Tag name");

        mockMvc.perform(post("/dashboard/tags/{id}/edit", tag.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", "") // can not be blank
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/tag/edit"))
            .andExpect(model().attributeHasFieldErrors("tag", "name"));

        mockMvc.perform(post("/dashboard/tags/{id}/edit", tag.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", "a") // should be minimum 2 characters
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/tag/edit"))
            .andExpect(model().attributeHasFieldErrors("tag", "name"));

        mockMvc.perform(post("/dashboard/tags/{id}/edit", tag.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", "a".repeat(31)) // max 30 characters
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/tag/edit"))
            .andExpect(model().attributeHasFieldErrors("tag", "name"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldReturnEditFormValidationErrorWhenTagNameIsNotUnique() throws Exception {
        Tag tag = createTag("Tag name");
        Tag secondTag = createTag("Not unique");

        mockMvc.perform(post("/dashboard/tags/{id}/edit", tag.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", secondTag.getName()) // can not be blank
        )
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/tag/edit"))
            .andExpect(model().attributeHasFieldErrors("tag", "name"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldUpdateCategoryWhenFormFieldsAreValid() throws Exception {
        Tag tag = createTag("Tag name");

        mockMvc.perform(post("/dashboard/tags/{id}/edit", tag.getId())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .with(csrf())
            .param("name", "Updated name")
        )
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrl("/dashboard/tags"));

        Tag updatedTag = tagRepository.findById(tag.getId()).orElseThrow(TagNotFound::new);

        assertEquals("Updated name", updatedTag.getName());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldReturnDeleteConfirmViewWhenTagExists() throws Exception {
        Tag tag = createTag("Tag name");

        mockMvc.perform(get("/dashboard/tags/{id}/delete", tag.getId()))
            .andExpect(status().isOk())
            .andExpect(view().name("dashboard/tag/delete"))
            .andExpect(model().attributeExists("tag"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldRedirectToTagsListWhenTagNotExistsAfterDeleteAttempt() throws Exception {
        mockMvc.perform(get("/dashboard/tags/{id}/delete", 1))
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrl("/dashboard/tags"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldRedirectToTagsListWhenTagNotExistsAfterConfirmation() throws Exception {
        mockMvc.perform(post("/dashboard/tags/{id}/delete", 1).with(csrf()))
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrl("/dashboard/tags"));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRATOR"})
    public void shouldDeleteTagWhenConfirmed() throws Exception {
        Tag tag = createTag("Tag name");

        mockMvc.perform(post("/dashboard/tags/{id}/delete", tag.getId()).with(csrf()))
            .andExpect(flash().attributeExists("alert"))
            .andExpect(redirectedUrl("/dashboard/tags"));

        assertFalse(tagRepository.existsById(tag.getId()));
    }

    private Tag createTag(String name) {
        Tag tag = new Tag();
        tag.setName(name);

        return tagRepository.save(tag);
    }
}
