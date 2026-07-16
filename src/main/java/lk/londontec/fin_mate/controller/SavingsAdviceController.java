package lk.londontec.fin_mate.controller;

import lk.londontec.fin_mate.dto.MonthlySummary;
import lk.londontec.fin_mate.llm.OpenAiSavingsAdviceClient;
import lk.londontec.fin_mate.security.AppUserPrincipal;
import lk.londontec.fin_mate.service.savings.SavingsAdviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1/advice")
@RequiredArgsConstructor
public class SavingsAdviceController {

    private final SavingsAdviceService savingsAdviceService;
    private final OpenAiSavingsAdviceClient adviceClient;

    @GetMapping("/{yearMonth}")
    public AdviceResponse getAdvice(@AuthenticationPrincipal AppUserPrincipal principal,
                                    @PathVariable String yearMonth) {
        MonthlySummary summary = savingsAdviceService.buildMonthlySummary(
                principal.getUserId(), YearMonth.parse(yearMonth));
        List<String> tips = adviceClient.generateTips(summary);
        return new AdviceResponse(summary, tips);
    }

    public record AdviceResponse(MonthlySummary summary, List<String> tips) {
    }
}