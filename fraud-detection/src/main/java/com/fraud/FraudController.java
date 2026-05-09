package com.fraud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class FraudController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TransactionRepository txnRepo;

    @Autowired
    private RulesEngine engine;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userRepo.save(user);
    }

    @PostMapping("/login")
    public User login(@RequestBody User req) {
        return userRepo.findByNameAndPassword(req.getName(), req.getPassword());
    }

    @PostMapping("/reset-password")
    public User resetPassword(@RequestBody User req) {
        User existingUser = userRepo.findByName(req.getName());
        if (existingUser != null) {
            existingUser.setPassword(req.getPassword());
            return userRepo.save(existingUser);
        }
        return null;
    }

    @PostMapping("/transaction")
    public Transaction process(@RequestBody Transaction txn) {

        List<Transaction> history = txnRepo.findByUserId(txn.getUserId());

        txn.setStatus(engine.evaluate(txn, history));

        return txnRepo.save(txn);
    }

    @PostMapping("/upload")
    public List<Transaction> uploadCsv(@RequestParam("file") MultipartFile file) {
        List<Transaction> results = new ArrayList<>();
        Map<String, List<Transaction>> batchHistory = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // skip header
                    continue;
                }
                String[] data = line.split(",");
                if (data.length >= 9) {
                    Transaction txn = new Transaction();
                    txn.setTransactionId(UUID.randomUUID().toString());
                    txn.setUserId(data[0].trim());
                    txn.setAmount(Double.parseDouble(data[1].trim()));
                    txn.setLocation(data[2].trim());
                    txn.setMerchant(data[3].trim());
                    txn.setDeviceType(data[4].trim());
                    txn.setIpAddress(data[5].trim());
                    txn.setDate(data[6].trim());
                    txn.setTime(data[7].trim());
                    txn.setPaymentType(data[8].trim());
                    if (data.length >= 10) {
                        txn.setExpectedStatus(data[9].trim());
                    }
                    
                    try {
                        String dateTimeStr = data[6].trim() + " " + data[7].trim();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                        long ts = LocalDateTime.parse(dateTimeStr, formatter)
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli();
                        txn.setTimestamp(ts);
                    } catch (Exception e) {
                        txn.setTimestamp(System.currentTimeMillis());
                    }

                    // Combine DB history with current batch history for accurate real-time analysis
                    List<Transaction> history = txnRepo.findByUserId(txn.getUserId());
                    List<Transaction> currentBatchUserHistory = batchHistory.getOrDefault(txn.getUserId(), new ArrayList<>());
                    
                    List<Transaction> combinedHistory = new ArrayList<>(history);
                    combinedHistory.addAll(currentBatchUserHistory);

                    txn.setStatus(engine.evaluate(txn, combinedHistory));
                    
                    Transaction saved = txnRepo.save(txn);
                    results.add(saved);
                    
                    // Update batch history for next rows in the same upload
                    currentBatchUserHistory.add(saved);
                    batchHistory.put(txn.getUserId(), currentBatchUserHistory);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }
    @DeleteMapping("/transactions")
    public Map<String, String> clearAll() {
        txnRepo.deleteAll();
        Map<String, String> response = new HashMap<>();
        response.put("message", "All transactions cleared successfully");
        return response;
    }
}