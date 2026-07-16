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
public class CombankSmsParser implements SmsParser {

    // Matches: "...debited Rs.2,500.00 on 15-07-2026 at KEELLS SUPER."
    private static final Pattern DEBIT_PATTERN = Pattern.compile(
            "debited\\s+Rs\\.?([\\d,]+\\.\\d{2})\\s+on\\s+(\\d{2}-\\d{2}-\\d{4})\\s+at\\s+([A-Za-z0-9 &.'-]+?)\\.",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern CREDIT_PATTERN = Pattern.compile(
            "credited\\s+Rs\\.?([\\d,]+\\.\\d{2})\\s+on\\s+(\\d{2}-\\d{2}-\\d{4})",
            Pattern.CASE_INSENSITIVE);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public boolean supports(String sender) {
        return sender != null && sender.toUpperCase().contains("COMBANK");
    }

    @Override
    public Transaction parse(String smsBody, String sender) {
        Matcher debitMatcher = DEBIT_PATTERN.matcher(smsBody);
        if (debitMatcher.find()) {
            return buildTransaction(smsBody, sender, debitMatcher.group(1),
                    debitMatcher.group(2), debitMatcher.group(3).trim(),
                    Transaction.TransactionType.DEBIT);
        }

        Matcher creditMatcher = CREDIT_PATTERN.matcher(smsBody);
        if (creditMatcher.find()) {
            return buildTransaction(smsBody, sender, creditMatcher.group(1),
                    creditMatcher.group(2), "DEPOSIT",
                    Transaction.TransactionType.CREDIT);
        }

        log.warn("CombankSmsParser could not parse SMS: {}", smsBody);
        return null; // caller decides how to handle unparsed SMS
    }

    private Transaction buildTransaction(String smsBody, String sender, String amountStr,
                                         String dateStr, String merchant,
                                         Transaction.TransactionType type) {
        BigDecimal amount = new BigDecimal(amountStr.replace(",", ""));
        LocalDate date = LocalDate.parse(dateStr, DATE_FMT);

        return Transaction.builder()
                .amount(amount)
                .type(type)
                .merchant(merchant)
                .rawSms(smsBody)
                .bankSender(sender)
                .transactionDate(date.atStartOfDay())
                .build();
    }
}
