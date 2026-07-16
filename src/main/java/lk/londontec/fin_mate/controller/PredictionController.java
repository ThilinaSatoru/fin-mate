package lk.londontec.fin_mate.controller;

import lk.londontec.fin_mate.entity.Category;
import lk.londontec.fin_mate.repository.CategoryRepository;
import lk.londontec.fin_mate.service.PredictionService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/{userId}/category/{categoryId}")
    public Map<String, BigDecimal> predictCategory(@PathVariable Long userId,
                                                   @PathVariable Long categoryId) {
        BigDecimal predicted = predictionService.predictNextMonthSpend(userId, categoryId);
        return Map.of("predictedSpend", predicted);
    }

    @GetMapping("/{userId}/all")
    public Map<String, BigDecimal> predictAllCategories(@PathVariable Long userId) {
        List<Category> expenseCategories = categoryRepository.findAll().stream()
                .filter(c -> c.getType() == Category.CategoryType.EXPENSE)
                .toList();

        return expenseCategories.stream()
                .collect(Collectors.toMap(
                        Category::getName,
                        c -> predictionService.predictNextMonthSpend(userId, c.getId())
                ));
    }

    @GetMapping("/{userId}/total")
    public Map<String, BigDecimal> predictTotal(@PathVariable Long userId) {
        List<Category> expenseCategories = categoryRepository.findAll().stream()
                .filter(c -> c.getType() == Category.CategoryType.EXPENSE)
                .toList();

        BigDecimal total = predictionService.predictTotalMonthlySpend(userId, expenseCategories);
        return Map.of("predictedTotalSpend", total);
    }
}