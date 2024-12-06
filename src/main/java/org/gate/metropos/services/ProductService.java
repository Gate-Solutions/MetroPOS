package org.gate.metropos.services;

import lombok.AllArgsConstructor;
import org.gate.metropos.models.BranchProduct;
import org.gate.metropos.models.Category;
import org.gate.metropos.models.Product;
import org.gate.metropos.repositories.BranchProductRepository;
import org.gate.metropos.repositories.CategoryRepository;
import org.gate.metropos.repositories.ProductRepository;
import org.gate.metropos.utils.ServiceResponse;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final BranchProductRepository branchProductRepository;
    private final CategoryRepository categoryRepository;
    public ProductService() {
        this.productRepository = new ProductRepository();
        this.branchProductRepository = new BranchProductRepository();
        this.categoryRepository = new CategoryRepository();
    }

    public ServiceResponse<Product> createProduct(Product product) {
        Product existingProduct = productRepository.checkExistence(product.getCode());
        if (existingProduct != null) {
            return new ServiceResponse<>(false, 400, "Product with this code already exists", null);
        }
        try {
            validateProductData(product);
        } catch (IllegalArgumentException e) {
            return new ServiceResponse<>(false, 400, e.getMessage(), null);
        }
        Product newProduct = productRepository.createProduct(product);
        return new ServiceResponse<>(true, 200, "Product created successfully", newProduct);
    }

    public ServiceResponse<Product> updateProduct(Product product) {
        try {
            validateProductData(product);
        } catch (IllegalArgumentException e) {
            return new ServiceResponse<>(false, 400, e.getMessage(), null);
        }
        Product updatedProduct = productRepository.updateProduct(product);
        return new ServiceResponse<>(true, 200, "Product updated successfully", updatedProduct);
    }

    public ServiceResponse<BranchProduct> addProductToBranch(Long branchId, Long productId, Integer quantity) {

        if (quantity <= 0) {
            return new ServiceResponse<>(false, 400, "Quantity cannot be negative or zero", null);
        }


        Product product = productRepository.findById(productId);
        if (product == null) {
            return new ServiceResponse<>(false, 404, "Product not found with ID: " + productId, null);
        }


        BranchProduct branchProduct = branchProductRepository.addProductToBranch(branchId, productId, quantity);
        return new ServiceResponse<>(true, 200, "Product added to branch successfully", branchProduct);
    }

    public ServiceResponse<Product> findById(Long id) {
        Product product = productRepository.findById(id);
        if (product == null) {
            return new ServiceResponse<>(false, 404, "Product not found with ID: " + id, null);
        }
        return new ServiceResponse<>(true, 200, "Product found successfully", product);
    }

    public ServiceResponse<List<Product>> getAllProducts() {
        List<Product> products = productRepository.getAllProducts();
        return new ServiceResponse<>(true, 200, "Products retrieved successfully", products);
    }

    public ServiceResponse<List<BranchProduct>> getProductsByBranch(Long branchId) {
        List<BranchProduct> products = branchProductRepository.getProductsByBranch(branchId);
        return new ServiceResponse<>(true, 200, "Branch products retrieved successfully", products);
    }

    private void validateProductData(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (product.getCode() == null || product.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Product code cannot be empty");
        }
        if (product.getOriginalPrice() == null || product.getOriginalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Original price must be greater than zero");
        }
        if (product.getSalePrice() == null || product.getSalePrice().compareTo(product.getOriginalPrice()) <= 0) {
            throw new IllegalArgumentException("Sale price must be greater than original price");
        }
        if (product.getPriceOfCarton() == null || product.getPriceOfCarton().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Carton price must be greater than zero");
        }
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
        Category category = categoryRepository.findById(product.getCategory().getId());
        if (category == null) {
            throw new IllegalArgumentException("Invalid category ID: " + product.getCategory().getId());
        }
    }
}
