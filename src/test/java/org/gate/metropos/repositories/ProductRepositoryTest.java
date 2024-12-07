package org.gate.metropos.repositories;

import org.gate.metropos.enums.ProductFields;
import org.gate.metropos.models.Category;
import org.gate.metropos.models.Product;
import org.jooq.*;
import org.jooq.Record;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ProductRepositoryTest {
    @Mock
    private DSLContext dsl;
    @Mock
    private CategoryRepository categoryRepository;
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

    @InjectMocks
    private ProductRepository repository;

    private Product mockProduct;
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
        when(insertStep.set(any(Field.class), Optional.ofNullable(any()))).thenReturn(insertSetMoreStep);
        when(insertSetMoreStep.set(any(Field.class), Optional.ofNullable(any()))).thenReturn(insertSetMoreStep);
        when(insertSetMoreStep.returning(any(Field.class), any(Field.class), any(Field.class),
                any(Field.class), any(Field.class), any(Field.class), any(Field.class),
                any(Field.class))).thenReturn(insertResultStep);
        when(insertResultStep.fetchOne()).thenReturn(record);

        initializeMockData();
        setupRecordMocks();
    }

    @Test
    void findByIdWhenRecordExists() {
        Long id = mockProduct.getId();
        when(categoryRepository.findById(mockCategory.getId())).thenReturn(mockCategory);

        Product result = repository.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(mockProduct.getCode(), result.getCode());
        assertEquals(mockProduct.getName(), result.getName());
        assertEquals(mockCategory.getId(), result.getCategory().getId());
    }

    @Test
    void findByIdWhenRecordDoesNotExist() {
        when(conditionStep.fetchOne()).thenReturn(null);

        Product result = repository.findById(999L);

        assertNull(result);
    }

    @Test
    void checkExistenceWhenProductExists() {
        String code = mockProduct.getCode();
        when(categoryRepository.findById(mockCategory.getId())).thenReturn(mockCategory);

        Product result = repository.checkExistence(code);

        assertNotNull(result);
        assertEquals(code, result.getCode());
        assertEquals(mockProduct.getName(), result.getName());
    }

    @Test
    void createProduct() {
        when(categoryRepository.findById(mockCategory.getId())).thenReturn(mockCategory);

        Product result = repository.createProduct(mockProduct);

        assertNotNull(result);
        assertEquals(mockProduct.getCode(), result.getCode());
        assertEquals(mockProduct.getName(), result.getName());
        assertEquals(mockProduct.getOriginalPrice(), result.getOriginalPrice());
        assertTrue(result.isActive());
    }

    @Test
    void getAllProducts() {
        when(fromStep.fetch()).thenReturn(records);
        when(categoryRepository.findById(mockCategory.getId())).thenReturn(mockCategory);
        when(records.map(any())).thenReturn(Arrays.asList(
                createTestProduct(1L, "P1"),
                createTestProduct(2L, "P2")
        ));

        List<Product> results = repository.getAllProducts();

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    private void initializeMockData() {
        mockCategory = Category.builder()
                .id(1L)
                .name("Test Category")
                .build();

        mockProduct = createTestProduct(1L, "TEST");
    }

    private Product createTestProduct(Long id, String prefix) {
        return Product.builder()
                .id(id)
                .name(prefix + " Product")
                .code(prefix + "001")
                .category(mockCategory)
                .originalPrice(new BigDecimal("100.00"))
                .salePrice(new BigDecimal("120.00"))
                .priceOfCarton(new BigDecimal("500.00"))
                .isActive(true)
                .build();
    }

    private void setupRecordMocks() {
        when(record.get(ProductFields.ID.toField(), Long.class)).thenReturn(mockProduct.getId());
        when(record.get(ProductFields.NAME.toField(), String.class)).thenReturn(mockProduct.getName());
        when(record.get(ProductFields.CODE.toField(), String.class)).thenReturn(mockProduct.getCode());
        when(record.get(ProductFields.CATEGORY_ID.toField(), Long.class)).thenReturn(mockProduct.getCategory().getId());
        when(record.get(ProductFields.ORIGINAL_PRICE.toField(), BigDecimal.class)).thenReturn(mockProduct.getOriginalPrice());
        when(record.get(ProductFields.SALE_PRICE.toField(), BigDecimal.class)).thenReturn(mockProduct.getSalePrice());
        when(record.get(ProductFields.PRICE_OF_CARTON.toField(), BigDecimal.class)).thenReturn(mockProduct.getPriceOfCarton());
        when(record.get(ProductFields.IS_ACTIVE.toField(), Boolean.class)).thenReturn(mockProduct.isActive());
    }
}
