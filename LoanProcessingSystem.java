import java.io.Serializable;

public class LoanProcessingSystem implements Serializable {
    private static final long serialVersionUID = 1L;

    // Input fields
    private String customerId;
    private int age;
    private double monthlySalary;
    private double existingLoanAmount;
    private int creditScore;
    private String employmentType; // "Salaried" or "Self-Employed"
    private double requestedLoanAmount;
    private int loanTenureMonths;

    // Calculated fields
    private double debtToIncomeRatio;
    private double eligibleLoanAmount;
    private double interestRate;
    private double emi;
    private String approvalStatus;

    // Constructor with built-in validation
    public LoanProcessingSystem(String customerId, int age, double monthlySalary, double existingLoanAmount,
                                int creditScore, String employmentType, double requestedLoanAmount, int loanTenureMonths) {
        
        // Input validation checks
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid Customer ID");
        }
        if (age < 18 || age > 65) {
            throw new IllegalArgumentException("Age must be between 18 and 65");
        }
        if (monthlySalary <= 0) {
            throw new IllegalArgumentException("Monthly salary must be positive");
        }
        if (existingLoanAmount < 0) {
            throw new IllegalArgumentException("Existing loan amount cannot be negative");
        }
        if (creditScore < 300 || creditScore > 850) {
            throw new IllegalArgumentException("Credit score must be between 300 and 850");
        }
        if (requestedLoanAmount <= 0) {
            throw new IllegalArgumentException("Requested loan amount must be positive");
        }
        if (loanTenureMonths <= 0) {
            throw new IllegalArgumentException("Loan tenure must be positive");
        }

        this.customerId = customerId;
        this.age = age;
        this.monthlySalary = monthlySalary;
        this.existingLoanAmount = existingLoanAmount;
        this.creditScore = creditScore;
        this.employmentType = employmentType;
        this.requestedLoanAmount = requestedLoanAmount;
        this.loanTenureMonths = loanTenureMonths;

        // Process the application automatically upon creation
        processLoan();
    }

    private void processLoan() {
        // 1. Calculate Debt-to-Income (DTI) Ratio
        // Assuming a standard monthly obligation rule: estimation of existing EMI as 2% of total existing debt
        double estimatedExistingEmi = this.existingLoanAmount * 0.02;
        this.debtToIncomeRatio = (estimatedExistingEmi / this.monthlySalary) * 100;

        // 2. Determine base Interest Rate based on Credit Score and Employment
        if (this.creditScore >= 750) {
            this.interestRate = 7.5;
        } else if (this.creditScore >= 650) {
            this.interestRate = 9.0;
        } else {
            this.interestRate = 12.0; 
        }

        // Penalty for self-employed due to income instability risk
        if ("Self-Employed".equalsIgnoreCase(this.employmentType)) {
            this.interestRate += 1.0;
        }

        // 3. Determine Maximum Eligible Loan Amount based on salary multiplier
        if (this.creditScore >= 700) {
            this.eligibleLoanAmount = this.monthlySalary * 20;
        } else {
            this.eligibleLoanAmount = this.monthlySalary * 10;
        }

        // Deduct existing obligations from risk capacity
        this.eligibleLoanAmount = Math.max(0, this.eligibleLoanAmount - (this.existingLoanAmount * 0.5));

        // 4. Calculate EMI using standard standard reducing balance formula: [P x R x (1+R)^N]/[((1+R)^N)-1]
        double monthlyRate = (this.interestRate / 12) / 100;
        this.emi = (this.requestedLoanAmount * monthlyRate * Math.pow(1 + monthlyRate, this.loanTenureMonths)) 
                    / (Math.pow(1 + monthlyRate, this.loanTenureMonths) - 1);

        // 5. Final Approval / Rejection Status Check
        if (this.creditScore < 600) {
            this.approvalStatus = "Rejected (Poor Credit Score)";
        } else if (this.debtToIncomeRatio > 50) {
            this.approvalStatus = "Rejected (High Debt-To-Income Ratio)";
        } else if (this.existingLoanAmount > (this.monthlySalary * 40)) {
            this.approvalStatus = "Rejected (Existing Loan Exceeds Safe Threshold)";
        } else if (this.requestedLoanAmount > this.eligibleLoanAmount) {
            this.approvalStatus = "Rejected (Requested Amount Exceeds Eligible Maximum of " + String.format("%.2f", this.eligibleLoanAmount) + ")";
        } else {
            this.approvalStatus = "Approved";
        }
    }

    // Getters for evaluation by QA system
    public double getDebtToIncomeRatio() { return debtToIncomeRatio; }
    public double getEligibleLoanAmount() { return eligibleLoanAmount; }
    public double getInterestRate() { return interestRate; }
    public double getEmi() { return emi; }
    public String getApprovalStatus() { return approvalStatus; }
}
