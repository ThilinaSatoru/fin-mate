package lk.londontec.fin_mate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // nullable until categorized

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type; // DEBIT or CREDIT

    private String merchant; // parsed from SMS, e.g. "KEELLS SUPER"

    @Column(columnDefinition = "TEXT")
    private String rawSms; // keep original for debugging/reparsing

    private String bankSender; // e.g. "COMBANK", "SAMPATH"

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Builder.Default
    private boolean flaggedAnomaly = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TransactionType {DEBIT, CREDIT}
}