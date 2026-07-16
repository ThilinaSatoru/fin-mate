package lk.londontec.fin_mate.controller;

import lk.londontec.fin_mate.dto.AffordabilityResult;
import lk.londontec.fin_mate.llm.OpenAiAdviceClient;
import lk.londontec.fin_mate.security.AppUserPrincipal;
import lk.londontec.fin_mate.service.affordabilty.AffordabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/affordability")
@RequiredArgsConstructor
public class AffordabilityController {

    private final AffordabilityService affordabilityService;
    private final OpenAiAdviceClient adviceClient;

    @GetMapping
    public AffordabilityResponse check(@AuthenticationPrincipal AppUserPrincipal principal,
                                       @RequestParam BigDecimal itemCost) {
        AffordabilityResult result = affordabilityService.evaluate(principal.getUserId(), itemCost);
        String explanation = adviceClient.explainAffordability(result);
        return new AffordabilityResponse(result, explanation);
    }

    public record AffordabilityResponse(AffordabilityResult result, String explanation) {
    }
}