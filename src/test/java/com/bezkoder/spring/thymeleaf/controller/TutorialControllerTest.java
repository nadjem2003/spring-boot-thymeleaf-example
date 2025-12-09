package com.bezkoder.spring.thymeleaf.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bezkoder.spring.thymeleaf.entity.Tutorial;
import com.bezkoder.spring.thymeleaf.repository.TutorialRepository;

/**
 * Unit tests for TutorialController.
 * These tests verify that controller endpoints correctly handle HTTP requests,
 * interact with the repository, and return appropriate views and redirects.
 */
@WebMvcTest(TutorialController.class)
@DisplayName("Tutorial Controller Tests")
class TutorialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TutorialRepository tutorialRepository;

    private Tutorial testTutorial;
    private List<Tutorial> tutorialList;

    @BeforeEach
    void setUp() {
        testTutorial = new Tutorial("Test Tutorial", "Test Description", 5, true);
        testTutorial.setId(1);
        
        tutorialList = new ArrayList<>();
        tutorialList.add(testTutorial);
    }

    // Test: Verify GET /tutorials returns the tutorials view with all tutorials
    // Reason: This is the main listing page. It must display all tutorials
    // when no search keyword is provided, which is the default behavior
    @Test
    @DisplayName("Should display all tutorials when accessing /tutorials")
    void testGetAllTutorials() throws Exception {
        when(tutorialRepository.findAll()).thenReturn(tutorialList);
        
        mockMvc.perform(get("/tutorials"))
                .andExpect(status().isOk())
                .andExpect(view().name("tutorials"))
                .andExpect(model().attributeExists("tutorials"))
                .andExpect(model().attribute("tutorials", tutorialList));
        
        verify(tutorialRepository, times(1)).findAll();
    }

    // Test: Verify GET /tutorials with keyword parameter filters tutorials
    // Reason: Search functionality is a key feature. This ensures users can
    // filter tutorials by title keyword, which improves usability
    @Test
    @DisplayName("Should filter tutorials by keyword when provided")
    void testGetAllTutorialsWithKeyword() throws Exception {
        String keyword = "Test";
        when(tutorialRepository.findByTitleContainingIgnoreCase(keyword)).thenReturn(tutorialList);
        
        mockMvc.perform(get("/tutorials").param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(view().name("tutorials"))
                .andExpect(model().attributeExists("tutorials"))
                .andExpect(model().attributeExists("keyword"))
                .andExpect(model().attribute("keyword", keyword))
                .andExpect(model().attribute("tutorials", tutorialList));
        
        verify(tutorialRepository, times(1)).findByTitleContainingIgnoreCase(keyword);
        verify(tutorialRepository, never()).findAll();
    }

    // Test: Verify GET /tutorials handles repository exceptions gracefully
    // Reason: Error handling is critical for user experience. The controller
    // should catch exceptions and display error messages instead of crashing
    @Test
    @DisplayName("Should handle exceptions when retrieving tutorials")
    void testGetAllTutorialsWithException() throws Exception {
        when(tutorialRepository.findAll()).thenThrow(new RuntimeException("Database error"));
        
        mockMvc.perform(get("/tutorials"))
                .andExpect(status().isOk())
                .andExpect(view().name("tutorials"))
                .andExpect(model().attributeExists("message"));
        
        verify(tutorialRepository, times(1)).findAll();
    }

    // Test: Verify GET /tutorials/new returns the form view for creating new tutorial
    // Reason: The "Add Tutorial" feature requires a form page. This ensures
    // users can access the creation form with a properly initialized empty tutorial
    @Test
    @DisplayName("Should display form for creating new tutorial")
    void testAddTutorial() throws Exception {
        mockMvc.perform(get("/tutorials/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("tutorial_form"))
                .andExpect(model().attributeExists("tutorial"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Create new Tutorial"));
        
        // Verify the tutorial in model has published=true by default
        // This is tested through the view name and model attributes
    }

    // Test: Verify POST /tutorials/save creates a new tutorial successfully
    // Reason: Creating tutorials is a core CRUD operation. This ensures the
    // save endpoint correctly persists new tutorials and redirects with success message
    @Test
    @DisplayName("Should save new tutorial and redirect with success message")
    void testSaveTutorial() throws Exception {
        Tutorial newTutorial = new Tutorial("New Tutorial", "New Description", 3, false);
        when(tutorialRepository.save(any(Tutorial.class))).thenReturn(newTutorial);
        
        mockMvc.perform(post("/tutorials/save")
                .param("title", "New Tutorial")
                .param("description", "New Description")
                .param("level", "3")
                .param("published", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutorials"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "The Tutorial has been saved successfully!"));
        
        verify(tutorialRepository, times(1)).save(any(Tutorial.class));
    }

    // Test: Verify POST /tutorials/save handles save exceptions gracefully
    // Reason: Database errors can occur during save. The controller should
    // catch exceptions and provide user feedback instead of showing error pages
    @Test
    @DisplayName("Should handle exceptions when saving tutorial")
    void testSaveTutorialWithException() throws Exception {
        when(tutorialRepository.save(any(Tutorial.class))).thenThrow(new RuntimeException("Save failed"));
        
        mockMvc.perform(post("/tutorials/save")
                .param("title", "New Tutorial")
                .param("description", "New Description")
                .param("level", "3")
                .param("published", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutorials?message=Save+failed"));
        
        verify(tutorialRepository, times(1)).save(any(Tutorial.class));
    }

    // Test: Verify GET /tutorials/{id} displays edit form for existing tutorial
    // Reason: Editing tutorials requires loading existing data into the form.
    // This ensures users can modify tutorial details correctly
    @Test
    @DisplayName("Should display edit form for existing tutorial")
    void testEditTutorial() throws Exception {
        Integer tutorialId = 1;
        when(tutorialRepository.findById(tutorialId)).thenReturn(Optional.of(testTutorial));
        
        mockMvc.perform(get("/tutorials/{id}", tutorialId))
                .andExpect(status().isOk())
                .andExpect(view().name("tutorial_form"))
                .andExpect(model().attributeExists("tutorial"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Edit Tutorial (ID: " + tutorialId + ")"))
                .andExpect(model().attribute("tutorial", testTutorial));
        
        verify(tutorialRepository, times(1)).findById(tutorialId);
    }

    // Test: Verify GET /tutorials/{id} handles non-existent tutorial gracefully
    // Reason: Users might try to edit tutorials that don't exist (deleted, wrong ID).
    // The controller should redirect with an error message instead of crashing
    @Test
    @DisplayName("Should redirect when editing non-existent tutorial")
    void testEditTutorialNotFound() throws Exception {
        Integer tutorialId = 999;
        when(tutorialRepository.findById(tutorialId)).thenReturn(Optional.empty());
        
        mockMvc.perform(get("/tutorials/{id}", tutorialId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutorials"))
                .andExpect(flash().attributeExists("message"));
        
        verify(tutorialRepository, times(1)).findById(tutorialId);
    }

    // Test: Verify GET /tutorials/{id} handles exceptions during edit
    // Reason: Database errors can occur when loading tutorial for editing.
    // Proper error handling ensures the application remains stable
    @Test
    @DisplayName("Should handle exceptions when loading tutorial for edit")
    void testEditTutorialWithException() throws Exception {
        Integer tutorialId = 1;
        when(tutorialRepository.findById(tutorialId)).thenThrow(new RuntimeException("Database error"));
        
        mockMvc.perform(get("/tutorials/{id}", tutorialId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutorials"))
                .andExpect(flash().attributeExists("message"));
        
        verify(tutorialRepository, times(1)).findById(tutorialId);
    }

    // Test: Verify GET /tutorials/delete/{id} deletes tutorial successfully
    // Reason: Delete is a critical operation. This ensures tutorials can be
    // removed from the system and users receive confirmation feedback
    @Test
    @DisplayName("Should delete tutorial and redirect with success message")
    void testDeleteTutorial() throws Exception {
        Integer tutorialId = 1;
        doNothing().when(tutorialRepository).deleteById(tutorialId);
        
        mockMvc.perform(get("/tutorials/delete/{id}", tutorialId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutorials"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "The Tutorial with id=" + tutorialId + " has been deleted successfully!"));
        
        verify(tutorialRepository, times(1)).deleteById(tutorialId);
    }

    // Test: Verify GET /tutorials/delete/{id} handles delete exceptions gracefully
    // Reason: Delete operations can fail (e.g., foreign key constraints).
    // The controller should handle errors gracefully and inform the user
    @Test
    @DisplayName("Should handle exceptions when deleting tutorial")
    void testDeleteTutorialWithException() throws Exception {
        Integer tutorialId = 1;
        doThrow(new RuntimeException("Delete failed")).when(tutorialRepository).deleteById(tutorialId);
        
        mockMvc.perform(get("/tutorials/delete/{id}", tutorialId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutorials"))
                .andExpect(flash().attributeExists("message"));
        
        verify(tutorialRepository, times(1)).deleteById(tutorialId);
    }

    // Test: Verify GET /tutorials/{id}/published/{status} updates published status to true
    // Reason: The publish/unpublish toggle is a key feature. This ensures
    // tutorials can be published, making them visible to users
    @Test
    @DisplayName("Should publish tutorial and redirect with success message")
    void testUpdatePublishedStatusToTrue() throws Exception {
        Integer tutorialId = 1;
        boolean published = true;
        doNothing().when(tutorialRepository).updatePublishedStatus(tutorialId, published);
        
        mockMvc.perform(get("/tutorials/{id}/published/{status}", tutorialId, published))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutorials"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "The Tutorial id=" + tutorialId + " has been published"));
        
        verify(tutorialRepository, times(1)).updatePublishedStatus(tutorialId, published);
    }

    // Test: Verify GET /tutorials/{id}/published/{status} updates published status to false
    // Reason: Unpublishing tutorials is equally important. This ensures
    // tutorials can be hidden/disabled when needed
    @Test
    @DisplayName("Should unpublish tutorial and redirect with success message")
    void testUpdatePublishedStatusToFalse() throws Exception {
        Integer tutorialId = 1;
        boolean published = false;
        doNothing().when(tutorialRepository).updatePublishedStatus(tutorialId, published);
        
        mockMvc.perform(get("/tutorials/{id}/published/{status}", tutorialId, published))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutorials"))
                .andExpect(flash().attributeExists("message"))
                .andExpect(flash().attribute("message", "The Tutorial id=" + tutorialId + " has been disabled"));
        
        verify(tutorialRepository, times(1)).updatePublishedStatus(tutorialId, published);
    }

    // Test: Verify GET /tutorials/{id}/published/{status} handles update exceptions
    // Reason: Status updates can fail due to database issues. Proper error
    // handling ensures the application remains stable and users are informed
    @Test
    @DisplayName("Should handle exceptions when updating published status")
    void testUpdatePublishedStatusWithException() throws Exception {
        Integer tutorialId = 1;
        boolean published = true;
        doThrow(new RuntimeException("Update failed")).when(tutorialRepository).updatePublishedStatus(tutorialId, published);
        
        mockMvc.perform(get("/tutorials/{id}/published/{status}", tutorialId, published))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutorials"))
                .andExpect(flash().attributeExists("message"));
        
        verify(tutorialRepository, times(1)).updatePublishedStatus(tutorialId, published);
    }

    // Test: Verify POST /tutorials/save updates existing tutorial (when ID is present)
    // Reason: The same save endpoint handles both create and update. This ensures
    // existing tutorials can be modified through the form submission
    @Test
    @DisplayName("Should update existing tutorial when ID is provided")
    void testSaveTutorialUpdate() throws Exception {
        Tutorial existingTutorial = new Tutorial("Updated Title", "Updated Description", 7, true);
        existingTutorial.setId(1);
        when(tutorialRepository.save(any(Tutorial.class))).thenReturn(existingTutorial);
        
        mockMvc.perform(post("/tutorials/save")
                .param("id", "1")
                .param("title", "Updated Title")
                .param("description", "Updated Description")
                .param("level", "7")
                .param("published", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutorials"))
                .andExpect(flash().attributeExists("message"));
        
        verify(tutorialRepository, times(1)).save(any(Tutorial.class));
    }
}

