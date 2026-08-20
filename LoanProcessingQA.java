public class LoanProcessingQA {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   RUNNING LOAN PROCESSING SYSTEM QA SUITE       ");
        System.out.println("=================================================\n");

        testAgeBoundaries();
        testInvalidSalary();
        testPoorCreditScore();
        testExistingLoanExceedingThreshold();
        testHighDebtToIncomeRatio();
        testDifferentEmploymentCategories();
        testBoundaryLoanAmounts();
        testEmiCalculationAccuracy();
        testInvalidInputHandling();
        testExceptionHandling();

        System.out.println("\n=================================================");
        System.out.println("            ALL QA TESTS COMPLETED               ");
        System.out.println("=================================================");
    }

    private static void testAgeBoundaries() {
        System.out.print("Test 1: Minimum/Maximum Age Boundary -> ");
        try {
            // Valid lower bound
            new LoanProcessingSystem("C01", 18, 5000, 0, 750, "Salaried", 10000, 12);
            // Valid upper bound
            new LoanProcessingSystem("C02", 65, 5000, 0, 750, "Salaried", 10000, 12);
            
            // Out of bounds checks
            try {
                new LoanProcessingSystem("C03", 17, 5000, 0, 750, "Salaried", 10000, 12);
                System.out.println("FAILED (Allowed under-age application)");
                return;
            } catch (IllegalArgumentException e) {}

            try {
                new LoanProcessingSystem("C04", 66, 5000, 0, 750, "Salaried", 10000, 12);
                System.out.println("FAILED (Allowed over-age application)");
                return;
            } catch (IllegalArgumentException e) {}

            System.out.println("PASSED");
        } catch (Exception e) {
            System.out.println("FAILED with unexpected error: " + e.getMessage());
        }
    }

    private static void testInvalidSalary() {
        System.out.print("Test 2: Invalid Salary handling -> ");
        try {
            new LoanProcessingSystem("C05", 30, -100, 0, 700, "Salaried", 5000, 12);
            System.out.println("FAILED (Accepted negative salary)");
        } catch (IllegalArgumentException e) {
            System.out.println("PASSED");
        }
    }

    private static void testPoorCreditScore() {
        System.out.print("Test 3: Poor Credit Score Rejection -> ");
        LoanProcessingSystem system = new LoanProcessingSystem("C06", 30, 6000, 0, 450, "Salaried", 5000, 12);
        if (system.getApprovalStatus().contains("Rejected")) {
            System.out.println("PASSED");
        } else {
            System.out.println("FAILED (Approved bad credit application)");
        }
    }

    private static void testExistingLoanExceedingThreshold() {
        System.out.print("Test 4: Existing Loan Exceeding Threshold -> ");
        // Salary = 2000, Limit threshold is 40x salary = 80,000. Existing = 90,000.
        LoanProcessingSystem system = new LoanProcessingSystem("C07", 35, 2000, 90000, 750, "Salaried", 5000, 12);
        if (system.getApprovalStatus().contains("Exceeds Safe Threshold") || system.getApprovalStatus().contains("Rejected")) {
            System.out.println("PASSED");
        } else {
            System.out.println("FAILED");
        }
    }

    private static void testHighDebtToIncomeRatio() {
        System.out.print("Test 5: High Debt-to-Income Ratio -> ");
        // Salary = 2000. Existing Loan = 60,000. Estimated existing EMI (2%) = 1200. DTI = 1200/2000 = 60% (> 50%)
        LoanProcessingSystem system = new LoanProcessingSystem("C08", 35, 2000, 60000, 750, "Salaried", 2000, 12);
        if (system.getApprovalStatus().contains("Debt-To-Income") || system.getDebtToIncomeRatio() > 50) {
            System.out.println("PASSED");
        } else {
            System.out.println("FAILED");
        }
    }

    private static void testDifferentEmploymentCategories() {
        System.out.print("Test 6: Different Employment Categories (Rate Premium) -> ");
        LoanProcessingSystem salaried = new LoanProcessingSystem("C09", 28, 5000, 0, 780, "Salaried", 10000, 12);
        LoanProcessingSystem selfEmployed = new LoanProcessingSystem("C10", 28, 5000, 0, 780, "Self-Employed", 10000, 12);

        if (selfEmployed.getInterestRate() > salaried.getInterestRate()) {
            System.out.println("PASSED");
        } else {
            System.out.println("FAILED (Risk premium not applied to Self-Employed category)");
        }
    }

    private static void testBoundaryLoanAmounts() {
        System.out.print("Test 7: Boundary Loan Amounts -> ");
        // Eligible capacity calculation check: Salary 5000 * 20 = 100,000 maximum boundary.
        LoanProcessingSystem safeBound = new LoanProcessingSystem("C11", 40, 5000, 0, 800, "Salaried", 99000, 24);
        LoanProcessingSystem unsafeBound = new LoanProcessingSystem("C12", 40, 5000, 0, 800, "Salaried", 101000, 24);

        if (safeBound.getApprovalStatus().equals("Approved") && unsafeBound.getApprovalStatus().contains("Exceeds")) {
            System.out.println("PASSED");
        } else {
            System.out.println("FAILED");
        }
    }

    private static void testEmiCalculationAccuracy() {
        System.out.print("Test 8: EMI Calculation Accuracy -> ");
        // Principal = 10000, Rate = 7.5% annually (0.625% monthly), Tenure = 12 months
        LoanProcessingSystem system = new LoanProcessingSystem("C13", 25, 6000, 0, 800, "Salaried", 10000, 12);
        double expectedEmi = 867.57; // Mathematically verified standard reducing balance return
        
        if (Math.abs(system.getEmi() - expectedEmi) < 1.0) {
            System.out.println("PASSED");
        } else {
            System.out.println("FAILED (Expected ~" + expectedEmi + " but got " + system.getEmi() + ")");
        }
    }

    private static void testInvalidInputHandling() {
        System.out.print("Test 9: Invalid Input Handling (Blank ID Check) -> ");
        try {
            new LoanProcessingSystem("   ", 25, 4000, 0, 700, "Salaried", 5000, 12);
            System.out.println("FAILED (Accepted whitespace/blank customer ID)");
        } catch (IllegalArgumentException e) {
            System.out.println("PASSED");
        }
    }

    private static void testExceptionHandling() {
        System.out.print("Test 10: General System Exception Safety -> ");
        try {
            // Passing a bad structure to see if system fails gracefully throwing predictable errors
            new LoanProcessingSystem("C14", -5, -200, -10, 1200, "Unknown", -500, -6);
            System.out.println("FAILED (System did not trap multi-layered input anomalies)");
        } catch (IllegalArgumentException e) {
            System.out.println("PASSED (System contained runtime exceptions cleanly)");
        }
    }
}
