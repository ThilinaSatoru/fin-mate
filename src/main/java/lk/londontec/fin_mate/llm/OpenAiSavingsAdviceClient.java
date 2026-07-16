package lk.londontec.fin_mate.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.londontec.fin_mate.dto.MonthlySummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenAiSavingsAdviceClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiSavingsAdviceClient(@Value("${openai.api.key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public List<String> generateTips(MonthlySummary summary) {
        String categoryBreakdown = summary.spendByCategory().entrySet().stream()
                .map(e -> "- %s: LKR %s (%.1f%% vs last month)".formatted(
                        e.getKey(), e.getValue(),
                        summary.spendChangeVsLastMonth().getOrDefault(e.getKey(), BigDecimal.ZERO)))
                .reduce("", (a, b) -> a + "\n" + b);

        String prompt = """
                A user in Sri Lanka has this financial summary for the month:
                Income: LKR %s
                Expenses: LKR %s
                Net savings: LKR %s
                
                Spending by category:
                %s
                
                Give exactly 3 short, specific, actionable savings tips based on this data.
                Reference actual categories and numbers where relevant. Keep each tip to one sentence.
                Respond as a JSON array of exactly 3 strings, nothing else. Example:
                ["tip one", "tip two", "tip three"]
                """.formatted(summary.totalIncome(), summary.totalExpenses(),
                summary.netSavings(), categoryBreakdown);

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "max_tokens", 300,
                    "temperature", 0.7
            );

            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            var root = objectMapper.readTree(response);
            String content = root.path("choices").get(0).path("message").path("content").asText().trim();

            // Strip markdown code fences if the model wraps the JSON in them
            content = content.replaceAll("^```json", "").replaceAll("^```", "").replaceAll("```$", "").trim();

            List<String> tips = objectMapper.readValue(content, List.class);
            return tips.isEmpty() ? fallbackTips(summary) : tips;

        } catch (Exception e) {
            log.error("OpenAI savings advice generation failed: {}", e.getMessage());
            return fallbackTips(summary);
        }
    }

    /**
     * Deterministic fallback: flag the highest-spend category and biggest % increase.
     */
    private List<String> fallbackTips(MonthlySummary summary) {
        List<String> tips = new java.util.ArrayList<>();

        summary.spendByCategory().entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .ifPresent(top -> tips.add("Your highest spending category this month was "
                        + top.getKey() + " at LKR " + top.getValue() + " — consider setting a budget cap here."));

        summary.spendChangeVsLastMonth().entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) > 0)
                .ifPresent(biggestIncrease -> tips.add(biggestIncrease.getKey()
                        + " spending increased by " + biggestIncrease.getValue().setScale(0, java.math.RoundingMode.HALF_UP)
                        + "% compared to last month — worth reviewing."));

        if (summary.netSavings().compareTo(BigDecimal.ZERO) < 0) {
            tips.add("You spent more than you earned this month — try to identify one non-essential expense to cut next month.");
        } else {
            tips.add("You saved LKR " + summary.netSavings() + " this month — consider moving it to a separate savings account.");
        }

        return tips;
    }
}