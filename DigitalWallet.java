import java.util.*;

public class DigitalWallet {
    private final String accountId;
    private double balance;
    private final String pin;
    private int failedPinAttempts;
    private double dailySpent;
    private final List<Transaction> transactionHistory;
    
    // Configurable thresholds for Fraud Detection
    private static final double DAILY_LIMIT = 5000.0;
    private static final double LARGE_TRANSACTION_THRESHOLD = 2000.0;
    private static final int MAX_FAILED_PIN_ATTEMPTS = 3;
    private static final int FRAUD_TIME_WINDOW_MINUTES = 10;
    private static final int FRAUD_MAX_TRANSACTIONS = 5;

    public static class Transaction {
        String type;
        double amount;
        long timestamp;
        boolean isFlagged;

        public Transaction(String type, double amount, boolean isFlagged) {
            this.type = type;
            this.amount = amount;
            this.timestamp = System.currentTimeMillis();
            this.isFlagged = isFlagged;
        }
    }

    // Account Creation
    public DigitalWallet(String accountId, double initialDeposit, String pin) {
        if (initialDeposit < 0) {
            throw new IllegalArgumentException("Initial deposit cannot be negative.");
        }
        this.accountId = accountId;
        this.balance = initialDeposit;
        this.pin = pin;
        this.failedPinAttempts = 0;
        this.dailySpent = 0.0;
        this.transactionHistory = new ArrayList<>();
    }

    // Balance Verification
    public double getBalance() {
        return this.balance;
    }

    public String getAccountId() {
        return this.accountId;
    }

    public List<Transaction> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    // Security Check
    private boolean verifyPin(String inputPin) {
        if (failedPinAttempts >= MAX_FAILED_PIN_ATTEMPTS) {
            System.out.println("[ALERT] Account locked due to multiple failed PIN attempts.");
            return false;
        }
        if (this.pin.equals(inputPin)) {
            failedPinAttempts = 0; // Reset on success
            return true;
        } else {
            failedPinAttempts++;
            return false;
        }
    }

    // Basic Fraud Detection Mechanism
    private boolean checkFraud(double amount) {
        boolean flagged = false;
        long currentTime = System.currentTimeMillis();

        // 1. More than 5 transactions in 10 minutes
        long tenMinutesAgo = currentTime - (FRAUD_TIME_WINDOW_MINUTES * 60 * 1000L);
        long recentTransactionsCount = transactionHistory.stream()
                .filter(t -> t.timestamp >= tenMinutesAgo)
                .count();
        if (recentTransactionsCount >= FRAUD_MAX_TRANSACTIONS) {
            System.out.println("[FRAUD DETECTED] High frequency of transactions ( >5 in 10 mins ).");
            flagged = true;
        }

        // 2. Large transaction
        if (amount >= LARGE_TRANSACTION_THRESHOLD) {
            System.out.println("[FRAUD DETECTED] Large transaction amount threshold breached.");
            flagged = true;
        }

        // 3. Multiple failed PIN attempts
        if (failedPinAttempts >= MAX_FAILED_PIN_ATTEMPTS) {
            System.out.println("[FRAUD DETECTED] Multiple failed PIN attempts registered.");
            flagged = true;
        }

        // 4. Unusual transaction amount (e.g., negative or abnormally precise fraction)
        if (amount <= 0) {
            System.out.println("[FRAUD DETECTED] Unusual transaction amount detected.");
            flagged = true;
        }

        return flagged;
    }

    // Deposit Logic
    public synchronized boolean deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Transaction Rejected: Negative or zero amount.");
            return false;
        }
        boolean isSuspicious = checkFraud(amount);
        this.balance += amount;
        transactionHistory.add(new Transaction("DEPOSIT", amount, isSuspicious));
        return true;
    }

    // Withdrawal Logic
    public synchronized boolean withdraw(double amount, String pin) {
        if (!verifyPin(pin)) {
            checkFraud(amount); // Triggers security warning log
            return false;
        }
        if (amount <= 0) {
            System.out.println("Transaction Rejected: Negative or zero amount.");
            return false;
        }
        // Insufficient balance validation
        if (amount > this.balance) {
            System.out.println("Transaction Rejected: Insufficient balance.");
            return false;
        }
        // Daily transaction limit verification
        if (this.dailySpent + amount > DAILY_LIMIT) {
            System.out.println("Transaction Rejected: Daily transaction limit exceeded.");
            return false;
        }

        boolean isSuspicious = checkFraud(amount);
        this.balance -= amount;
        this.dailySpent += amount;
        transactionHistory.add(new Transaction("WITHDRAWAL", amount, isSuspicious));
        return true;
    }

    // Money Transfer Logic
    public synchronized boolean transfer(DigitalWallet targetAccount, double amount, String pin) {
        if (targetAccount == null) {
            System.out.println("Transaction Rejected: Target account does not exist.");
            return false;
        }
        if (!verifyPin(pin)) {
            checkFraud(amount);
            return false;
        }
        if (amount <= 0) {
            System.out.println("Transaction Rejected: Negative or zero amount.");
            return false;
        }
        if (amount > this.balance) {
            System.out.println("Transaction Rejected: Insufficient balance.");
            return false;
        }
        if (this.dailySpent + amount > DAILY_LIMIT) {
            System.out.println("Transaction Rejected: Daily transaction limit exceeded.");
            return false;
        }

        boolean isSuspicious = checkFraud(amount);
        
        // Execute atomicity
        this.balance -= amount;
        this.dailySpent += amount;
        targetAccount.deposit(amount);

        transactionHistory.add(new Transaction("TRANSFER_OUT to " + targetAccount.getAccountId(), amount, isSuspicious));
        return true;
    }
}
