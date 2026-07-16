package lk.londontec.fin_mate.service.savings;

import lk.londontec.fin_mate.dto.MonthlySummary;
import lk.londontec.fin_mate.entity.Category;
import lk.londontec.fin_mate.entity.Transaction;
import lk.londontec.fin_mate.repository.CategoryRepository;
import lk.londontec.fin_mate.repository.TransactionRepository;
import lk.londontec.fin_mate.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SavingsAdviceService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionService transactionService;

    public MonthlySummary buildMonthlySummary(Long userId, YearMonth month) {
        BigDecimal income = transactionService.getTotalIncome(userId, month);
        BigDecimal expenses = transactionService.getTotalExpenses(userId, month);
        BigDecimal net = income.subtract(expenses);

        Map<String, BigDecimal> spendByCategory = new HashMap<>();
        Map<String, BigDecimal> spendChange = new HashMap<>();

        List<Category> expenseCategories = categoryRepository.findAll().stream()
                .filter(c -> c.getType() == Category.CategoryType.EXPENSE)
                .toList();

        YearMonth previousMonth = month.minusMonths(1);

        for (Category category : expenseCategories) {
            BigDecimal currentTotal = sumForCategory(userId, category.getId(), month);
            BigDecimal previousTotal = sumForCategory(userId, category.getId(), previousMonth);

            if (currentTotal.compareTo(BigDecimal.ZERO) > 0) {
                spendByCategory.put(category.getName(), currentTotal);
            }

            spendChange.put(category.getName(), percentChange(previousTotal, currentTotal));
        }

        return new MonthlySummary(income, expenses, net, spendByCategory, spendChange);
    }

    private BigDecimal sumForCategory(Long userId, Long categoryId, YearMonth month) {
        var start = month.atDay(1).atStartOfDay();
        var end = month.atEndOfMonth().atTime(23, 59, 59);

        List<Transaction> txns = transactionRepository
                .findByUserIdAndCategoryIdAndTransactionDateBetween(userId, categoryId, start, end);

        return txns.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal percentChange(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100);
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}