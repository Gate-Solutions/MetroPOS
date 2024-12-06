package org.gate.metropos.services;


import lombok.AllArgsConstructor;
import org.gate.metropos.models.Reports.PurchaseReport;
import org.gate.metropos.models.Reports.ReportCriteria;
import org.gate.metropos.models.Reports.SalesReport;
import org.gate.metropos.models.Reports.StockReport;
import org.gate.metropos.repositories.Reports.PurchaseReportRepository;
import org.gate.metropos.repositories.Reports.SalesReportRepository;
import org.gate.metropos.repositories.Reports.StockReportRepository;

import java.time.LocalDate;

@AllArgsConstructor
public class ReportsService {

    private SalesReportRepository salesReportRepository;
    private PurchaseReportRepository purchaseReportRepository;
    private StockReportRepository stockReportRepository;

    public ReportsService() {
        salesReportRepository = new SalesReportRepository();
        purchaseReportRepository = new PurchaseReportRepository();
        stockReportRepository = new StockReportRepository();
    }

    public SalesReport getSalesReport(ReportCriteria criteria) {
        return salesReportRepository.getSalesReport(criteria);
    }

    public SalesReport getDailyReport(LocalDate date, Long branchId) {
        ReportCriteria criteria = ReportCriteria.builder()
                .startDate(date)
                .endDate(date)
                .branchId(branchId)
                .build();
        return getSalesReport(criteria);
    }

    public SalesReport getMonthlyReport(int year, int month, Long branchId) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        ReportCriteria criteria = ReportCriteria.builder()
                .startDate(startDate)
                .endDate(endDate)
                .branchId(branchId)
                .build();
        return getSalesReport(criteria);
    }

    public SalesReport getYearlyReport(int year, Long branchId) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        ReportCriteria criteria = ReportCriteria.builder()
                .startDate(startDate)
                .endDate(endDate)
                .branchId(branchId)
                .build();
        return getSalesReport(criteria);
    }

    public PurchaseReport getDailyPurchaseReport(LocalDate date, Long branchId, Long employeeId) {
        ReportCriteria criteria = ReportCriteria.builder()
                .startDate(date)
                .endDate(date)
                .branchId(branchId)
                .employeeId(employeeId)
                .build();
        return getPurchaseReport(criteria);
    }

    public PurchaseReport getMonthlyPurchaseReport(int year, int month, Long branchId, Long employeeId) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        ReportCriteria criteria = ReportCriteria.builder()
                .startDate(startDate)
                .endDate(endDate)
                .branchId(branchId)
                .employeeId(employeeId)
                .build();
        return getPurchaseReport(criteria);
    }

    public PurchaseReport getYearlyPurchaseReport(int year, Long branchId, Long employeeId) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        ReportCriteria criteria = ReportCriteria.builder()
                .startDate(startDate)
                .endDate(endDate)
                .branchId(branchId)
                .employeeId(employeeId)
                .build();
        return getPurchaseReport(criteria);
    }

    public PurchaseReport getPurchaseReport(ReportCriteria criteria) {
        return purchaseReportRepository.getPurchaseReport(criteria);
    }

    public StockReport getCurrentStockReport(Long branchId) {
        ReportCriteria criteria = ReportCriteria.builder()
                .branchId(branchId)
                .build();
        return getStockReport(criteria);
    }

    public StockReport getStockReport() {
        return this.getStockReport(new ReportCriteria());
    }

    public StockReport getStockReport(ReportCriteria criteria) {
        return stockReportRepository.getStockReport(criteria);
    }

}
