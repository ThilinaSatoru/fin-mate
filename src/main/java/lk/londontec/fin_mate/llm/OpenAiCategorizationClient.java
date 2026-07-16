package lk.londontec.fin_mate.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenAiCategorizationClient {

    private static final List<String> VALID_CATEGORIES = List.of(
            "FOOD", "TRANSPORT", "RENT", "UTILITIES",
            "ENTERTAINMENT", "HEALTH", "SHOPPING", "INCOME", "OTHER");
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiCategorizationClient(@Value("${openai.api.key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * Returns one of VALID_CATEGORIES, or "OTHER" if the call fails or response is unusable.
     */
    public String categorize(String merchant, java.math.BigDecimal amount) {
        String prompt = """
                Classify this bank transaction into exactly one category.
                Merchant: %s
                Amount: LKR %s
                Valid categories: %s
                Respond with ONLY the category name, nothing else.
                """.formatted(merchant, amount, String.join(", ", VALID_CATEGORIES));

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "max_tokens", 10,
                    "temperature", 0
            );

            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            String category = extractCategory(response);
            return VALID_CATEGORIES.contains(category) ? category : "OTHER";

        } catch (Exception e) {
            log.error("OpenAI categorization failed for merchant '{}': {}", merchant, e.getMessage());
            return "OTHER";
        }
    }

    private String extractCategory(String rawJsonResponse) throws Exception {
        var root = objectMapper.readTree(rawJsonResponse);
        String content = root.path("choices").get(0).path("message").path("content").asText();
        return content.trim().toUpperCase();
    }
}
