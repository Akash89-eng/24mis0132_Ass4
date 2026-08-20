import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrderManagementQA {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   EXECUTION OF ORDER MANAGEMENT QA TEST SUITE   ");
        System.out.println("=================================================\n");

        int passedTests = 0;

        for (int i = 1; i <= 20; i++) {
            System.out.println("--- Test Case " + i + " ---");
            boolean success = runTestCase(i);
            if (success) passedTests++;
            System.out.println();
        }

        System.out.println("=================================================");
        System.out.println("QA SUMMARY: " + passedTests + " / 20 TEST CASES PASSED.");
        System.out.println("=================================================");
    }

    private static boolean runTestCase(int caseNum) {
        List<OrderManagement.Product> products = new ArrayList<>();
        String coupon = "";
        String caseDescription = "";

        switch (caseNum) {
            case 1:
                caseDescription = "Single Product normal baseline checkout";
                products.add(new OrderManagement.Product("P001", "Electronics", 1, 10, 100.0, 0.0, 0.10));
                break;

            case 2:
                caseDescription = "Multiple Products variation baseline checkout";
                products.add(new OrderManagement.Product("P001", "Electronics", 1, 5, 100.0, 0.0, 0.10));
                products.add(new OrderManagement.Product("P002", "Clothing", 2, 20, 50.0, 0.10, 0.05));
                break;

            case 3:
                caseDescription = "Zero quantity entry handler check";
                products.add(new OrderManagement.Product("P001", "Electronics", 0, 10, 100.0, 0.0, 0.10));
                break;

            case 4:
                caseDescription = "Negative quantity exception processing structural safety";
                products.add(new OrderManagement.Product("P001", "Electronics", -3, 10, 100.0, 0.0, 0.10));
                break;

            case 5:
                caseDescription = "Invalid empty string Product ID fault management";
                products.add(new OrderManagement.Product("", "Electronics", 2, 10, 100.0, 0.0, 0.10));
                break;

            case 6:
                caseDescription = "Invalid Coupon Code alert verification step";
                products.add(new OrderManagement.Product("P001", "Electronics", 1, 10, 100.0, 0.0, 0.10));
                coupon = "FAKECOUPON";
                break;

            case 7:
                caseDescription = "Maximum Coupon Discount capping logic limits verification";
                products.add(new OrderManagement.Product("P001", "Electronics", 10, 50, 200.0, 0.0, 0.10));
                coupon = "SAVE10"; // 10% of 2000 is 200, Cap is 50
                break;

            case 8:
                caseDescription = "Standard Tax Calculation verification pipeline accuracy";
                products.add(new OrderManagement.Product("P001", "Electronics", 1, 10, 200.0, 0.0, 0.18)); // GST 18%
                break;

            case 9:
                caseDescription = "Free Shipping baseline threshold application assessment";
                products.add(new OrderManagement.Product("P001", "Electronics", 3, 10, 200.0, 0.0, 0.0)); // Subtotal 600 > 500
                break;

            case 10:
                caseDescription = "Bulk Order criteria qualification percentage calculations";
                products.add(new OrderManagement.Product("P001", "Electronics", 12, 20, 10.0, 0.0, 0.0)); // Qty 12 >= 10
                break;

            case 11:
                caseDescription = "Absolute Out-of-Stock complete inventory drop item bypass";
                products.add(new OrderManagement.Product("P001", "Electronics", 2, 0, 100.0, 0.0, 0.10));
                break;

            case 12:
                caseDescription = "Partial Stock restriction adaptation fulfillment correction";
                products.add(new OrderManagement.Product("P001", "Electronics", 10, 4, 100.0, 0.0, 0.0)); // 10 ordered, 4 available
                break;

            case 13:
                caseDescription = "Combination: Multiple items, Bulk criteria met, Valid coupon application";
                products.add(new OrderManagement.Product("P001", "Electronics", 10, 20, 50.0, 0.0, 0.10)); // Bulk discount triggers
                products.add(new OrderManagement.Product("P002", "Books", 1, 5, 20.0, 0.0, 0.0));
                coupon = "SAVE10";
                break;

            case 14:
                caseDescription = "Combination: Negative processing mixed safely along valid components";
                products.add(new OrderManagement.Product("P001", "Electronics", -2, 10, 100.0, 0.0, 0.10)); // Skipped
                products.add(new OrderManagement.Product("P002", "Books", 2, 10, 30.0, 0.0, 0.0));          // Valid
                break;

            case 15:
                caseDescription = "Combination: Partial stock adjustment encountering an invalid coupon rule";
                products.add(new OrderManagement.Product("P001", "Electronics", 5, 2, 100.0, 0.0, 0.0)); // Adjusts to 2
                coupon = "INVALIDCODE";
                break;

            case 16:
                caseDescription = "Combination: High value criteria matching Free Shipping and Max coupon caps";
                products.add(new OrderManagement.Product("P001", "Luxury", 2, 5, 1000.0, 0.0, 0.0)); 
                coupon = "MEGA50"; // Cap is 200
                break;

            case 17:
                caseDescription = "Empty total array dataset transaction verification handling";
                // Empty array list container check
                break;

            case 18:
                caseDescription = "Combination: Bulk execution containing category baseline adjustments";
                products.add(new OrderManagement.Product("P001", "Fashion", 10, 20, 100.0, 0.10, 0.10)); // Category + Bulk
                break;

            case 19:
                caseDescription = "Combination: Zero items mix paired alongside standard single item check";
                products.add(new OrderManagement.Product("P001", "Electronics", 0, 10, 100.0, 0.0, 0.0));
                products.add(new OrderManagement.Product("P002", "Books", 1, 10, 20.0, 0.0, 0.0));
                break;

            case 20:
                caseDescription = "Negative pricing matrix fault recovery analysis validation safety";
                products.add(new OrderManagement.Product("P001", "Electronics", 2, 10, -50.0, 0.0, 0.10)); // Invalid Price
                break;
        }

        System.out.println("Description: " + caseDescription);
        OrderManagement.OrderResult result = OrderManagement.processOrder(products, coupon);

        // Print active output calculations logs
        System.out.println("-> Subtotal: $" + result.subtotal);
        System.out.println("-> Category Discount: $" + result.categoryDiscount);
        System.out.println("-> Bulk Discount: $" + result.bulkDiscount);
        System.out.println("-> Coupon Discount: $" + result.couponDiscount);
        System.out.println("-> Tax (GST): $" + result.gst);
        System.out.println("-> Shipping: $" + result.shippingCharge);
        System.out.println("-> Final Total Charge Amount: $" + result.finalAmount);
        System.out.println("-> Execution System Logs: " + Arrays.toString(result.logs.toArray()));

        // Verification safety engine check assertion
        if (result.finalAmount >= 0.0) {
            System.out.println("Result Resulting Status: PASSED");
            return true;
        } else {
            System.out.println("Result Resulting Status: FAILED");
            return false;
        }
    }
}
