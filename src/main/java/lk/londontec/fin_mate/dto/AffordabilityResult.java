package lk.londontec.fin_mate.dto;

import java.math.BigDecimal;

public record AffordabilityResult(
        BigDecimal itemCost,
        BigDecimal avgMonthlyIncome,
        BigDecimal avgMonthlyExpenses,
        BigDecimal avgMonthlyDisposable,
        BigDecimal currentSavingsRate,   // disposable / income, as %
        int monthsToSaveIfSetAside,      // months to save the full cost from disposable income alone
        boolean affordableInOneGo,       // true if they have enough net worth/savings already (if tracked)
        boolean healthyToBuyNow,         // true if buying now leaves a reasonable buffer
        String verdict                   // short human label: "AFFORDABLE", "STRETCH", "NOT_RECOMMENDED"
) {
}
