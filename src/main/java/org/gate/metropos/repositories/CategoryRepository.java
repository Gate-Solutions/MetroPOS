package org.gate.metropos.repositories;

import lombok.AllArgsConstructor;
import org.gate.metropos.config.DatabaseConfig;
import org.gate.metropos.enums.CategoryFields;
import org.gate.metropos.models.Category;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.List;

@AllArgsConstructor
public class CategoryRepository {
    private final DSLContext dsl;

    public CategoryRepository() {
        dsl = DatabaseConfig.getLocalDSL();
    }

    public Category createCategory(Category category) {
        Record record = dsl.insertInto(CategoryFields.CategoryTable.toTableField())
                .set(CategoryFields.NAME.toField(), category.getName())
                .returning()
                .fetchOne();
        return mapToCategory(record);
    }

    public Category findById(Long id) {
        Record record = dsl.select()
                .from(CategoryFields.CategoryTable.toTableField())
                .where(CategoryFields.ID.toField().eq(id))
                .fetchOne();
        return mapToCategory(record);
    }

    private Category mapToCategory(Record record) {
        if (record == null) return null;
        return Category.builder()
                .id(record.get(CategoryFields.ID.toField(), Long.class))
                .name(record.get(CategoryFields.NAME.toField(), String.class))
                .build();
    }

    public List<Category> getAllCategories() {
        Result<Record> records = dsl.select()
                .from(CategoryFields.CategoryTable.toTableField())
                .fetch();

        return records.map(this::mapToCategory);
    }


}
