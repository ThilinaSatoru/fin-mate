package lk.londontec.fin_mate.util;

import lk.londontec.fin_mate.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class SampathSmsParser implements SmsParser {

    // Matches: "Rs 1,200.00 debited from A/C **5678 at PIZZA HUT on 15/07/2026."
    private static final Pattern DEBIT_PATTERN = Pattern.compile(
            "Rs\\s?([\\d,]+\\.\\d{2})\\s+debited.*?at\\s+([A-Za-z0-9 &.'-]+?)\\s+on\\s+(\\d{2}/\\d{2}/\\d{4})",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern CREDIT_PATTERN = Pattern.compile(
            "Rs\\s?([\\d,]+\\.\\d{2})\\s+credited.*?on\\s+(\\d{2}/\\d{2}/\\d{4})",
            Pattern.CASE_INSENSITIVE);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public boolean supports(String sender) {
        return sender != null && sender.toUpperCase().contains("SAMPATH");
    }

    @Override
    public Transaction parse(String smsBody, String sender) {
        Matcher debitMatcher = DEBIT_PATTERN.matcher(smsBody);
        if (debitMatcher.find()) {
            BigDecimal amount = new BigDecimal(debitMatcher.group(1).replace(",", ""));
            LocalDate date = LocalDate.parse(debitMatcher.group(3), DATE_FMT);
            return Transaction.builder()
                    .amount(amount)
                    .type(Transaction.TransactionType.DEBIT)
                    .merchant(debitMatcher.group(2).trim())
                    .rawSms(smsBody)
                    .bankSender(sender)
                    .transactionDate(date.atStartOfDay())
                    .build();
        }

        Matcher creditMatcher = CREDIT_PATTERN.matcher(smsBody);
        if (creditMatcher.find()) {
            BigDecimal amount = new BigDecimal(creditMatcher.group(1).replace(",", ""));
            LocalDate date = LocalDate.parse(creditMatcher.group(2), DATE_FMT);
            return Transaction.builder()
                    .amount(amount)
                    .type(Transaction.TransactionType.CREDIT)
                    .merchant("DEPOSIT")
                    .rawSms(smsBody)
                    .bankSender(sender)
                    .transactionDate(date.atStartOfDay())
                    .build();
        }

        log.warn("SampathSmsParser could not parse SMS: {}", smsBody);
        return null;
    }
}
