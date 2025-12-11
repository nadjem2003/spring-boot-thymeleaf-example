package com.bezkoder.spring.thymeleaf.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Tutorial entity class.
 * These tests verify that the entity correctly handles data assignment,
 * getter/setter methods, and business logic constraints.
 */
@DisplayName("Tutorial Entity Tests")
class TutorialTest {

    private Tutorial tutorial;

    @BeforeEach
    void setUp() {
        tutorial = new Tutorial();
    }

    // Test: Verify default constructor creates a valid empty Tutorial object
    // Reason: Ensures the entity can be instantiated without parameters,
    // which is required by JPA for entity creation and form binding
    @Test
    @DisplayName("Should create Tutorial with default constructor")
    void testDefaultConstructor() {
        assertNotNull(tutorial);
        assertNull(tutorial.getId());
        assertNull(tutorial.getTitle());
        assertNull(tutorial.getDescription());
        assertEquals(0, tutorial.getLevel());
        assertFalse(tutorial.isPublished());
    }

    // Test: Verify parameterized constructor correctly initializes all fields
    // Reason: Ensures the constructor properly sets all entity attributes,
    // which is important for creating Tutorial objects with initial values
    @Test
    @DisplayName("Should create Tutorial with parameterized constructor")
    void testParameterizedConstructor() {
        Tutorial tutorial = new Tutorial("Spring Boot Basics", "Learn Spring Boot", 5, true);
        
        assertEquals("Spring Boot Basics", tutorial.getTitle());
        assertEquals("Learn Spring Boot", tutorial.getDescription());
        assertEquals(5, tutorial.getLevel());
        assertTrue(tutorial.isPublished());
    }

    // Test: Verify ID getter and setter work correctly
    // Reason: ID is the primary key and must be properly managed by JPA.
    // Testing ensures the entity can be persisted and retrieved correctly
    @Test
    @DisplayName("Should set and get ID correctly")
    void testIdGetterSetter() {
        Integer testId = 1;
        tutorial.setId(testId);
        
        assertEquals(testId, tutorial.getId());
    }

    // Test: Verify title getter and setter work correctly
    // Reason: Title is a required field (nullable=false) and must be properly
    // set and retrieved. This is critical for data integrity
    @Test
    @DisplayName("Should set and get title correctly")
    void testTitleGetterSetter() {
        String testTitle = "Java Fundamentals";
        tutorial.setTitle(testTitle);
        
        assertEquals(testTitle, tutorial.getTitle());
    }

    // Test: Verify description getter and setter work correctly
    // Reason: Description is an optional field but must be properly handled
    // when set. This ensures the entity can store optional information
    @Test
    @DisplayName("Should set and get description correctly")
    void testDescriptionGetterSetter() {
        String testDescription = "A comprehensive guide to Java programming";
        tutorial.setDescription(testDescription);
        
        assertEquals(testDescription, tutorial.getDescription());
    }

    // Test: Verify description can be set to null
    // Reason: Description is optional, so null values should be allowed.
    // This ensures the entity handles optional fields correctly
    @Test
    @DisplayName("Should allow null description")
    void testDescriptionCanBeNull() {
        tutorial.setDescription(null);
        
        assertNull(tutorial.getDescription());
    }

    // Test: Verify level getter and setter work correctly
    // Reason: Level represents difficulty/level and must be properly stored.
    // This is important for filtering and sorting tutorials by difficulty
    @Test
    @DisplayName("Should set and get level correctly")
    void testLevelGetterSetter() {
        int testLevel = 7;
        tutorial.setLevel(testLevel);
        
        assertEquals(testLevel, tutorial.getLevel());
    }

    // Test: Verify level can be set to minimum value (0)
    // Reason: According to entity comments, level should be between 0-10.
    // Testing minimum boundary ensures the constraint is properly handled
    @Test
    @DisplayName("Should accept minimum level value (0)")
    void testLevelMinimumValue() {
        tutorial.setLevel(0);
        
        assertEquals(0, tutorial.getLevel());
    }

    // Test: Verify level can be set to maximum value (10)
    // Reason: Testing maximum boundary ensures the entity accepts the full
    // valid range of level values as specified in the documentation
    @Test
    @DisplayName("Should accept maximum level value (10)")
    void testLevelMaximumValue() {
        tutorial.setLevel(10);
        
        assertEquals(10, tutorial.getLevel());
    }

    // Test: Verify published status getter and setter work correctly
    // Reason: Published status controls tutorial visibility. This boolean
    // flag must be properly managed for the publish/unpublish feature
    @Test
    @DisplayName("Should set and get published status correctly")
    void testPublishedGetterSetter() {
        tutorial.setPublished(true);
        assertTrue(tutorial.isPublished());
        
        tutorial.setPublished(false);
        assertFalse(tutorial.isPublished());
    }

    // Test: Verify toString method returns meaningful representation
    // Reason: toString is useful for debugging and logging. It should include
    // all relevant fields to help identify the entity in logs
    @Test
    @DisplayName("Should return meaningful string representation")
    void testToString() {
        tutorial.setId(1);
        tutorial.setTitle("Test Tutorial");
        tutorial.setDescription("Test Description");
        tutorial.setLevel(5);
        tutorial.setPublished(true);
        
        String result = tutorial.toString();
        
        assertTrue(result.contains("1"));
        assertTrue(result.contains("Test Tutorial"));
        assertTrue(result.contains("Test Description"));
        assertTrue(result.contains("5"));
        assertTrue(result.contains("true"));
    }

    // Test: Verify entity can be fully populated with all fields
    // Reason: Ensures the entity can handle complete data scenarios,
    // which is the normal use case in the application
    @Test
    @DisplayName("Should handle complete entity with all fields")
    void testCompleteEntity() {
        tutorial.setId(100);
        tutorial.setTitle("Complete Tutorial");
        tutorial.setDescription("This is a complete tutorial description");
        tutorial.setLevel(8);
        tutorial.setPublished(true);
        
        assertEquals(100, tutorial.getId());
        assertEquals("Complete Tutorial", tutorial.getTitle());
        assertEquals("This is a complete tutorial description", tutorial.getDescription());
        assertEquals(8, tutorial.getLevel());
        assertTrue(tutorial.isPublished());
    }

    // Test: Verify entity handles empty string title (edge case)
    // Reason: While title is required, we should verify how the entity
    // behaves with edge cases. This helps identify potential validation issues
    @Test
    @DisplayName("Should handle empty string title")
    void testEmptyStringTitle() {
        tutorial.setTitle("");
        
        assertEquals("", tutorial.getTitle());
    }

    // Test: Verify entity can handle long description (up to 256 chars)
    // Reason: Description has a max length of 256 characters. Testing with
    // a long string ensures the entity can handle the maximum allowed length
    @Test
    @DisplayName("Should handle maximum length description")
    void testMaximumLengthDescription() {
        String longDescription = "A".repeat(256);
        tutorial.setDescription(longDescription);
        
        assertEquals(256, tutorial.getDescription().length());
        assertEquals(longDescription, tutorial.getDescription());
    }

    // Test: Verify entity can handle long title (up to 128 chars)
    // Reason: Title has a max length of 128 characters. Testing ensures
    // the entity can handle the maximum allowed title length
    @Test
    @DisplayName("Should handle maximum length title")
    void testMaximumLengthTitle() {
        String longTitle = "T".repeat(128);
        tutorial.setTitle(longTitle);
        
        assertEquals(128, tutorial.getTitle().length());
        assertEquals(longTitle, tutorial.getTitle());
    }
}
