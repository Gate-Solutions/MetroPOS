package org.gate.metropos.services;

import org.gate.metropos.models.BranchProduct;
import org.gate.metropos.models.Category;
import org.gate.metropos.models.Product;
import org.gate.metropos.repositories.BranchProductRepository;
import org.gate.metropos.repositories.CategoryRepository;
import org.gate.metropos.repositories.ProductRepository;
import org.gate.metropos.utils.ServiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private BranchProductRepository branchProductRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product mockProduct;
    private Category mockCategory;
    private BranchProduct mockBranchProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize mock category
        mockCategory = Category.builder()
                .id(1L)
                .name("Test Category")
                .build();

        // Initialize mock product
        mockProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .code("PRD001")
                .category(mockCategory)
                .originalPrice(new BigDecimal("100.00"))
                .salePrice(new BigDecimal("120.00"))
                .priceOfCarton(new BigDecimal("500.00"))
                .isActive(true)
                .build();

        // Initialize mock branch product
        mockBranchProduct = BranchProduct.builder()
                .id(1L)
                .branchId(1L)
                .productId(1L)
                .quantity(10)
                .build();

        // Setup repository mocks
        when(categoryRepository.findById(1L)).thenReturn(mockCategory);
        when(productRepository.findById(1L)).thenReturn(mockProduct);
        when(productRepository.createProduct(any(Product.class))).thenReturn(mockProduct);
        when(branchProductRepository.addProductToBranch(1L, 1L, 10)).thenReturn(mockBranchProduct);
    }


    @Test
    void createProductSuccessful() {
        when(productRepository.checkExistence(any())).thenReturn(null);

        ServiceResponse<Product> response = productService.createProduct(mockProduct);

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("Product created successfully", response.getMessage());
    }

    @Test
    void createProductWithExistingCode() {
        when(productRepository.checkExistence(any())).thenReturn(mockProduct);

        ServiceResponse<Product> response = productService.createProduct(mockProduct);

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Product with this code already exists", response.getMessage());
    }

    @Test
    void createProductWithInvalidData() {
        mockProduct.setName("");

        ServiceResponse<Product> response = productService.createProduct(mockProduct);

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Product name cannot be empty", response.getMessage());
    }

    @Test
    void addProductToBranchSuccessful() {
        ServiceResponse<BranchProduct> response = productService.addProductToBranch(1L, 1L, 10);

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(1L, response.getData().getProductId());
        assertEquals(10, response.getData().getQuantity());
    }


    @Test
    void addProductToBranchWithInvalidQuantity() {
        ServiceResponse<BranchProduct> response = productService.addProductToBranch(1L, 1L, 0);

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Quantity cannot be negative or zero", response.getMessage());
    }

    @Test
    void addProductToBranchWithNonExistentProduct() {
        when(productRepository.findById(999L)).thenReturn(null);

        ServiceResponse<BranchProduct> response = productService.addProductToBranch(1L, 999L, 10);

        assertFalse(response.isSuccess());
        assertEquals(404, response.getCode());
        assertEquals("Product not found with ID: 999", response.getMessage());
    }

    @Test
    void getAllProductsSuccessful() {
        when(productRepository.getAllProducts()).thenReturn(Arrays.asList(mockProduct));

        ServiceResponse<List<Product>> response = productService.getAllProducts();

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
    }

    @Test
    void getProductsByBranchSuccessful() {
        when(branchProductRepository.getProductsByBranch(1L))
                .thenReturn(Arrays.asList(mockBranchProduct));

        ServiceResponse<List<BranchProduct>> response = productService.getProductsByBranch(1L);

        assertTrue(response.isSuccess());
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals(1L, response.getData().get(0).getProductId());
    }

    @Test
    void validateProductDataWithInvalidCategory() {
        mockProduct.getCategory().setId(999L);
        when(categoryRepository.findById(999L)).thenReturn(null);

        ServiceResponse<Product> response = productService.createProduct(mockProduct);

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Invalid category ID: 999", response.getMessage());
    }

    @Test
    void validateProductDataWithInvalidPrices() {
        mockProduct.setSalePrice(new BigDecimal("90.00")); // Less than original price

        ServiceResponse<Product> response = productService.createProduct(mockProduct);

        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
        assertEquals("Sale price must be greater than original price", response.getMessage());
    }
}
