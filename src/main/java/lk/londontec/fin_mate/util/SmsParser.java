package lk.londontec.fin_mate.util;

import lk.londontec.fin_mate.entity.Transaction;
import lk.londontec.fin_mate.enums.BankSender;

public interface SmsParser {
    BankSender bank();
    Transaction parse(String smsBody, String sender);

    default boolean supports(String sender) {
        return BankSender.fromSender(sender)
                .map(resolved -> resolved == bank())
                .orElse(false);
    }
}
