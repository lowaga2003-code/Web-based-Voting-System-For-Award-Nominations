package com.pation.awardnomination.voting;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/** Voting - receipt codes and the SHA-256 seal of a ballot. */
final class BallotSeal {

    /** No 0/O, 1/I/L: receipt codes are read and typed by people. */
    private static final char[] ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private BallotSeal() { }

    /** e.g. VT-7F3K-2Q9M */
    static String newReceiptCode() {
        StringBuilder sb = new StringBuilder("VT-");
        for (int i = 0; i < 8; i++) {
            if (i == 4) sb.append('-');
            sb.append(ALPHABET[RANDOM.nextInt(ALPHABET.length)]);
        }
        return sb.toString();
    }

    static String newSalt() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return hex(bytes);
    }

    static String hash(String receipt, Long categoryId, Integer nomineeId, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String raw = receipt + "|" + categoryId + "|" + nomineeId + "|" + salt;
            return hex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String h = Integer.toHexString(0xff & b);
            if (h.length() == 1) sb.append('0');
            sb.append(h);
        }
        return sb.toString();
    }
}
