package lk.londontec.fin_mate.controller;

import lk.londontec.fin_mate.entity.Transaction;
import lk.londontec.fin_mate.security.AppUserPrincipal;
import lk.londontec.fin_mate.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public Transaction create(@AuthenticationPrincipal AppUserPrincipal principal,
                              @RequestBody Transaction txn) {
        return transactionService.createTransaction(principal.getUserId(), txn);
    }

    @GetMapping("/month/{yearMonth}")
    public List<Transaction> getMonthly(@AuthenticationPrincipal AppUserPrincipal principal,
                                        @PathVariable String yearMonth) {
        return transactionService.getMonthlyTransactions(principal.getUserId(), YearMonth.parse(yearMonth));
    }

    @GetMapping("/uncategorized")
    public List<Transaction> getUncategorized(@AuthenticationPrincipal AppUserPrincipal principal) {
        return transactionService.getUncategorized(principal.getUserId());
    }

    @PatchMapping("/{transactionId}/category/{categoryId}")
    public Transaction categorize(@AuthenticationPrincipal AppUserPrincipal principal,
                                  @PathVariable Long transactionId,
                                  @PathVariable Long categoryId) {
        return transactionService.assignCategory(principal.getUserId(), transactionId, categoryId);
    }

    @GetMapping("/summary/{yearMonth}")
    public MonthlySummary summary(@AuthenticationPrincipal AppUserPrincipal principal,
                                  @PathVariable String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        var income = transactionService.getTotalIncome(principal.getUserId(), ym);
        var expenses = transactionService.getTotalExpenses(principal.getUserId(), ym);
        return new MonthlySummary(income, expenses, income.subtract(expenses));
    }

    public record MonthlySummary(java.math.BigDecimal income,
                                 java.math.BigDecimal expenses,
                                 java.math.BigDecimal net) {
    }
}