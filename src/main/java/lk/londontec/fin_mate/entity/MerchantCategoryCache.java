package lk.londontec.fin_mate.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "merchant_category_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantCategoryCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String merchant; // uppercased merchant string

    @Column(nullable = false)
    private String categoryName;
}