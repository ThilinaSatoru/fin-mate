package lk.londontec.fin_mate.controller;

import lk.londontec.fin_mate.entity.Transaction;
import lk.londontec.fin_mate.service.sms.SmsIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsIngestionService smsIngestionService;

    @PostMapping("/{userId}")
    public Transaction ingest(@PathVariable Long userId, @RequestBody SmsIngestRequest request) {
        return smsIngestionService.ingest(userId, request.body(), request.sender());
    }

    public record SmsIngestRequest(String body, String sender) {
    }
}