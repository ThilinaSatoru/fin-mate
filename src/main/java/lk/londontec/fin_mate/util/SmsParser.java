package lk.londontec.fin_mate.util;

import lk.londontec.fin_mate.entity.Transaction;

public interface SmsParser {
    boolean supports(String sender);

    Transaction parse(String smsBody, String sender);
}
