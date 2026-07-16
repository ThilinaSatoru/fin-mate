package lk.londontec.fin_mate.dto;

import java.math.BigDecimal;
import java.util.Map;

public record MonthlySummary(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netSavings,
        Map<String, BigDecimal> spendByCategory,   // category name -> total spent
        Map<String, BigDecimal> spendChangeVsLastMonth // category name -> % change (+/-)
) {
}
