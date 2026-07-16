package lk.londontec.fin_mate.enums;

import java.util.Arrays;
import java.util.Optional;

public enum BankSender {
    COMBANK(5551),
    SAMPATH(5552),
    HNB(5553);

    private final int numericId;

    BankSender(int numericId) {
        this.numericId = numericId;
    }

    /**
     * Resolves a bank from a raw SMS sender value, which may be:
     * - an alphanumeric sender ID from a real device (e.g. "COMBANK", "SampathBank")
     * - a plain numeric sender from the Android emulator (e.g. "5551")
     */
    public static Optional<BankSender> fromSender(String sender) {
        if (sender == null || sender.isBlank()) return Optional.empty();

        String trimmed = sender.trim();

        // Numeric sender (emulator testing)
        try {
            int numeric = Integer.parseInt(trimmed);
            return Arrays.stream(values())
                    .filter(bank -> bank.numericId == numeric)
                    .findFirst();
        } catch (NumberFormatException ignored) {
            // Not numeric — fall through to string match
        }

        // Alphanumeric sender (real device)
        String upper = trimmed.toUpperCase();
        return Arrays.stream(values())
                .filter(bank -> upper.contains(bank.name()))
                .findFirst();
    }

    public int getNumericId() {
        return numericId;
    }
}
