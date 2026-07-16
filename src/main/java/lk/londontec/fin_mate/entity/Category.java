package lk.londontec.fin_mate.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g. FOOD, TRANSPORT, UTILITIES, ENTERTAINMENT, RENT, INCOME, OTHER

    private String icon; // optional, for Flutter UI

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type; // INCOME or EXPENSE

    public enum CategoryType {INCOME, EXPENSE}
}
