package lk.londontec.fin_mate.repository;

import lk.londontec.fin_mate.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdAndTransactionDateBetween(
            Long userId, LocalDateTime start, LocalDateTime end);

    List<Transaction> findByUserIdAndCategoryIdAndTransactionDateBetween(
            Long userId, Long categoryId, LocalDateTime start, LocalDateTime end);

    List<Transaction> findByCategoryIsNullAndUserId(Long userId); // uncategorized queue

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.user.id = :userId AND t.type = 'DEBIT'
            AND t.transactionDate BETWEEN :start AND :end
            """)
    BigDecimal sumExpensesBetween(@Param("userId") Long userId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.user.id = :userId AND t.type = 'CREDIT'
            AND t.transactionDate BETWEEN :start AND :end
            """)
    BigDecimal sumIncomeBetween(@Param("userId") Long userId,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);
}
