package org.gate.metropos.repositories.Reports;

import lombok.AllArgsConstructor;
import org.gate.metropos.config.DatabaseConfig;
import org.gate.metropos.enums.BranchFields;
import org.gate.metropos.enums.SaleFields;
import org.gate.metropos.models.Branch;
import org.gate.metropos.models.Employee;
import org.gate.metropos.models.Reports.ReportCriteria;
import org.gate.metropos.models.Reports.SalesReport;
import org.gate.metropos.models.Sale;
import org.gate.metropos.repositories.BranchRepository;
import org.gate.metropos.repositories.EmployeeRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.OffsetDateTime;
import java.util.List;

@AllArgsConstructor
public class SalesReportRepository {
    private DSLContext dsl;
    private BranchRepository branchRepository;
    private EmployeeRepository employeeRepository;
    public SalesReportRepository() {
        dsl = DatabaseConfig.getLocalDSL();
        branchRepository = new BranchRepository();
        employeeRepository = new EmployeeRepository();
    }


    public SalesReport getSalesReport(ReportCriteria criteria) {
        SelectConditionStep<Record> query = dsl
                .select()
                .from(SaleFields.toTableField())
                .leftJoin(BranchFields.toTableField()).on(SaleFields.BRANCH_ID.toField().eq(BranchFields.toTableField().field(BranchFields.ID.toField())))
                .where(DSL.noCondition());

        if (criteria.getBranchId() != null) {
            query.and(SaleFields.BRANCH_ID.toField().eq(criteria.getBranchId()));
        }
        if (criteria.getEmployeeId() != null) {
            query.and(SaleFields.CREATED_BY.toField().eq(criteria.getEmployeeId()));
        }
        if (criteria.getStartDate() != null) {
            query.and(SaleFields.INVOICE_DATE.toField().greaterOrEqual(criteria.getStartDate()));
        }
        if (criteria.getEndDate() != null) {
            query.and(SaleFields.INVOICE_DATE.toField().lessOrEqual(criteria.getEndDate()));
        }

        Result<Record> results = query.fetch();

        BigDecimal totalSales = results.stream()
                .map(r -> r.get(SaleFields.TOTAL_AMOUNT.toField(), BigDecimal.class))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscounts = results.stream()
                .map(r -> r.get(SaleFields.DISCOUNT.toField(), BigDecimal.class))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netAmount = results.stream()
                .map(r -> r.get(SaleFields.NET_AMOUNT.toField(), BigDecimal.class))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Sale> sales = results.map(this::mapToSale);


        SalesReport report = SalesReport.builder()
                .startDate(criteria.getStartDate())
                .endDate(criteria.getEndDate())
                .totalSales(totalSales)
                .totalDiscounts(totalDiscounts)
                .netAmount(netAmount)
                .details(sales)
                .build();

        if(criteria.getBranchId() != null) {
            Branch branch = branchRepository.findById(criteria.getBranchId());
            report.setBranch(branch);
        }
        if(criteria.getEmployeeId() != null) {
            Employee employee = employeeRepository.getEmployee(criteria.getEmployeeId());
            report.setEmployee(employee);
        }
        report.setStartDate(criteria.getStartDate());
        report.setEndDate(criteria.getEndDate());

        return report;
    }


    private  Sale mapToSale(Record record) {
        if (record == null) return null;

        Long branchId = record.get(SaleFields.BRANCH_ID.toField(), Long.class);
        Long createdBy = record.get(SaleFields.CREATED_BY.toField(), Long.class);

        return Sale.builder()
                .id(record.get(SaleFields.ID.toField(), Long.class))
                .invoiceNumber(record.get(SaleFields.INVOICE_NUMBER.toField(), String.class))
                .branchId(branchId)
                .createdBy(createdBy)
                .invoiceDate(record.get(SaleFields.INVOICE_DATE.toField(), Date.class).toLocalDate())
                .totalAmount(record.get(SaleFields.TOTAL_AMOUNT.toField(), BigDecimal.class))
                .discount(record.get(SaleFields.DISCOUNT.toField(), BigDecimal.class))
                .netAmount(record.get(SaleFields.NET_AMOUNT.toField(), BigDecimal.class))
                .notes(record.get(SaleFields.NOTES.toField(), String.class))
                .createdAt(record.get(SaleFields.CREATED_AT.toField(), OffsetDateTime.class).toLocalDateTime())
                .updatedAt(record.get(SaleFields.UPDATED_AT.toField(), OffsetDateTime.class).toLocalDateTime())
                .branch(branchRepository.findById(branchId))
                .creator(employeeRepository.findById(createdBy))
                .build();
    }
}
