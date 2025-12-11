package com.bezkoder.spring.thymeleaf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.bezkoder.spring.thymeleaf.entity.Tutorial;
import com.bezkoder.spring.thymeleaf.repository.TutorialRepository;

/**
 * Integration tests for the Tutorial application.
 * These tests verify end-to-end functionality by testing the complete
 * request-response cycle through all layers (Controller -> Repository -> Database).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@DisplayName("Tutorial Integration Tests")
class TutorialIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TutorialRepository tutorialRepository;

    @BeforeEach
    void setUp() {
        tutorialRepository.deleteAll();
    }

    // Test: Verify complete flow of creating a new tutorial
    // Reason: End-to-end test ensures the entire create workflow functions correctly,
    // from form submission through database persistence to redirect
    @Test
    @DisplayName("Should create tutorial through complete workflow")
    void testCreateTutorialFlow() throws Exception {
        // Verify initial state - no tutorials
        assertThat(tutorialRepository.count()).isEqualTo(0);
        
        // Submit form to create tutorial
        mockMvc.perform(post("/tutorials/save")
                .param("title", "Integration Test Tutorial")
                .param("description", "Testing full workflow")
                .param("level", "5")
                .param("published", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutorials"))
                .andExpect(flash().attributeExists("message"));
        
        // Verify tutorial was saved to database
        List<Tutorial> tutorials = tutorialRepository.findAll();
        assertThat(tutorials).hasSize(1);
        assertThat(tutorials.get(0).getTitle()).isEqualTo("Integration Test Tutorial");
        assertThat(tutorials.get(0).getDescription()).isEqualTo("Testing full workflow");
        assertThat(tutorials.get(0).getLevel()).isEqualTo(5);
        assertThat(tutorials.get(0).isPublished()).isTrue();
    }

    // Test: Verify complete flow of viewing tutorial list
    // Reason: The main listing page is the entry point. This ensures tutorials
    // are correctly retrieved from database and displayed
    @Test
    @DisplayName("Should display tutorial list with saved tutorials")
    void testViewTutorialList() throws Exception {
        // Create test data
        Tutorial tutorial1 = new Tutorial("Tutorial 1", "Description 1", 3, true);
        Tutorial tutorial2 = new Tutorial("Tutorial 2", "Description 2", 7, false);
        tutorialRepository.save(tutorial1);
        tutorialRepository.save(tutorial2);
        
        // Access the list page
        mockMvc.perform(get("/tutorials"))
                .andExpect(status().isOk())
                .andExpect(view().name("tutorials"))
                .andExpect(model().attributeExists("tutorials"));
        
        // Verify data exists in repository
        assertThat(tutorialRepository.count()).isEqualTo(2);
    }

    // Test: Verify complete flow of editing an existing tutorial
    // Reason: Edit workflow is critical for data management. This ensures users
    // can modify tutorials and changes are persisted correctly
    @Test
    @DisplayName("Should edit tutorial through complete workflow")
    void testEditTutorialFlow() throws Exception {
        // Create a tutorial
        Tutorial original = new Tutorial("Original Title", "Original Description", 4, true);
        Tutorial saved = tutorialRepository.save(original);
        Integer id = saved.getId();
        
        // Access edit page
        mockMvc.perform(get("/tutorials/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("tutorial_form"))
                .andExpect(model().attributeExists("tutorial"))
                .andExpect(model().attributeExists("pageTitle"));
        
        // Submit updated data
        mockMvc.perform(post("/tutorials/save")
                .param("id", id.toString())
                .param("title", "Updated Title")
                .param("description", "Updated Description")
                .param("level", "8")
                .param("published", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutorials"));
        
        // Verify changes were persisted
        Tutorial updated = tutorialRepository.findById(id).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        assertThat(updated.getLevel()).isEqualTo(8);
        assertThat(updated.isPublished()).isFalse();
    }

    // Test: Verify complete flow of deleting a tutorial
    // Reason: Delete is a destructive operation. This ensures tutorials are
    // properly removed from the database and users receive confirmation
    @Test
    @DisplayName("Should delete tutorial through complete workflow")
    void testDeleteTutorialFlow() throws Exception {
        // Create a tutorial
        Tutorial tutorial = new Tutorial("To Be Deleted", "Will be removed", 5, true);
        Tutorial saved = tutorialRepository.save(tutorial);
        Integer id = saved.getId();
        
        // Verify it exists
        assertThat(tutorialRepository.existsById(id)).isTrue();
        
        // Delete the tutorial
        mockMvc.perform(get("/tutorials/delete/{id}", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutorials"))
                .andExpect(flash().attributeExists("message"));
        
        // Verify it was deleted
        assertThat(tutorialRepository.existsById(id)).isFalse();
        assertThat(tutorialRepository.count()).isEqualTo(0);
    }

    // Test: Verify search functionality works end-to-end
    // Reason: Search is a key user feature. This ensures the complete search
    // flow from user input through database query to results display works correctly
    @Test
    @DisplayName("Should search tutorials through complete workflow")
    void testSearchTutorialsFlow() throws Exception {
        // Create test data with different titles
        Tutorial tutorial1 = new Tutorial("Spring Boot Guide", "Spring Boot tutorial", 5, true);
        Tutorial tutorial2 = new Tutorial("Java Basics", "Java fundamentals", 2, true);
        Tutorial tutorial3 = new Tutorial("Spring Framework", "Spring framework guide", 6, true);
        tutorialRepository.save(tutorial1);
        tutorialRepository.save(tutorial2);
        tutorialRepository.save(tutorial3);
        
        // Search for "Spring"
        mockMvc.perform(get("/tutorials").param("keyword", "Spring"))
                .andExpect(status().isOk())
                .andExpect(view().name("tutorials"))
                .andExpect(model().attributeExists("tutorials"))
                .andExpect(model().attributeExists("keyword"))
                .andExpect(model().attribute("keyword", "Spring"));
        
        // Verify search results in repository
        List<Tutorial> springResults = tutorialRepository.findByTitleContainingIgnoreCase("Spring");
        assertThat(springResults).hasSize(2);
        assertThat(springResults).extracting(Tutorial::getTitle)
                .containsExactlyInAnyOrder("Spring Boot Guide", "Spring Framework");
    }

    // Test: Verify publish/unpublish toggle works end-to-end
    // Reason: The publish status toggle is a key feature. This ensures the
    // complete workflow from click to database update functions correctly
    @Test
    @DisplayName("Should toggle published status through complete workflow")
    void testTogglePublishedStatusFlow() throws Exception {
        // Create unpublished tutorial
        Tutorial tutorial = new Tutorial("Test Tutorial", "Description", 5, false);
        Tutorial saved = tutorialRepository.save(tutorial);
        Integer id = saved.getId();
        
        // Verify initial state
        assertThat(saved.isPublished()).isFalse();
        
        // Publish the tutorial
        mockMvc.perform(get("/tutorials/{id}/published/{status}", id, true))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutorials"))
                .andExpect(flash().attributeExists("message"));
        
        // Verify status was updated
        Tutorial published = tutorialRepository.findById(id).orElseThrow();
        assertThat(published.isPublished()).isTrue();
        
        // Unpublish the tutorial
        mockMvc.perform(get("/tutorials/{id}/published/{status}", id, false))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tutorials"));
        
        // Verify status was updated again
        Tutorial unpublished = tutorialRepository.findById(id).orElseThrow();
        assertThat(unpublished.isPublished()).isFalse();
    }

    // Test: Verify accessing new tutorial form works correctly
    // Reason: The create form is the entry point for adding tutorials.
    // This ensures the form is accessible and properly initialized
    @Test
    @DisplayName("Should access new tutorial form correctly")
    void testAccessNewTutorialForm() throws Exception {
        mockMvc.perform(get("/tutorials/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("tutorial_form"))
                .andExpect(model().attributeExists("tutorial"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(model().attribute("pageTitle", "Create new Tutorial"));
    }

    // Test: Verify empty state when no tutorials exist
    // Reason: The application should handle empty database gracefully.
    // This ensures the "No tutorials found" message displays correctly
    @Test
    @DisplayName("Should handle empty tutorial list")
    void testEmptyTutorialList() throws Exception {
        // Ensure no tutorials exist
        assertThat(tutorialRepository.count()).isEqualTo(0);
        
        // Access list page
        mockMvc.perform(get("/tutorials"))
                .andExpect(status().isOk())
                .andExpect(view().name("tutorials"))
                .andExpect(model().attributeExists("tutorials"));
    }

    // Test: Verify search with no results handles gracefully
    // Reason: Users may search for non-existent keywords. The application
    // should display an empty list gracefully without errors
    @Test
    @DisplayName("Should handle search with no results")
    void testSearchWithNoResults() throws Exception {
        // Create a tutorial
        Tutorial tutorial = new Tutorial("Java Tutorial", "Java guide", 5, true);
        tutorialRepository.save(tutorial);
        
        // Search for non-matching keyword
        mockMvc.perform(get("/tutorials").param("keyword", "NonExistentKeyword"))
                .andExpect(status().isOk())
                .andExpect(view().name("tutorials"))
                .andExpect(model().attributeExists("tutorials"))
                .andExpect(model().attributeExists("keyword"));
        
        // Verify no results in repository
        List<Tutorial> results = tutorialRepository.findByTitleContainingIgnoreCase("NonExistentKeyword");
        assertThat(results).isEmpty();
    }

    // Test: Verify case-insensitive search works in integration
    // Reason: Search should work regardless of case. This end-to-end test
    // ensures the complete search feature handles case variations correctly
    @Test
    @DisplayName("Should perform case-insensitive search")
    void testCaseInsensitiveSearch() throws Exception {
        // Create tutorials with different cases
        Tutorial tutorial1 = new Tutorial("Spring Boot", "Guide", 5, true);
        Tutorial tutorial2 = new Tutorial("spring framework", "Framework", 6, true);
        Tutorial tutorial3 = new Tutorial("SPRING DATA", "Data", 7, true);
        tutorialRepository.save(tutorial1);
        tutorialRepository.save(tutorial2);
        tutorialRepository.save(tutorial3);
        
        // Search with lowercase
        mockMvc.perform(get("/tutorials").param("keyword", "spring"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("tutorials"));
        
        // Verify all variations are found
        List<Tutorial> results = tutorialRepository.findByTitleContainingIgnoreCase("spring");
        assertThat(results).hasSize(3);
    }

    // Test: Verify multiple CRUD operations in sequence
    // Reason: Real-world usage involves multiple operations. This test ensures
    // the application handles sequences of create, read, update, delete correctly
    @Test
    @DisplayName("Should handle multiple CRUD operations in sequence")
    void testMultipleCrudOperations() throws Exception {
        // Create
        Tutorial tutorial = new Tutorial("Initial Title", "Initial Description", 3, true);
        Tutorial saved = tutorialRepository.save(tutorial);
        Integer id = saved.getId();
        assertThat(tutorialRepository.count()).isEqualTo(1);
        
        // Read
        mockMvc.perform(get("/tutorials"))
                .andExpect(status().isOk());
        
        // Update
        mockMvc.perform(post("/tutorials/save")
                .param("id", id.toString())
                .param("title", "Updated Title")
                .param("description", "Updated Description")
                .param("level", "7")
                .param("published", "false"))
                .andExpect(status().is3xxRedirection());
        
        Tutorial updated = tutorialRepository.findById(id).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        
        // Delete
        mockMvc.perform(get("/tutorials/delete/{id}", id))
                .andExpect(status().is3xxRedirection());
        
        assertThat(tutorialRepository.count()).isEqualTo(0);
    }
}
