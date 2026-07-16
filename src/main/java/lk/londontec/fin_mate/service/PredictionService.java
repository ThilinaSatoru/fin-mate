package lk.londontec.fin_mate.service;

import lk.londontec.fin_mate.entity.Category;
import lk.londontec.fin_mate.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private static final int LOOKBACK_MONTHS = 3;
    private final TransactionRepository transactionRepository;

    /**
     * Predicts next month's spend for a given category using a weighted moving average
     * over the last LOOKBACK_MONTHS. More recent months are weighted higher.
     */
    public BigDecimal predictNextMonthSpend(Long userId, Long categoryId) {
        YearMonth current = YearMonth.now();
        List<BigDecimal> monthlyTotals = new ArrayList<>();

        for (int i = LOOKBACK_MONTHS; i >= 1; i--) {
            YearMonth month = current.minusMonths(i);
            var start = month.atDay(1).atStartOfDay();
            var end = month.atEndOfMonth().atTime(23, 59, 59);

            List<lk.londontec.fin_mate.entity.Transaction> txns =
                    transactionRepository.findByUserIdAndCategoryIdAndTransactionDateBetween(
                            userId, categoryId, start, end);

            BigDecimal monthTotal = txns.stream()
                    .map(lk.londontec.fin_mate.entity.Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            monthlyTotals.add(monthTotal);
        }

        return weightedAverage(monthlyTotals);
    }

    /**
     * Weighted average — most recent month counts more.
     * For 3 months [oldest, middle, newest] weights are [1, 2, 3].
     */
    private BigDecimal weightedAverage(List<BigDecimal> values) {
        if (values.isEmpty()) return BigDecimal.ZERO;

        BigDecimal weightedSum = BigDecimal.ZERO;
        int totalWeight = 0;

        for (int i = 0; i < values.size(); i++) {
            int weight = i + 1; // 1, 2, 3...
            weightedSum = weightedSum.add(values.get(i).multiply(BigDecimal.valueOf(weight)));
            totalWeight += weight;
        }

        if (totalWeight == 0) return BigDecimal.ZERO;

        return weightedSum.divide(BigDecimal.valueOf(totalWeight), 2, RoundingMode.HALF_UP);
    }

    /**
     * Predicts spend across ALL expense categories for next month.
     * Useful for the dashboard's "predicted total spend" widget.
     */
    public BigDecimal predictTotalMonthlySpend(Long userId, List<Category> expenseCategories) {
        BigDecimal total = BigDecimal.ZERO;
        for (Category category : expenseCategories) {
            total = total.add(predictNextMonthSpend(userId, category.getId()));
        }
        return total;
    }
}