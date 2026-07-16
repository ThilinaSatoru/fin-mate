package lk.londontec.fin_mate.component;

import lk.londontec.fin_mate.entity.Category;
import lk.londontec.fin_mate.entity.Category.CategoryType;
import lk.londontec.fin_mate.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        seedCategory("FOOD", CategoryType.EXPENSE, "🍔");
        seedCategory("TRANSPORT", CategoryType.EXPENSE, "🚌");
        seedCategory("RENT", CategoryType.EXPENSE, "🏠");
        seedCategory("UTILITIES", CategoryType.EXPENSE, "💡");
        seedCategory("ENTERTAINMENT", CategoryType.EXPENSE, "🎬");
        seedCategory("HEALTH", CategoryType.EXPENSE, "💊");
        seedCategory("SHOPPING", CategoryType.EXPENSE, "🛍");
        seedCategory("INCOME", CategoryType.INCOME, "💰");
        seedCategory("OTHER", CategoryType.EXPENSE, "❓");
    }

    private void seedCategory(String name, CategoryType type, String icon) {
        categoryRepository.findByName(name).orElseGet(() ->
                categoryRepository.save(Category.builder()
                        .name(name)
                        .type(type)
                        .icon(icon)
                        .build()));
    }
}