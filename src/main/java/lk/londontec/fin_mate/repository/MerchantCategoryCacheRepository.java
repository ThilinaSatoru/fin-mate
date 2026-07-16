package lk.londontec.fin_mate.repository;

import lk.londontec.fin_mate.entity.MerchantCategoryCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantCategoryCacheRepository extends JpaRepository<MerchantCategoryCache, Long> {
    Optional<MerchantCategoryCache> findByMerchant(String merchant);
}
