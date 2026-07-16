package lk.londontec.fin_mate.service;

import lk.londontec.fin_mate.entity.Category;
import lk.londontec.fin_mate.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    // Used by CategorizationService (rule-based matcher) later
    public Category getOrDefault(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.findByName("OTHER")
                        .orElseThrow(() -> new IllegalStateException("Seed data missing: OTHER category")));
    }
}