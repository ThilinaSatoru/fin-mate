package lk.londontec.fin_mate.service.sms;

import lk.londontec.fin_mate.entity.Transaction;
import lk.londontec.fin_mate.entity.User;
import lk.londontec.fin_mate.repository.TransactionRepository;
import lk.londontec.fin_mate.repository.UserRepository;
import lk.londontec.fin_mate.util.SmsParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsIngestionService {

    private final List<SmsParser> parsers; // Spring injects all SmsParser beans
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Transaction ingest(Long userId, String smsBody, String sender) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        SmsParser parser = parsers.stream()
                .filter(p -> p.supports(sender))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException(
                        "No parser registered for sender: " + sender));

        Transaction txn = parser.parse(smsBody, sender);
        if (txn == null) {
            log.warn("Failed to parse SMS from {}: storing as unparsed", sender);
            txn = Transaction.builder()
                    .rawSms(smsBody)
                    .bankSender(sender)
                    .amount(java.math.BigDecimal.ZERO)
                    .type(Transaction.TransactionType.DEBIT)
                    .merchant("UNPARSED")
                    .transactionDate(java.time.LocalDateTime.now())
                    .build();
        }

        txn.setUser(user);
        return transactionRepository.save(txn);
    }
}
