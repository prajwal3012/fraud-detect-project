
import java.util.*;
import java.io.*;

public class AccuracyCheck {
    public static void main(String[] args) throws Exception {
        // Mock RulesEngine for quick check
        // We'll use the logic from RulesEngine.java
        
        File csvFile = new File("test_transactions.csv");
        BufferedReader br = new BufferedReader(new FileReader(csvFile));
        String line;
        br.readLine(); // skip header
        
        int total = 0;
        int matches = 0;
        
        Map<String, List<Transaction>> userHistory = new HashMap<>();

        while ((line = br.readLine()) != null) {
            String[] data = line.split(",");
            if (data.length < 10) continue;
            
            Transaction t = new Transaction();
            t.setUserId(data[0].trim());
            t.setAmount(Double.parseDouble(data[1].trim()));
            t.setLocation(data[2].trim());
            t.setMerchant(data[3].trim());
            t.setDeviceType(data[4].trim());
            t.setIpAddress(data[5].trim());
            t.setDate(data[6].trim());
            t.setTime(data[7].trim());
            t.setPaymentType(data[8].trim());
            t.setExpectedStatus(data[9].trim());
            
            // Mock timestamp
            String[] timeParts = t.getTime().split(":");
            long hour = Long.parseLong(timeParts[0]);
            long min = Long.parseLong(timeParts[1]);
            t.setTimestamp(hour * 3600000 + min * 60000); // Simple relative timestamp

            List<Transaction> history = userHistory.getOrDefault(t.getUserId(), new ArrayList<>());
            
            String status = evaluate(t, history);
            
            boolean isDetectedFraud = status.contains("Fraud") || status.contains("Severity") || status.contains("Risk");
            boolean isExpectedFraud = t.getExpectedStatus().toLowerCase().contains("fraud") || t.getExpectedStatus().toLowerCase().contains("risky");
            
            if (isDetectedFraud == isExpectedFraud) {
                matches++;
            } else {
                System.out.println("Mismatch at User: " + t.getUserId() + " Amount: " + t.getAmount() + " Status: [" + status + "] Expected: [" + t.getExpectedStatus() + "]");
            }
            
            total++;
            history.add(t);
            userHistory.put(t.getUserId(), history);
        }
        
        System.out.println("Total: " + total);
        System.out.println("Matches: " + matches);
        System.out.println("Accuracy: " + (100.0 * matches / total) + "%");
    }

    public static String evaluate(Transaction t, List<Transaction> history) {
        int score = 0;
        if (!history.isEmpty()) {
            double sum = 0;
            for (Transaction tx : history) sum += tx.getAmount();
            double mean = sum / history.size();
            if (t.getAmount() > 2000 && t.getAmount() > mean * 5) score += 50;
        } else {
            if (t.getAmount() > 10000) score += 40;
        }

        List<String> riskyLocations = Arrays.asList("Nigeria", "Russia", "North Korea", "Syria", "Unknown");
        if (riskyLocations.contains(t.getLocation())) score += 35;

        List<String> riskyMerchants = Arrays.asList("CryptoExchange", "Casino", "DarkWebMarket");
        if (riskyMerchants.contains(t.getMerchant())) score += 30;

        if (t.getDeviceType().equalsIgnoreCase("emulator") || t.getDeviceType().equalsIgnoreCase("unknown")) score += 20;

        long tenMinutesMillis = 10 * 60 * 1000;
        long recentCount = history.stream().filter(tx -> Math.abs(t.getTimestamp() - tx.getTimestamp()) < tenMinutesMillis).count();
        if (recentCount >= 2) score += 40;

        long oneHourMillis = 60 * 60 * 1000;
        boolean impossibleTravel = history.stream().filter(tx -> Math.abs(t.getTimestamp() - tx.getTimestamp()) < oneHourMillis)
                .anyMatch(tx -> !t.getLocation().equals(tx.getLocation()));
        if (impossibleTravel) score += 35;

        if (t.getTime().matches("^0[0-4]:.*")) score += 15;

        if (t.getAmount() > 100) {
            boolean hasSmallProbing = history.stream().filter(tx -> Math.abs(t.getTimestamp() - tx.getTimestamp()) < tenMinutesMillis)
                    .anyMatch(tx -> tx.getAmount() < 10);
            if (hasSmallProbing) score += 30;
        }

        if (t.getPaymentType().equalsIgnoreCase("WireTransfer")) score += 15;

        if (score >= 80) return "Fraud - High Severity";
        if (score >= 45) return "Review - Medium Severity";
        if (score >= 35) return "Low Risk";
        return "Safe";
    }
}

class Transaction {
    private String userId, location, merchant, deviceType, ipAddress, date, time, paymentType, expectedStatus;
    private double amount;
    private long timestamp;
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
    public String getExpectedStatus() { return expectedStatus; }
    public void setExpectedStatus(String expectedStatus) { this.expectedStatus = expectedStatus; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
