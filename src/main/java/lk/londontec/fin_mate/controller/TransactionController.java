package lk.londontec.fin_mate.controller;

import lk.londontec.fin_mate.entity.Transaction;
import lk.londontec.fin_mate.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/{userId}")
    public Transaction create(@PathVariable Long userId, @RequestBody Transaction txn) {
        return transactionService.createTransaction(userId, txn);
    }

    @GetMapping("/{userId}/month/{yearMonth}")
    public List<Transaction> getMonthly(@PathVariable Long userId,
                                        @PathVariable String yearMonth) {
        return transactionService.getMonthlyTransactions(userId, YearMonth.parse(yearMonth));
    }

    @GetMapping("/{userId}/uncategorized")
    public List<Transaction> getUncategorized(@PathVariable Long userId) {
        return transactionService.getUncategorized(userId);
    }

    @PatchMapping("/{transactionId}/category/{categoryId}")
    public Transaction categorize(@PathVariable Long transactionId, @PathVariable Long categoryId) {
        return transactionService.assignCategory(transactionId, categoryId);
    }

    @GetMapping("/{userId}/summary/{yearMonth}")
    public MonthlySummary summary(@PathVariable Long userId, @PathVariable String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        var income = transactionService.getTotalIncome(userId, ym);
        var expenses = transactionService.getTotalExpenses(userId, ym);
        return new MonthlySummary(income, expenses, income.subtract(expenses));
    }

    public record MonthlySummary(java.math.BigDecimal income,
                                 java.math.BigDecimal expenses,
                                 java.math.BigDecimal net) {
    }
}