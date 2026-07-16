package lk.londontec.fin_mate.controller;

import lk.londontec.fin_mate.entity.Transaction;
import lk.londontec.fin_mate.security.AppUserPrincipal;
import lk.londontec.fin_mate.service.sms.SmsIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsIngestionService smsIngestionService;

    @PostMapping
    public Transaction ingest(@AuthenticationPrincipal AppUserPrincipal principal,
                              @RequestBody SmsIngestRequest request) {
        return smsIngestionService.ingest(principal.getUserId(), request.body(), request.sender());
    }

    public record SmsIngestRequest(String body, String sender) {
    }
}