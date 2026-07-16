package lk.londontec.fin_mate.controller;

import lk.londontec.fin_mate.entity.Category;
import lk.londontec.fin_mate.repository.CategoryRepository;
import lk.londontec.fin_mate.security.AppUserPrincipal;
import lk.londontec.fin_mate.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/category/{categoryId}")
    public Map<String, BigDecimal> predictCategory(@AuthenticationPrincipal AppUserPrincipal principal,
                                                   @PathVariable Long categoryId) {
        BigDecimal predicted = predictionService.predictNextMonthSpend(principal.getUserId(), categoryId);
        return Map.of("predictedSpend", predicted);
    }

    @GetMapping("/all")
    public Map<String, BigDecimal> predictAllCategories(@AuthenticationPrincipal AppUserPrincipal principal) {
        List<Category> expenseCategories = categoryRepository.findAll().stream()
                .filter(c -> c.getType() == Category.CategoryType.EXPENSE)
                .toList();

        return expenseCategories.stream()
                .collect(Collectors.toMap(
                        Category::getName,
                        c -> predictionService.predictNextMonthSpend(principal.getUserId(), c.getId())
                ));
    }

    @GetMapping("/total")
    public Map<String, BigDecimal> predictTotal(@AuthenticationPrincipal AppUserPrincipal principal) {
        List<Category> expenseCategories = categoryRepository.findAll().stream()
                .filter(c -> c.getType() == Category.CategoryType.EXPENSE)
                .toList();

        BigDecimal total = predictionService.predictTotalMonthlySpend(principal.getUserId(), expenseCategories);
        return Map.of("predictedTotalSpend", total);
    }
}