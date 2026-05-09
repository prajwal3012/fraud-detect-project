import com.fraud.*;
import java.util.*;

public class DebugRules {
    public static void main(String[] args) {
        RulesEngine engine = new RulesEngine();
        Transaction t = new Transaction();
        t.setUserId("user_1");
        t.setAmount(234.56);
        t.setLocation("USA");
        t.setMerchant("Amazon");
        t.setDeviceType("Mobile");
        t.setIpAddress("12.34.56.78");
        t.setTime("10:00");
        t.setTimestamp(System.currentTimeMillis());
        t.setPaymentType("CreditCard");

        String status = engine.evaluate(t, new ArrayList<>());
        System.out.println("Status: " + status);
    }
}
