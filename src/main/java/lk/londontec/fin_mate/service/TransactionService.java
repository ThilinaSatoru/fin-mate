package lk.londontec.fin_mate.service;

import lk.londontec.fin_mate.entity.Category;
import lk.londontec.fin_mate.entity.Transaction;
import lk.londontec.fin_mate.entity.User;
import lk.londontec.fin_mate.repository.CategoryRepository;
import lk.londontec.fin_mate.repository.TransactionRepository;
import lk.londontec.fin_mate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Transaction createTransaction(Long userId, Transaction txn) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        txn.setUser(user);
        return transactionRepository.save(txn);
    }

    public List<Transaction> getMonthlyTransactions(Long userId, YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);
        return transactionRepository.findByUserIdAndTransactionDateBetween(userId, start, end);
    }

    public List<Transaction> getUncategorized(Long userId) {
        return transactionRepository.findByCategoryIsNullAndUserId(userId);
    }

    @Transactional
    public Transaction assignCategory(Long transactionId, Long categoryId) {
        Transaction txn = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        txn.setCategory(category);
        return transactionRepository.save(txn);
    }

    public java.math.BigDecimal getTotalExpenses(Long userId, YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);
        return transactionRepository.sumExpensesBetween(userId, start, end);
    }

    public java.math.BigDecimal getTotalIncome(Long userId, YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);
        return transactionRepository.sumIncomeBetween(userId, start, end);
    }
}