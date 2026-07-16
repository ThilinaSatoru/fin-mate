package lk.londontec.fin_mate.service.affordabilty;

import lk.londontec.fin_mate.dto.AffordabilityResult;
import lk.londontec.fin_mate.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class AffordabilityService {

    private static final int LOOKBACK_MONTHS = 3;
    // If buying now would eat more than this % of monthly disposable income, flag as risky
    private static final BigDecimal STRETCH_THRESHOLD_MONTHS = BigDecimal.valueOf(6);
    private final TransactionService transactionService;

    public AffordabilityResult evaluate(Long userId, BigDecimal itemCost) {
        YearMonth current = YearMonth.now();

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (int i = 1; i <= LOOKBACK_MONTHS; i++) {
            YearMonth month = current.minusMonths(i);
            totalIncome = totalIncome.add(transactionService.getTotalIncome(userId, month));
            totalExpenses = totalExpenses.add(transactionService.getTotalExpenses(userId, month));
        }

        BigDecimal avgIncome = divide(totalIncome, LOOKBACK_MONTHS);
        BigDecimal avgExpenses = divide(totalExpenses, LOOKBACK_MONTHS);
        BigDecimal avgDisposable = avgIncome.subtract(avgExpenses);

        BigDecimal savingsRate = avgIncome.compareTo(BigDecimal.ZERO) > 0
                ? avgDisposable.divide(avgIncome, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        int monthsToSave = avgDisposable.compareTo(BigDecimal.ZERO) > 0
                ? itemCost.divide(avgDisposable, 0, RoundingMode.CEILING).intValue()
                : Integer.MAX_VALUE;

        boolean healthyToBuyNow = avgDisposable.compareTo(BigDecimal.ZERO) > 0
                && BigDecimal.valueOf(monthsToSave).compareTo(STRETCH_THRESHOLD_MONTHS) <= 0;

        String verdict;
        if (avgDisposable.compareTo(BigDecimal.ZERO) <= 0) {
            verdict = "NOT_RECOMMENDED";
        } else if (monthsToSave <= 3) {
            verdict = "AFFORDABLE";
        } else if (monthsToSave <= 6) {
            verdict = "STRETCH";
        } else {
            verdict = "NOT_RECOMMENDED";
        }

        return new AffordabilityResult(
                itemCost,
                avgIncome,
                avgExpenses,
                avgDisposable,
                savingsRate,
                monthsToSave == Integer.MAX_VALUE ? -1 : monthsToSave,
                false, // affordableInOneGo — wire up once you track a savings/balance figure
                healthyToBuyNow,
                verdict
        );
    }

    private BigDecimal divide(BigDecimal total, int months) {
        return total.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
    }
}