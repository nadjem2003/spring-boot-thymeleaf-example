package com.bezkoder.spring.thymeleaf.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import com.bezkoder.spring.thymeleaf.entity.Tutorial;

/**
 * Integration tests for TutorialRepository.
 * These tests verify that the repository correctly interacts with the database,
 * including CRUD operations and custom query methods.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Tutorial Repository Tests")
class TutorialRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TutorialRepository tutorialRepository;

    private Tutorial savedTutorial;

    @BeforeEach
    void setUp() {
        // Clear any existing data before each test
        tutorialRepository.deleteAll();
        
        // Create and persist a test tutorial for use in tests
        Tutorial tutorial = new Tutorial("Spring Boot Advanced", "Advanced Spring Boot concepts", 8, true);
        savedTutorial = entityManager.persistAndFlush(tutorial);
    }

    // Test: Verify repository can save a new tutorial
    // Reason: Save is the fundamental CRUD operation. This ensures tutorials
    // can be persisted to the database, which is required for all other operations
    @Test
    @DisplayName("Should save a new tutorial")
    void testSaveTutorial() {
        Tutorial newTutorial = new Tutorial("Java Basics", "Introduction to Java", 3, false);
        
        Tutorial saved = tutorialRepository.save(newTutorial);
        
        assertNotNull(saved.getId());
        assertEquals("Java Basics", saved.getTitle());
        assertEquals("Introduction to Java", saved.getDescription());
        assertEquals(3, saved.getLevel());
        assertFalse(saved.isPublished());
    }

    // Test: Verify repository can find a tutorial by ID
    // Reason: FindById is essential for retrieving specific tutorials,
    // which is needed for edit operations and viewing details
    @Test
    @DisplayName("Should find tutorial by ID")
    void testFindById() {
        Optional<Tutorial> found = tutorialRepository.findById(savedTutorial.getId());
        
        assertTrue(found.isPresent());
        assertEquals(savedTutorial.getId(), found.get().getId());
        assertEquals("Spring Boot Advanced", found.get().getTitle());
    }

    // Test: Verify repository returns empty Optional for non-existent ID
    // Reason: The application must handle cases where a tutorial doesn't exist.
    // This ensures proper error handling when accessing invalid IDs
    @Test
    @DisplayName("Should return empty Optional for non-existent ID")
    void testFindByIdNotFound() {
        Optional<Tutorial> found = tutorialRepository.findById(9999);
        
        assertFalse(found.isPresent());
    }

    // Test: Verify repository can find all tutorials
    // Reason: FindAll is used to display the tutorial list page.
    // This ensures the main listing functionality works correctly
    @Test
    @DisplayName("Should find all tutorials")
    void testFindAll() {
        // Add more tutorials
        Tutorial tutorial2 = new Tutorial("Java 101", "Java basics", 2, true);
        Tutorial tutorial3 = new Tutorial("Spring MVC", "Spring MVC guide", 6, false);
        entityManager.persist(tutorial2);
        entityManager.persist(tutorial3);
        entityManager.flush();
        
        List<Tutorial> allTutorials = tutorialRepository.findAll();
        
        assertTrue(allTutorials.size() >= 3);
        assertTrue(allTutorials.stream().anyMatch(t -> t.getTitle().equals("Spring Boot Advanced")));
        assertTrue(allTutorials.stream().anyMatch(t -> t.getTitle().equals("Java 101")));
        assertTrue(allTutorials.stream().anyMatch(t -> t.getTitle().equals("Spring MVC")));
    }

    // Test: Verify repository returns empty list when no tutorials exist
    // Reason: The application should handle empty database gracefully.
    // This ensures the "No tutorials found" message displays correctly
    @Test
    @DisplayName("Should return empty list when no tutorials exist")
    void testFindAllEmpty() {
        tutorialRepository.deleteAll();
        
        List<Tutorial> allTutorials = tutorialRepository.findAll();
        
        assertTrue(allTutorials.isEmpty());
    }

    // Test: Verify custom search method finds tutorials by title (case-insensitive)
    // Reason: The search functionality is a key feature. This test ensures
    // users can find tutorials regardless of case sensitivity
    @Test
    @DisplayName("Should find tutorials by title containing keyword (case-insensitive)")
    void testFindByTitleContainingIgnoreCase() {
        // Add tutorials with different cases
        Tutorial tutorial1 = new Tutorial("Spring Framework", "Framework guide", 5, true);
        Tutorial tutorial2 = new Tutorial("spring boot", "Boot guide", 4, true);
        Tutorial tutorial3 = new Tutorial("SPRING DATA", "Data guide", 6, true);
        Tutorial tutorial4 = new Tutorial("Java Basics", "Java guide", 3, false);
        entityManager.persist(tutorial1);
        entityManager.persist(tutorial2);
        entityManager.persist(tutorial3);
        entityManager.persist(tutorial4);
        entityManager.flush();
        
        // Test case-insensitive search
        List<Tutorial> results1 = tutorialRepository.findByTitleContainingIgnoreCase("spring");
        List<Tutorial> results2 = tutorialRepository.findByTitleContainingIgnoreCase("SPRING");
        List<Tutorial> results3 = tutorialRepository.findByTitleContainingIgnoreCase("Spring");
        
        assertTrue(results1.size() >= 3);
        assertTrue(results2.size() >= 3);
        assertTrue(results3.size() >= 3);
        
        // Verify all results contain "spring" in title (case-insensitive)
        results1.forEach(t -> assertTrue(t.getTitle().toLowerCase().contains("spring")));
    }

    // Test: Verify search returns empty list when keyword doesn't match
    // Reason: Search should handle no-results scenarios gracefully.
    // This ensures the UI displays "No tutorials found" correctly
    @Test
    @DisplayName("Should return empty list when keyword doesn't match")
    void testFindByTitleContainingIgnoreCaseNoMatch() {
        List<Tutorial> results = tutorialRepository.findByTitleContainingIgnoreCase("NonExistentKeyword");
        
        assertTrue(results.isEmpty());
    }

    // Test: Verify search finds partial matches in title
    // Reason: Users should be able to find tutorials with partial keyword matches.
    // This improves search usability (e.g., "boot" finds "Spring Boot")
    @Test
    @DisplayName("Should find tutorials with partial keyword match")
    void testFindByTitleContainingIgnoreCasePartialMatch() {
        Tutorial tutorial1 = new Tutorial("Spring Boot", "Boot guide", 5, true);
        Tutorial tutorial2 = new Tutorial("Boot Camp", "Camp guide", 4, true);
        entityManager.persist(tutorial1);
        entityManager.persist(tutorial2);
        entityManager.flush();
        
        List<Tutorial> results = tutorialRepository.findByTitleContainingIgnoreCase("boot");
        
        assertTrue(results.size() >= 2);
        assertTrue(results.stream().anyMatch(t -> t.getTitle().contains("Boot")));
    }

    // Test: Verify repository can update an existing tutorial
    // Reason: Update functionality is essential for editing tutorials.
    // This ensures changes to tutorial data are properly persisted
    @Test
    @DisplayName("Should update existing tutorial")
    void testUpdateTutorial() {
        savedTutorial.setTitle("Updated Title");
        savedTutorial.setDescription("Updated Description");
        savedTutorial.setLevel(9);
        
        Tutorial updated = tutorialRepository.save(savedTutorial);
        
        assertEquals(savedTutorial.getId(), updated.getId());
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated Description", updated.getDescription());
        assertEquals(9, updated.getLevel());
    }

    // Test: Verify repository can delete a tutorial by ID
    // Reason: Delete is a critical CRUD operation. This ensures tutorials
    // can be removed from the database when users delete them
    @Test
    @DisplayName("Should delete tutorial by ID")
    void testDeleteById() {
        Integer id = savedTutorial.getId();
        
        tutorialRepository.deleteById(id);
        
        Optional<Tutorial> deleted = tutorialRepository.findById(id);
        assertFalse(deleted.isPresent());
    }

    // Test: Verify custom updatePublishedStatus method works correctly
    // Reason: The publish/unpublish toggle is a key feature. This test ensures
    // the custom query correctly updates the published status without loading the entity
    @Test
    @DisplayName("Should update published status using custom query")
    void testUpdatePublishedStatus() {
        Integer id = savedTutorial.getId();
        assertTrue(savedTutorial.isPublished());
        
        // Update to unpublished
        tutorialRepository.updatePublishedStatus(id, false);
        entityManager.clear(); // Clear persistence context to force reload
        
        Tutorial updated = tutorialRepository.findById(id).get();
        assertFalse(updated.isPublished());
        
        // Update back to published
        tutorialRepository.updatePublishedStatus(id, true);
        entityManager.clear();
        
        Tutorial republished = tutorialRepository.findById(id).get();
        assertTrue(republished.isPublished());
    }

    // Test: Verify updatePublishedStatus handles non-existent ID gracefully
    // Reason: The application should handle attempts to update non-existent tutorials.
    // This ensures proper error handling in edge cases
    @Test
    @DisplayName("Should handle updatePublishedStatus for non-existent ID")
    void testUpdatePublishedStatusNonExistentId() {
        // Should not throw exception, but also should not affect any records
        assertDoesNotThrow(() -> {
            tutorialRepository.updatePublishedStatus(9999, true);
        });
    }

    // Test: Verify repository can count total tutorials
    // Reason: Count is useful for statistics and pagination.
    // This ensures we can track the total number of tutorials in the system
    @Test
    @DisplayName("Should count total tutorials")
    void testCount() {
        long initialCount = tutorialRepository.count();
        
        Tutorial newTutorial = new Tutorial("New Tutorial", "Description", 5, true);
        tutorialRepository.save(newTutorial);
        
        assertEquals(initialCount + 1, tutorialRepository.count());
    }

    // Test: Verify repository can check if tutorial exists by ID
    // Reason: ExistsById is useful for validation before operations.
    // This ensures we can verify tutorial existence without loading the full entity
    @Test
    @DisplayName("Should check if tutorial exists by ID")
    void testExistsById() {
        assertTrue(tutorialRepository.existsById(savedTutorial.getId()));
        assertFalse(tutorialRepository.existsById(9999));
    }

    // Test: Verify search with empty string returns all tutorials
    // Reason: Edge case handling - empty search string might be treated as
    // "show all". This ensures consistent behavior in the search feature
    @Test
    @DisplayName("Should handle empty keyword in search")
    void testFindByTitleContainingIgnoreCaseEmptyKeyword() {
        List<Tutorial> results = tutorialRepository.findByTitleContainingIgnoreCase("");
        
        // Empty string should match all titles (contains empty string)
        assertTrue(results.size() >= 1);
    }

    // Test: Verify repository handles special characters in search
    // Reason: Users might search with special characters. This ensures
    // the search functionality is robust and doesn't break with special input
    @Test
    @DisplayName("Should handle special characters in search keyword")
    void testFindByTitleContainingIgnoreCaseSpecialCharacters() {
        Tutorial tutorial = new Tutorial("C++ Tutorial", "C++ guide", 5, true);
        entityManager.persist(tutorial);
        entityManager.flush();
        
        List<Tutorial> results = tutorialRepository.findByTitleContainingIgnoreCase("++");
        
        assertTrue(results.size() >= 1);
        assertTrue(results.stream().anyMatch(t -> t.getTitle().contains("++")));
    }
}

