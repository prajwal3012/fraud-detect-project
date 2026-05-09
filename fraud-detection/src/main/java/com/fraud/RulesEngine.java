package com.fraud;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RulesEngine {

    public String evaluate(Transaction t, List<Transaction> history) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        // Rule 1: Amount exceeds 3x mean of history
        if (!history.isEmpty()) {
            double sum = 0;
            for (Transaction tx : history)
                sum += tx.getAmount();
            double mean = sum / history.size();

            if (t.getAmount() > 2000 && t.getAmount() > mean * 5) {
                score += 50;
                reasons.add("Amount > 5x average and significant (>2000)");
            }
        } else {
            // New user, large initial transaction
            if (t.getAmount() > 10000) {
                score += 40;
                reasons.add("Large initial amount");
            }
        }

        // Rule 2: Suspicious locations
        List<String> riskyLocations = Arrays.asList("Nigeria", "Russia", "North Korea", "Syria", "Unknown");
        if (t.getLocation() != null && riskyLocations.contains(t.getLocation())) {
            score += 35;
            reasons.add("Risky location");
        }

        // Rule 3: High-Risk merchants
        List<String> riskyMerchants = Arrays.asList("CryptoExchange", "Casino", "DarkWebMarket");
        if (t.getMerchant() != null && riskyMerchants.contains(t.getMerchant())) {
            score += 30;
            reasons.add("High-risk merchant");
        }

        // Rule 4: Device Type anomaly
        if (t.getDeviceType() != null
                && (t.getDeviceType().equalsIgnoreCase("emulator") || t.getDeviceType().equalsIgnoreCase("unknown"))) {
            score += 20;
            reasons.add("Suspicious device type");
        }

        // Rule 5: High Velocity Check (3+ transactions in 10 mins)
        long tenMinutesMillis = 10 * 60 * 1000;
        long recentCount = history.stream()
                .filter(tx -> !tx.getTransactionId().equals(t.getTransactionId())) // Ignore self
                .filter(tx -> tx.getTimestamp() != t.getTimestamp()) // Ignore duplicates with same TS
                .filter(tx -> Math.abs(t.getTimestamp() - tx.getTimestamp()) < tenMinutesMillis)
                .count();
        if (recentCount >= 2) {
            score += 40;
            reasons.add("High transaction velocity (3rd+ in 10m)");
        }

        // Rule 6: Impossible Travel (Different location within 1 hour)
        long oneHourMillis = 60 * 60 * 1000;
        boolean impossibleTravel = history.stream()
                .filter(tx -> !tx.getTransactionId().equals(t.getTransactionId()))
                .filter(tx -> tx.getTimestamp() != t.getTimestamp())
                .filter(tx -> Math.abs(t.getTimestamp() - tx.getTimestamp()) < oneHourMillis)
                .anyMatch(tx -> t.getLocation() != null && tx.getLocation() != null
                        && !t.getLocation().equals(tx.getLocation()));
        if (impossibleTravel) {
            score += 35;
            reasons.add("Impossible travel (Location change within 1h)");
        }

        // Rule 7: Suspicious Time Window (12 AM - 5 AM)
        if (t.getTime() != null && t.getTime().matches("^0[0-4]:.*")) {
            score += 15;
            reasons.add("Transaction during odd hours (12AM-5AM)");
        }

        // Rule 8: Small Amount Probing (< $10 followed by large txn)
        if (t.getAmount() > 100) {
            boolean hasSmallProbing = history.stream()
                    .filter(tx -> tx.getTimestamp() != t.getTimestamp())
                    .filter(tx -> Math.abs(t.getTimestamp() - tx.getTimestamp()) < tenMinutesMillis)
                    .anyMatch(tx -> tx.getAmount() < 10);
            if (hasSmallProbing) {
                score += 30;
                reasons.add("Potential small amount probing detected");
            }
        }

        // Rule 9: Frequent IP Usage (Same user changing IPs frequently)
        Set<String> distinctIps = history.stream()
                .filter(tx -> tx.getTimestamp() != t.getTimestamp())
                .filter(tx -> Math.abs(t.getTimestamp() - tx.getTimestamp()) < tenMinutesMillis)
                .map(Transaction::getIpAddress)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        if (t.getIpAddress() != null) {
            distinctIps.add(t.getIpAddress());
        }
        if (distinctIps.size() > 1) {
            score += 15;
            reasons.add("Frequent IP address changes");
        }
        // Rule 10: Suspicious Payment Method
        if (t.getPaymentType() != null && t.getPaymentType().equalsIgnoreCase("WireTransfer")) {
            score += 15;
            reasons.add("Risk factor: Wire Transfer");
        }

        // Evaluate Threshold
        String result;
        if (score >= 80) {
            result = "Fraud - High Severity";
        } else if (score >= 45) {
            result = "Review - Medium Severity";
        } else if (score >= 35) {
            result = "Low Risk";
        } else {
            result = "Safe";
        }

        return result;
    }
}