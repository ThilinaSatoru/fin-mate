package lk.londontec.fin_mate.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.londontec.fin_mate.dto.AffordabilityResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenAiAdviceClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiAdviceClient(@Value("${openai.api.key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String explainAffordability(AffordabilityResult result) {
        String prompt = """
                A user in Sri Lanka is asking if they can afford an item costing LKR %s.
                Their average monthly income: LKR %s
                Their average monthly expenses: LKR %s
                Their average monthly disposable income: LKR %s
                Months needed to save the full amount from disposable income: %s
                Verdict: %s
                
                Write a short, friendly, 2-3 sentence explanation for the user in plain English,
                mentioning the timeframe and one practical tip. Do not repeat the raw numbers
                back mechanically — explain what they mean.
                """.formatted(
                result.itemCost(), result.avgMonthlyIncome(), result.avgMonthlyExpenses(),
                result.avgMonthlyDisposable(), result.monthsToSaveIfSetAside(), result.verdict());

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "max_tokens", 150,
                    "temperature", 0.7
            );

            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            var root = objectMapper.readTree(response);
            return root.path("choices").get(0).path("message").path("content").asText().trim();

        } catch (Exception e) {
            log.error("OpenAI advice generation failed: {}", e.getMessage());
            return fallbackExplanation(result);
        }
    }

    /**
     * Deterministic fallback if the LLM call fails — keeps the feature usable offline/API-down.
     */
    private String fallbackExplanation(AffordabilityResult result) {
        return switch (result.verdict()) {
            case "AFFORDABLE" -> "Based on your recent spending, you could save enough for this in about "
                    + result.monthsToSaveIfSetAside() + " months without major changes.";
            case "STRETCH" -> "This would take about " + result.monthsToSaveIfSetAside()
                    + " months to save for — it's possible, but you may want to cut back on non-essential spending first.";
            default -> "Based on your current income and expenses, this purchase isn't recommended right now. "
                    + "Your monthly expenses are close to or exceeding your income.";
        };
    }
}
