package org.gate.metropos.repositories;

import org.gate.metropos.enums.CategoryFields;
import org.gate.metropos.models.Category;
import org.jooq.*;
import org.jooq.Record;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryRepositoryTest {
    @Mock
    private DSLContext dsl;
    @Mock
    private Record record;
    @Mock
    private Result<Record> records;
    @Mock
    private SelectSelectStep<Record> selectStep;
    @Mock
    private SelectJoinStep<Record> fromStep;
    @Mock
    private SelectConditionStep<Record> conditionStep;
    @Mock
    private InsertSetStep<Record> insertStep;
    @Mock
    private InsertSetMoreStep<Record> insertSetMoreStep;
    @Mock
    private InsertResultStep<Record> insertResultStep;
    @Mock
    private UpdateSetFirstStep<Record> updateSetFirstStep;
    @Mock
    private UpdateSetMoreStep<Record> updateSetMoreStep;
    @Mock
    private UpdateConditionStep<Record> updateConditionStep;
    @Mock
    private UpdateResultStep<Record> updateResultStep;
    @Mock
    private DeleteUsingStep<Record> deleteUsingStep;
    @Mock
    private DeleteConditionStep<Record> deleteConditionStep;

    @InjectMocks
    private CategoryRepository repository;

    private Category mockCategory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock Select Query Chain
        when(dsl.select()).thenReturn(selectStep);
        when(selectStep.from((Table<?>) any())).thenReturn(fromStep);
        when(fromStep.where(any(Condition.class))).thenReturn(conditionStep);
        when(conditionStep.fetchOne()).thenReturn(record);

        // Mock Insert Chain
        when(dsl.insertInto(any(Table.class))).thenReturn(insertStep);
        when(insertStep.set(any(Field.class), any(String.class))).thenReturn(insertSetMoreStep);
        when(insertSetMoreStep.returning(any(Field.class), any(Field.class))).thenReturn(insertResultStep);
        when(insertResultStep.fetchOne()).thenReturn(record);

        // Mock Update Chain
        when(dsl.update(any(Table.class))).thenReturn(updateSetFirstStep);
        when(updateSetFirstStep.set(any(Field.class), any(String.class))).thenReturn(updateSetMoreStep);
        when(updateSetMoreStep.where(any(Condition.class))).thenReturn(updateConditionStep);
        when(updateConditionStep.returning()).thenReturn(updateResultStep);
        when(updateResultStep.fetchOne()).thenReturn(record);

        // Mock Delete Chain
        when(dsl.deleteFrom(any(Table.class))).thenReturn(deleteUsingStep);
        when(deleteUsingStep.where(any(Condition.class))).thenReturn(deleteConditionStep);
        when(deleteConditionStep.execute()).thenReturn(1);

        initializeMockCategory();
        setupRecordMocks();
    }

    private void initializeMockCategory() {
        mockCategory = Category.builder()
                .id(1L)
                .name("Test Category")
                .build();
    }

    private void setupRecordMocks() {
        when(record.get(CategoryFields.ID.toField(), Long.class)).thenReturn(mockCategory.getId());
        when(record.get(CategoryFields.NAME.toField(), String.class)).thenReturn(mockCategory.getName());
    }


    @Test
    void createCategorySuccessful() {
        String categoryName = "New Category";

        Category result = repository.createCategory(categoryName);

        assertNotNull(result);
        assertEquals(mockCategory.getId(), result.getId());
        assertEquals(mockCategory.getName(), result.getName());
    }

    @Test
    void findByNameWhenExists() {
        String name = mockCategory.getName();

        Category result = repository.findByName(name);

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(mockCategory.getId(), result.getId());
    }

    @Test
    void findByNameWhenNotExists() {
        when(conditionStep.fetchOne()).thenReturn(null);

        Category result = repository.findByName("NonExistent");

        assertNull(result);
    }

    @Test
    void findByIdWhenExists() {
        Long id = mockCategory.getId();

        Category result = repository.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(mockCategory.getName(), result.getName());
    }

    @Test
    void findByIdWhenNotExists() {
        when(conditionStep.fetchOne()).thenReturn(null);

        Category result = repository.findById(999L);

        assertNull(result);
    }

    @Test
    void getAllCategories() {
        when(fromStep.fetch()).thenReturn(records);
        when(records.map(any())).thenReturn(Arrays.asList(
                mockCategory,
                Category.builder().id(2L).name("Category 2").build()
        ));

        List<Category> results = repository.getAllCategories();

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void updateCategorySuccessful() {
        Category categoryToUpdate = Category.builder()
                .id(1L)
                .name("Updated Category")
                .build();

        Category result = repository.updateCategory(categoryToUpdate);

        assertNotNull(result);
        assertEquals(mockCategory.getId(), result.getId());
        assertEquals(mockCategory.getName(), result.getName());
    }

    @Test
    void deleteCategorySuccessful() {
        boolean result = repository.deleteCategory(1L);

        assertTrue(result);
        verify(deleteConditionStep).execute();
    }

    @Test
    void deleteCategoryFailed() {
        when(deleteConditionStep.execute()).thenReturn(0);

        boolean result = repository.deleteCategory(999L);

        assertFalse(result);
        verify(deleteConditionStep).execute();
    }
}
