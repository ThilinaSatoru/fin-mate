package lk.londontec.fin_mate.service;

import lk.londontec.fin_mate.component.KeywordCategoryRules;
import lk.londontec.fin_mate.entity.Category;
import lk.londontec.fin_mate.entity.MerchantCategoryCache;
import lk.londontec.fin_mate.entity.Transaction;
import lk.londontec.fin_mate.llm.OpenAiCategorizationClient;
import lk.londontec.fin_mate.repository.CategoryRepository;
import lk.londontec.fin_mate.repository.MerchantCategoryCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategorizationService {

    private final KeywordCategoryRules keywordRules;
    private final OpenAiCategorizationClient openAiClient;
    private final CategoryRepository categoryRepository;
    private final MerchantCategoryCacheRepository cacheRepository;

    @Transactional
    public Category categorize(Transaction txn) {
        String merchant = txn.getMerchant();

        // 1. Rule-based match — free, instant
        String ruleMatch = keywordRules.match(merchant);
        if (ruleMatch != null) {
            return resolveCategory(ruleMatch);
        }

        // 2. Cache — have we already asked the LLM about this exact merchant string?
        var cached = cacheRepository.findByMerchant(merchant.toUpperCase());
        if (cached.isPresent()) {
            return resolveCategory(cached.get().getCategoryName());
        }

        // 3. LLM fallback — costs money/latency, so cache the result
        String llmCategory = openAiClient.categorize(merchant, txn.getAmount());
        cacheRepository.save(MerchantCategoryCache.builder()
                .merchant(merchant.toUpperCase())
                .categoryName(llmCategory)
                .build());

        return resolveCategory(llmCategory);
    }

    private Category resolveCategory(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.findByName("OTHER")
                        .orElseThrow(() -> new IllegalStateException("Seed data missing OTHER category")));
    }
}
