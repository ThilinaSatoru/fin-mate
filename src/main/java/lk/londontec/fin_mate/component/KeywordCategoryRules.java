package lk.londontec.fin_mate.component;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class KeywordCategoryRules {

    // LinkedHashMap preserves insertion order — first match wins
    private final Map<String, String> rules = new LinkedHashMap<>();

    public KeywordCategoryRules() {
        rules.put("KEELLS", "FOOD");
        rules.put("CARGILLS", "FOOD");
        rules.put("FOOD CITY", "FOOD");
        rules.put("PIZZA", "FOOD");
        rules.put("KFC", "FOOD");
        rules.put("RESTAURANT", "FOOD");

        rules.put("UBER", "TRANSPORT");
        rules.put("PICKME", "TRANSPORT");
        rules.put("FUEL", "TRANSPORT");
        rules.put("PETROL", "TRANSPORT");
        rules.put("IOC", "TRANSPORT");
        rules.put("CEYPETCO", "TRANSPORT");

        rules.put("CEB", "UTILITIES");
        rules.put("ELECTRICITY", "UTILITIES");
        rules.put("WATER BOARD", "UTILITIES");
        rules.put("DIALOG", "UTILITIES");
        rules.put("SLT", "UTILITIES");
        rules.put("MOBITEL", "UTILITIES");

        rules.put("PHARMACY", "HEALTH");
        rules.put("HOSPITAL", "HEALTH");
        rules.put("HEALTHGUARD", "HEALTH");

        rules.put("CINEMA", "ENTERTAINMENT");
        rules.put("NETFLIX", "ENTERTAINMENT");
        rules.put("SPOTIFY", "ENTERTAINMENT");

        rules.put("RENT", "RENT");

        rules.put("SALARY", "INCOME");
        rules.put("DEPOSIT", "INCOME");
    }

    /**
     * Returns category name, or null if nothing matches.
     */
    public String match(String merchant) {
        if (merchant == null) return null;
        String upper = merchant.toUpperCase();
        return rules.entrySet().stream()
                .filter(e -> upper.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}