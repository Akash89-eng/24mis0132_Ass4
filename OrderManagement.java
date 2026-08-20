import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderManagement {

    // --- Data Models ---
    
    public static class Product {
        public String id;
        public String category;
        public int initialQuantity; // Offered quantity
        public int availableStock;   // Warehouse stock
        public double unitPrice;
        public double categoryDiscountPercent; // e.g., 0.10 for 10%
        public double taxPercent;             // e.g., 0.18 for 18% GST

        public Product(String id, String category, int initialQuantity, int availableStock, 
                       double unitPrice, double categoryDiscountPercent, double taxPercent) {
            this.id = id;
            this.category = category;
            this.initialQuantity = initialQuantity;
            this.availableStock = availableStock;
            this.unitPrice = unitPrice;
            this.categoryDiscountPercent = categoryDiscountPercent;
            this.taxPercent = taxPercent;
        }
    }

    public static class Coupon {
        public String code;
        public double discountPercent;
        public double maxDiscountLimit;

        public Coupon(String code, double discountPercent, double maxDiscountLimit) {
            this.code = code;
            this.discountPercent = discountPercent;
            this.maxDiscountLimit = maxDiscountLimit;
        }
    }

    public static class OrderResult {
        public double subtotal = 0.0;
        public double categoryDiscount = 0.0;
        public double bulkDiscount = 0.0;
        public double couponDiscount = 0.0;
        public double gst = 0.0;
        public double shippingCharge = 0.0;
        public double finalAmount = 0.0;
        public List<String> logs = new ArrayList<>();
    }

    // --- Validation & Business Rules Constants ---
    private static final double FREE_SHIPPING_THRESHOLD = 500.0;
    private static final double FLAT_SHIPPING_CHARGE = 50.0;
    private static final int BULK_ORDER_QUANTITY_THRESHOLD = 10;
    private static final double BULK_DISCOUNT_PERCENT = 0.05; // 5% additional bulk discount

    // Simulated active coupon database
    private static final Map<String, Coupon> COUPON_DB = new HashMap<>();
    static {
        COUPON_DB.put("SAVE10", new Coupon("SAVE10", 0.10, 50.0));
        COUPON_DB.put("MEGA50", new Coupon("MEGA50", 0.15, 200.0));
    }

    // --- Core Processing Logic ---
    public static OrderResult processOrder(List<Product> products, String couponCode) {
        OrderResult res = new OrderResult();

        if (products == null || products.isEmpty()) {
            res.logs.add("Error: Order contains no products.");
            return res;
        }

        double totalItemSubtotal = 0.0;
        double totalCategoryDiscount = 0.0;
        double totalBulkDiscount = 0.0;
        double totalTaxableAmount = 0.0;
        double totalGst = 0.0;

        for (Product p : products) {
            // 1. Data Validation Checks
            if (p.id == null || p.id.trim().isEmpty()) {
                res.logs.add("Error: Invalid or missing Product ID.");
                continue;
            }
            if (p.initialQuantity < 0) {
                res.logs.add("Error: Product " + p.id + " has a negative quantity (" + p.initialQuantity + "). Line item skipped.");
                continue;
            }
            if (p.initialQuantity == 0) {
                res.logs.add("Warning: Product " + p.id + " has zero quantity requested.");
                continue;
            }
            if (p.unitPrice < 0) {
                res.logs.add("Error: Product " + p.id + " has a negative unit price. Line item skipped.");
                continue;
            }

            // 2. Inventory Check (Out-of-stock management)
            int finalQuantity = p.initialQuantity;
            if (p.availableStock <= 0) {
                res.logs.add("Warning: Product " + p.id + " is completely out of stock. Item skipped.");
                continue;
            } else if (p.initialQuantity > p.availableStock) {
                finalQuantity = p.availableStock;
                res.logs.add("Warning: Product " + p.id + " has limited stock. Quantity adjusted from " 
                             + p.initialQuantity + " to " + finalQuantity + ".");
            }

            // 3. Item Level Calculations
            double itemSubtotal = finalQuantity * p.unitPrice;
            double itemCategoryDiscount = itemSubtotal * p.categoryDiscountPercent;
            
            // Bulk-order discount application
            double itemBulkDiscount = 0.0;
            if (finalQuantity >= BULK_ORDER_QUANTITY_THRESHOLD) {
                itemBulkDiscount = (itemSubtotal - itemCategoryDiscount) * BULK_DISCOUNT_PERCENT;
                res.logs.add("Applied 5% bulk discount for Product " + p.id + " due to volume.");
            }

            double itemTaxable = itemSubtotal - itemCategoryDiscount - itemBulkDiscount;
            double itemGst = itemTaxable * p.taxPercent;

            // Accumulate global amounts
            totalItemSubtotal += itemSubtotal;
            totalCategoryDiscount += itemCategoryDiscount;
            totalBulkDiscount += itemBulkDiscount;
            totalTaxableAmount += itemTaxable;
            totalGst += itemGst;
        }

        res.subtotal = totalItemSubtotal;
        res.categoryDiscount = totalCategoryDiscount;
        res.bulkDiscount = totalBulkDiscount;
        res.gst = totalGst;

        // 4. Coupon Discount Processing
        double netBeforeCoupon = totalTaxableAmount;
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            Coupon cp = COUPON_DB.get(couponCode.trim().toUpperCase());
            if (cp == null) {
                res.logs.add("Warning: Coupon code '" + couponCode + "' is invalid or expired.");
            } else {
                double calculatedCouponDiscount = netBeforeCoupon * cp.discountPercent;
                // Enforce maximum discount limit rules
                if (calculatedCouponDiscount > cp.maxDiscountLimit) {
                    res.couponDiscount = cp.maxDiscountLimit;
                    res.logs.add("Coupon '" + couponCode + "' applied. Discount capped at max limit: $" + cp.maxDiscountLimit);
                } else {
                    res.couponDiscount = calculatedCouponDiscount;
                    res.logs.add("Coupon '" + couponCode + "' applied successfully.");
                }
                netBeforeCoupon -= res.couponDiscount;
            }
        }

        // Recalculate global tax if coupon alters the global taxable base evenly, 
        // or apply straight calculated GST aggregates. Here we use the precise line-item GST.
        
        // 5. Shipping Calculation (Threshold configurations)
        double checkAmountForShipping = netBeforeCoupon; 
        if (checkAmountForShipping <= 0) {
            res.shippingCharge = 0.0;
        } else if (checkAmountForShipping >= FREE_SHIPPING_THRESHOLD) {
            res.shippingCharge = 0.0;
            res.logs.add("Order qualifies for Free Shipping (Subtotal over $" + FREE_SHIPPING_THRESHOLD + ").");
        } else {
            res.shippingCharge = FLAT_SHIPPING_CHARGE;
        }

        // 6. Final Calculation Formula
        res.finalAmount = netBeforeCoupon + res.gst + res.shippingCharge;
        if (res.finalAmount < 0) {
            res.finalAmount = 0.0;
        }

        return res;
    }
}
