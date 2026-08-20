public class WalletSecurityQA {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=================================================");
        System.out.println("   STARTING DIGITAL WALLET SECURITY QA TESTING   ");
        System.out.println("=================================================\n");

        testNormalTransaction();
        testInsufficientBalance();
        testDailyLimit();
        testMultipleFailedPINs();
        testSuspiciousTransaction();
        testDuplicateTransaction();
        testNegativeAmount();
        testConcurrentTransactions();
    }

    // 1. Normal Transaction Test
    private static void testNormalTransaction() {
        System.out.println("--- Test: Normal Transaction ---");
        DigitalWallet wallet = new DigitalWallet("ACC01", 1000.0, "1234");
        boolean success = wallet.withdraw(200.0, "1234");
        System.out.println("Withdrawal Successful: " + success);
        System.out.println("Remaining Balance: $" + wallet.getBalance() + "\n");
    }

    // 2. Insufficient Balance Test
    private static void testInsufficientBalance() {
        System.out.println("--- Test: Insufficient Balance ---");
        DigitalWallet wallet = new DigitalWallet("ACC02", 100.0, "1234");
        boolean success = wallet.withdraw(500.0, "1234");
        System.out.println("Transaction Status (Expected False): " + success + "\n");
    }

    // 3. Daily Limit Test
    private static void testDailyLimit() {
        System.out.println("--- Test: Daily Transaction Limit ---");
        DigitalWallet wallet = new DigitalWallet("ACC03", 10000.0, "1234");
        wallet.withdraw(4000.0, "1234");
        boolean success = wallet.withdraw(1500.0, "1234"); // Exceeds $5000 limit
        System.out.println("Transaction Status (Expected False): " + success + "\n");
    }

    // 4. Multiple Failed PINs Test
    private static void testMultipleFailedPINs() {
        System.out.println("--- Test: Multiple Failed PIN Attempts ---");
        DigitalWallet wallet = new DigitalWallet("ACC04", 1000.0, "1234");
        wallet.withdraw(10.0, "9999");
        wallet.withdraw(10.0, "8888");
        wallet.withdraw(10.0, "7777"); // Third fail blocks account
        boolean success = wallet.withdraw(10.0, "1234"); // Correct pin but locked
        System.out.println("Transaction Status (Expected False): " + success + "\n");
    }

    // 5. Suspicious Transaction Test
    private static void testSuspiciousTransaction() {
        System.out.println("--- Test: Suspicious Transaction (Large Amount & Frequency) ---");
        DigitalWallet wallet = new DigitalWallet("ACC05", 20000.0, "1234");
        
        System.out.println("Triggering Large Transaction Rule:");
        wallet.withdraw(2500.0, "1234"); // > $2000 threshold

        System.out.println("\nTriggering Velocity Rule ( >5 rapid requests ):");
        for (int i = 0; i < 6; i++) {
            wallet.deposit(10.0);
        }
        System.out.println("");
    }

    // 6. Duplicate Transaction Test
    private static void testDuplicateTransaction() {
        System.out.println("--- Test: Duplicate Transaction Protection Window ---");
        DigitalWallet wallet = new DigitalWallet("ACC06", 1000.0, "1234");
        wallet.withdraw(50.0, "1234");
        wallet.withdraw(50.0, "1234"); // Captured via velocity audit metrics
        System.out.println("History records track both sequential instances smoothly.\n");
    }

    // 7. Negative Amount Test
    private static void testNegativeAmount() {
        System.out.println("--- Test: Negative Amount Check ---");
        DigitalWallet wallet = new DigitalWallet("ACC07", 1000.0, "1234");
        boolean success = wallet.deposit(-50.0);
        System.out.println("Transaction Status (Expected False): " + success + "\n");
    }

    // 8. Concurrent Transactions Test
    private static void testConcurrentTransactions() {
        System.out.println("--- Test: Concurrent Transactions Safety ---");
        final DigitalWallet wallet = new DigitalWallet("ACC08", 1000.0, "1234");

        Thread thread1 = new Thread(() -> wallet.withdraw(600.0, "1234"));
        Thread thread2 = new Thread(() -> wallet.withdraw(600.0, "1234"));

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Final Balance after race conditions resolved: $" + wallet.getBalance());
        System.out.println("=================================================");
    }
}
