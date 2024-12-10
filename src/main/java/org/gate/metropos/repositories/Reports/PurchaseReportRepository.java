package org.gate.metropos.repositories.Reports;

import lombok.AllArgsConstructor;
import org.gate.metropos.config.DatabaseConfig;
import org.gate.metropos.enums.BranchFields;
import org.gate.metropos.enums.PurchaseInvoice.PurchaseInvoiceFields;
import org.gate.metropos.models.Branch;
import org.gate.metropos.models.Employee;
import org.gate.metropos.models.PurchaseInvoice.PurchaseInvoice;
import org.gate.metropos.models.Reports.PurchaseReport;
import org.gate.metropos.models.Reports.ReportCriteria;
import org.gate.metropos.repositories.BranchRepository;
import org.gate.metropos.repositories.EmployeeRepository;
import org.gate.metropos.repositories.SupplierRepository;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.Record;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.OffsetDateTime;
import java.util.List;

@AllArgsConstructor
public class PurchaseReportRepository {
    private DSLContext dsl;
    private BranchRepository branchRepository;
    private EmployeeRepository employeeRepository;
    private SupplierRepository supplierRepository;

    public PurchaseReportRepository() {
        dsl = DatabaseConfig.getLocalDSL();
        branchRepository = new BranchRepository();
        employeeRepository = new EmployeeRepository();
        supplierRepository = new SupplierRepository();
    }

    public PurchaseReport getPurchaseReport(ReportCriteria criteria) {
        SelectConditionStep<Record> query = dsl
                .select()
                .from(PurchaseInvoiceFields.toTableField())
                .leftJoin(BranchFields.toTableField())
                .on(PurchaseInvoiceFields.BRANCH_ID.toField()
                        .eq(BranchFields.toTableField().field(BranchFields.ID.toField())))
                .where(DSL.noCondition());

        if (criteria.getBranchId() != null) {
            query.and(PurchaseInvoiceFields.BRANCH_ID.toField().eq(criteria.getBranchId()));
        }
        if (criteria.getEmployeeId() != null) {
            query.and(PurchaseInvoiceFields.CREATED_BY.toField().eq(criteria.getEmployeeId()));
        }
        if (criteria.getStartDate() != null) {
            query.and(PurchaseInvoiceFields.INVOICE_DATE.toField().greaterOrEqual(criteria.getStartDate()));
        }
        if (criteria.getEndDate() != null) {
            query.and(PurchaseInvoiceFields.INVOICE_DATE.toField().lessOrEqual(criteria.getEndDate()));
        }

        Result<Record> results = query.fetch();

        BigDecimal totalPurchases = results.stream()
                .map(r -> r.get(PurchaseInvoiceFields.TOTAL_AMOUNT.toField(), BigDecimal.class))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<PurchaseInvoice> purchases = results.map(this::mapToPurchaseInvoice);

        Branch branch = null;
        if (criteria.getBranchId() != null) {
            branch = branchRepository.findById(criteria.getBranchId());
        }
        Employee employee = null;
        if(criteria.getEmployeeId() != null) {
            employee = employeeRepository.getEmployee(criteria.getEmployeeId());
        }

        return PurchaseReport.builder()
                .branch(branch)
                .employee(employee)
                .startDate(criteria.getStartDate())
                .endDate(criteria.getEndDate())
                .totalPurchases(totalPurchases)
                .details(purchases)
                .build();
    }

    private PurchaseInvoice mapToPurchaseInvoice(Record record) {
        if (record == null) return null;

        Long branchId = record.get(PurchaseInvoiceFields.BRANCH_ID.toField(), Long.class);
        Long createdBy = record.get(PurchaseInvoiceFields.CREATED_BY.toField(), Long.class);
        Long supplierId = record.get(PurchaseInvoiceFields.SUPPLIER_ID.toField(), Long.class);

        return PurchaseInvoice.builder()
                .id(record.get(PurchaseInvoiceFields.ID.toField(), Long.class))
                .invoiceNumber(record.get(PurchaseInvoiceFields.INVOICE_NUMBER.toField(), String.class))
                .supplierId(supplierId)
                .branchId(branchId)
                .createdBy(createdBy)
                .invoiceDate(record.get(PurchaseInvoiceFields.INVOICE_DATE.toField(), Date.class).toLocalDate())
                .totalAmount(record.get(PurchaseInvoiceFields.TOTAL_AMOUNT.toField(), BigDecimal.class))
                .notes(record.get(PurchaseInvoiceFields.NOTES.toField(), String.class))
                .createdAt(record.get(PurchaseInvoiceFields.CREATED_AT.toField(), OffsetDateTime.class).toLocalDateTime())
                .updatedAt(record.get(PurchaseInvoiceFields.UPDATED_AT.toField(), OffsetDateTime.class).toLocalDateTime())
                .supplier(supplierRepository.findById(supplierId))
                .branch(branchRepository.findById(branchId))
                .creator(employeeRepository.findById(createdBy))
                .build();
    }
}
