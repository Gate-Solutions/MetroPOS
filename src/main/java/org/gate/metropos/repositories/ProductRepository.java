package org.gate.metropos.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.gate.metropos.config.DatabaseConfig;
import org.gate.metropos.enums.ProductFields;
import org.gate.metropos.models.Category;
import org.gate.metropos.models.Product;
import org.gate.metropos.services.SyncService;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class ProductRepository {
    private final DSLContext dsl;
    private final CategoryRepository categoryRepository;
    private final SyncService syncService;
    private final ObjectMapper objectMapper;
    public ProductRepository() {
        dsl = DatabaseConfig.getLocalDSL();
        categoryRepository = new CategoryRepository();
        syncService = new SyncService();
        objectMapper = new ObjectMapper();
    }

    public Product findById(Long id) {
        Record record = dsl.select()
                .from(ProductFields.toTableField())
                .where(ProductFields.ID.toField().eq(id))
                .fetchOne();
        return mapToProduct(record);
    }

    public Product checkExistence(String code) {
        Record record = dsl.select()
                .from(ProductFields.toTableField())
                .where(ProductFields.CODE.toField().eq(code))
                .fetchOne();
        return mapToProduct(record);
    }

    public Product createProduct(Product product) {
        Record record = dsl.insertInto(ProductFields.toTableField())
                .set(ProductFields.NAME.toField(), product.getName())
                .set(ProductFields.CODE.toField(), product.getCode())
                .set(ProductFields.CATEGORY_ID.toField(), product.getCategory().getId())
                .set(ProductFields.ORIGINAL_PRICE.toField(), product.getOriginalPrice())
                .set(ProductFields.SALE_PRICE.toField(), product.getSalePrice())
                .set(ProductFields.PRICE_OF_CARTON.toField(), product.getPriceOfCarton())
                .set(ProductFields.IS_ACTIVE.toField(), product.isActive())
                .returning(
                        ProductFields.ID.toField(),
                        ProductFields.NAME.toField(),
                        ProductFields.CODE.toField(),
                        ProductFields.CATEGORY_ID.toField(),
                        ProductFields.ORIGINAL_PRICE.toField(),
                        ProductFields.SALE_PRICE.toField(),
                        ProductFields.PRICE_OF_CARTON.toField(),
                        ProductFields.IS_ACTIVE.toField()
                )
                .fetchOne();

        if(record == null) return null;

        int id = record.get(ProductFields.ID.toField(), Integer.class);

        try {
            Map<String, Object> fieldValues = new HashMap<>();
            fieldValues.put("name", product.getName());
            fieldValues.put("code", product.getCode());
            fieldValues.put("category_id", product.getCategory().getId());
            fieldValues.put("original_price", product.getOriginalPrice());
            fieldValues.put("sale_price", product.getSalePrice());
            fieldValues.put("price_of_carton", product.getPriceOfCarton());
            fieldValues.put("is_active", product.isActive());

            syncService.trackChange(
                    "products",
                    id,
                    "insert",
                    objectMapper.writeValueAsString(fieldValues)
            );
        } catch (JsonProcessingException e) {
            System.out.println(e);
        }

        return mapToProduct(record);
    }

    public Product updateProduct(Product product) {
        Record record = dsl.update(ProductFields.toTableField())
                .set(ProductFields.NAME.toField(), product.getName())
                .set(ProductFields.CATEGORY_ID.toField(), product.getCategory().getId())
                .set(ProductFields.ORIGINAL_PRICE.toField(), product.getOriginalPrice())
                .set(ProductFields.SALE_PRICE.toField(), product.getSalePrice())
                .set(ProductFields.PRICE_OF_CARTON.toField(), product.getPriceOfCarton())
                .set(ProductFields.IS_ACTIVE.toField(), product.isActive())
                .where(ProductFields.ID.toField().eq(product.getId()))
                .returning(
                        ProductFields.ID.toField(),
                        ProductFields.NAME.toField(),
                        ProductFields.CODE.toField(),
                        ProductFields.CATEGORY_ID.toField(),
                        ProductFields.ORIGINAL_PRICE.toField(),
                        ProductFields.SALE_PRICE.toField(),
                        ProductFields.PRICE_OF_CARTON.toField(),
                        ProductFields.IS_ACTIVE.toField()
                )
                .fetchOne();

        try {
            Map<String, Object> fieldValues = new HashMap<>();
            fieldValues.put("name", product.getName());
            fieldValues.put("category_id", product.getCategory().getId());
            fieldValues.put("original_price", product.getOriginalPrice());
            fieldValues.put("sale_price", product.getSalePrice());
            fieldValues.put("price_of_carton", product.getPriceOfCarton());
            fieldValues.put("is_active", product.isActive());

            syncService.trackChange(
                    "products",
                    product.getId().intValue(),
                    "update",
                    objectMapper.writeValueAsString(fieldValues)
            );
        } catch (JsonProcessingException e) {
            System.out.println(e);
        }

        return mapToProduct(record);
    }

    public List<Product> getAllProducts() {
        Result<Record> records = dsl.select()
                .from(ProductFields.toTableField())
                .fetch();
        return records.map(this::mapToProduct);
    }

    private Product mapToProduct(Record record) {
        if (record == null) return null;
        Long categoryId = record.get(ProductFields.CATEGORY_ID.toField(), Long.class);
        // Fetching category by using category id
        Category category = categoryRepository.findById(categoryId);
        if (category == null) return null;
        return Product.builder()
                .id(record.get(ProductFields.ID.toField(), Long.class))
                .name(record.get(ProductFields.NAME.toField(), String.class))
                .code(record.get(ProductFields.CODE.toField(), String.class))
                .category(category)
                .originalPrice(record.get(ProductFields.ORIGINAL_PRICE.toField(), BigDecimal.class))
                .salePrice(record.get(ProductFields.SALE_PRICE.toField(), BigDecimal.class))
                .priceOfCarton(record.get(ProductFields.PRICE_OF_CARTON.toField(), BigDecimal.class))
                .isActive(record.get(ProductFields.IS_ACTIVE.toField(), Boolean.class))
                .build();
    }


}
