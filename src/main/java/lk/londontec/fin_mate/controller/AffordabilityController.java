package lk.londontec.fin_mate.controller;

import lk.londontec.fin_mate.dto.AffordabilityResult;
import lk.londontec.fin_mate.llm.OpenAiAdviceClient;
import lk.londontec.fin_mate.service.affordabilty.AffordabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/affordability")
@RequiredArgsConstructor
public class AffordabilityController {

    private final AffordabilityService affordabilityService;
    private final OpenAiAdviceClient adviceClient;

    @GetMapping("/{userId}")
    public AffordabilityResponse check(@PathVariable Long userId,
                                       @RequestParam BigDecimal itemCost) {
        AffordabilityResult result = affordabilityService.evaluate(userId, itemCost);
        String explanation = adviceClient.explainAffordability(result);
        return new AffordabilityResponse(result, explanation);
    }

    public record AffordabilityResponse(AffordabilityResult result, String explanation) {
    }
}