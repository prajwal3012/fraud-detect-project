package com.fraud;

import org.junit.jupiter.api.Test;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RulesEngineTest {

    private final RulesEngine rulesEngine = new RulesEngine();

    @Test
    public void testCleanTransaction() {
        Transaction t = new Transaction();
        t.setAmount(100.0);
        t.setLocation("USA");
        t.setDeviceType("Mobile");

        Transaction h1 = new Transaction();
        h1.setAmount(120.0);
        h1.setLocation("USA");

        String result = rulesEngine.evaluate(t, Collections.singletonList(h1));
        assertTrue(result.contains("Safe - Not Fraud"));
    }

    @Test
    public void testHighSeverityFraud() {
        Transaction t = new Transaction();
        t.setAmount(1500.0);
        t.setLocation("Nigeria");
        t.setDeviceType("Emulator");

        Transaction h1 = new Transaction();
        h1.setAmount(100.0); 
        h1.setLocation("Nigeria");

        String result = rulesEngine.evaluate(t, Collections.singletonList(h1));

        // Should trigger: Amount (50) + Location (30) + Device (20) = 100 (High Severity)
        assertEquals("Fraud - High Severity", result);
    }

    @Test
    public void testVelocityFraud() {
        Transaction t = new Transaction();
        t.setAmount(100.0);
        t.setTimestamp(System.currentTimeMillis());
        
        java.util.List<Transaction> history = new java.util.ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Transaction h = new Transaction();
            h.setTimestamp(t.getTimestamp() - 1000); 
            h.setAmount(50.0);
            h.setIpAddress("1.1.1.1");
            history.add(h);
        }

        String result = rulesEngine.evaluate(t, history);
        // Velocity adds 40 points -> Medium Severity
        assertEquals("Review - Medium Severity", result);
    }

    @Test
    public void testImpossibleTravel() {
        Transaction t = new Transaction();
        t.setLocation("USA");
        t.setTimestamp(System.currentTimeMillis());

        Transaction h1 = new Transaction();
        h1.setLocation("UK");
        h1.setTimestamp(t.getTimestamp() - 3600000); 

        String result = rulesEngine.evaluate(t, Collections.singletonList(h1));
        // Impossible travel adds 35 points -> Medium Severity
        assertEquals("Review - Medium Severity", result);
    }

    @Test
    public void testSuspiciousTime() {
        Transaction t = new Transaction();
        t.setTime("03:45");
        
        String result = rulesEngine.evaluate(t, Collections.emptyList());
        // Suspicious time adds 15 points -> Low Risk
        assertEquals("Low Risk - Monitor", result);
    }

    @Test
    public void testProbingPattern() {
        Transaction t = new Transaction();
        t.setAmount(500.0);
        t.setTimestamp(System.currentTimeMillis());

        Transaction h1 = new Transaction();
        h1.setAmount(2.0); 
        h1.setTimestamp(t.getTimestamp() - 60000); 
        h1.setIpAddress("1.1.1.1");

        String result = rulesEngine.evaluate(t, Collections.singletonList(h1));
        // Probing (30) + Amount (50) = 80 -> High Severity
        assertEquals("Fraud - High Severity", result);
    }

    @Test
    public void testIPChanges() {
        Transaction t = new Transaction();
        t.setIpAddress("1.1.1.1");
        t.setTimestamp(System.currentTimeMillis());

        Transaction h1 = new Transaction();
        h1.setIpAddress("2.2.2.2");
        h1.setTimestamp(t.getTimestamp() - 60000);

        String result = rulesEngine.evaluate(t, Collections.singletonList(h1));
        // IP change adds 25 points -> Low Risk
        assertEquals("Low Risk - Monitor", result);
    }
}
