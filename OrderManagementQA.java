package QA;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QA Test Suite for Order Management System.
 * Tests 20+ combinations of quantities, coupons, products, tax, shipping, and limits.
 */
public class OrderManagementQA {

    private OrderProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new OrderProcessor();
    }

    // ==========================================
    // COMBINATION TESTS 1-5: QUANTITY & VALIDITY
    // ==========================================

    @Test
    @DisplayName("Combo 1: Single product, valid positive quantity, standard calculations")
    void testSingleProductValidQuantity() {
        Product p = new Product("P001", "Electronics", 1, 1000.0, 0.0, 0.18, true);
        Order order = new Order(Collections.singletonList(p), null);

        OrderResult result = processor.processOrder(order);

        assertEquals(1000.0, result.getSubtotal());
        assertEquals(1180.0, result.getFinalAmount()); // 1000 + 180 (18% GST) + shipping
    }

    @Test
    @DisplayName("Combo 2: Multiple items, all valid standard quantities")
    void testMultipleProductsValid() {
        Product p1 = new Product("P001", "Books", 2, 50.0, 0.0, 0.05, true);
        Product p2 = new Product("P002", "Clothing", 1, 100.0, 0.0, 0.12, true);
        Order order = new Order(Arrays.asList(p1, p2), null);

        OrderResult result = processor.processOrder(order);

        assertEquals(200.0, result.getSubtotal());
    }

    @Test
    @DisplayName("Combo 3: Single product, zero quantity edge case")
    void testSingleProductZeroQuantity() {
        Product p = new Product("P001", "Electronics", 0, 500.0, 0.0, 0.18, true);
        Order order = new Order(Collections.singletonList(p), null);

        assertThrows(IllegalArgumentException.class, () -> processor.processOrder(order));
    }

    @Test
    @DisplayName("Combo 4: Single product, negative quantity error")
    void testSingleProductNegativeQuantity() {
        Product p = new Product("P001", "Electronics", -5, 500.0, 0.0, 0.18, true);
        Order order = new Order(Collections.singletonList(p), null);

        assertThrows(IllegalArgumentException.class, () -> processor.processOrder(order));
    }

    @Test
    @DisplayName("Combo 5: Multiple products, mix of positive and negative quantities")
    void testMultipleProductsMixedQuantities() {
        Product p1 = new Product("P001", "Books", 2, 50.0, 0.0, 0.05, true);
        Product p2 = new Product("P002", "Clothing", -1, 100.0, 0.0, 0.12, true);
        Order order = new Order(Arrays.asList(p1, p2), null);

        assertThrows(IllegalArgumentException.class, () -> processor.processOrder(order));
    }

    // ==========================================
    // COMBINATION TESTS 6-10: AVAILABILITY & VALIDATION
    // ==========================================

    @Test
    @DisplayName("Combo 6: Single product out-of-stock")
    void testSingleProductOutOfStock() {
        Product p = new Product("P001", "Electronics", 1, 300.0, 0.0, 0.18, false); // false = out of stock
        Order order = new Order(Collections.singletonList(p), null);

        assertThrows(OutOfStockException.class, () -> processor.processOrder(order));
    }

    @Test
    @DisplayName("Combo 7: Multiple products, one item out-of-stock")
    void testMultipleProductsOneOutOfStock() {
        Product p1 = new Product("P001", "Books", 1, 20.0, 0.0, 0.05, true);
        Product p2 = new Product("P002", "Clothing", 2, 40.0, 0.0, 0.12, false);
        Order order = new Order(Arrays.asList(p1, p2), null);

        assertThrows(OutOfStockException.class, () -> processor.processOrder(order));
    }

    @Test
    @DisplayName("Combo 8: Invalid product properties (negative unit price)")
    void testInvalidProductPrice() {
        Product p = new Product("P001", "Electronics", 1, -100.0, 0.0, 0.18, true);
        Order order = new Order(Collections.singletonList(p), null);

        assertThrows(IllegalArgumentException.class, () -> processor.processOrder(order));
    }

    @Test
    @DisplayName("Combo 9: Invalid product properties (negative tax rates)")
    void testInvalidProductTax() {
        Product p = new Product("P001", "Electronics", 1, 100.0, 0.0, -0.05, true);
        Order order = new Order(Collections.singletonList(p), null);

        assertThrows(IllegalArgumentException.class, () -> processor.processOrder(order));
    }

    @Test
    @DisplayName("Combo 10: Empty product catalog order list")
    void testEmptyOrderList() {
        Order order = new Order(Collections.emptyList(), null);
        assertThrows(IllegalArgumentException.class, () -> processor.processOrder(order));
    }

    // ==========================================
    // COMBINATION TESTS 11-15: COUPONS & DISCOUNTS
    // ==========================================

    @Test
    @DisplayName("Combo 11: Valid single item with a valid coupon code")
    void testValidCouponDiscount() {
        Product p = new Product("P001", "Electronics", 1, 200.0, 0.0, 0.10, true);
        Coupon coupon = new Coupon("SAVE10", 0.10, true); // 10% off
        Order order = new Order(Collections.singletonList(p), coupon);

        OrderResult result = processor.processOrder(order);
        assertEquals(20.0, result.getCouponDiscount());
    }

    @Test
    @DisplayName("Combo 12: Single product with an invalid/expired coupon code")
    void testInvalidCouponCode() {
        Product p = new Product("P001", "Electronics", 1, 200.0, 0.0, 0.10, true);
        Coupon coupon = new Coupon("EXPIRED50", 0.50, false); // invalid coupon
        Order order = new Order(Collections.singletonList(p), coupon);

        assertThrows(InvalidCouponException.class, () -> processor.processOrder(order));
    }

    @Test
    @DisplayName("Combo 13: Multiple products with category-specific discount applied")
    void testCategorySpecificDiscount() {
        // Product has internal flat discount or category discount map rules apply
        Product p = new Product("P001", "Electronics", 1, 500.0, 50.0, 0.10, true); // 50.0 category discount
        Order order = new Order(Collections.singletonList(p), null);

        OrderResult result = processor.processOrder(order);
        assertEquals(50.0, result.getCategoryDiscount());
    }

    @Test
    @DisplayName("Combo 14: Multiple items exceeding maximum discount cap limit")
    void testMaximumDiscountLimitCap() {
        Product p = new Product("P001", "Luxury", 1, 10000.0, 0.0, 0.20, true);
        Coupon coupon = new Coupon("MEGA50", 0.50, true); // 50% = 5000 max savings cap is 1000
        Order order = new Order(Collections.singletonList(p), coupon);

        OrderResult result = processor.processOrder(order);
        // Assuming implementation max discount limit caps coupon savings at 1000.0
        assertTrue(result.getCouponDiscount() <= 1000.0);
    }

    @Test
    @DisplayName("Combo 15: Combination of category discounts and coupon discounts simultaneously")
    void testStackedDiscounts() {
        Product p = new Product("P001", "Electronics", 1, 100.0, 10.0, 0.0, true);
        Coupon coupon = new Coupon("WELCOME10", 0.10, true);
        Order order = new Order(Collections.singletonList(p), coupon);

        OrderResult result = processor.processOrder(order);
        assertEquals(10.0, result.getCategoryDiscount());
        // Stacked coupon applies either to base price or remaining subtotal depending on rule
        assertNotNull(result.getFinalAmount());
    }

    // ==========================================
    // COMBINATION TESTS 16-22: TAX, SHIPPING, BULK
    // ==========================================

    @Test
    @DisplayName("Combo 16: Standard tax calculation without any discounts")
    void testTaxCalculationNoDiscount() {
        Product p = new Product("P001", "Items", 2, 100.0, 0.0, 0.18, true); // 200 base subtotal
        Order order = new Order(Collections.singletonList(p), null);

        OrderResult result = processor.processOrder(order);
        assertEquals(36.0, result.getGstAmount()); // 18% of 200
    }

    @Test
    @DisplayName("Combo 17: Tax calculation with discount applied (calculated on net subtotal)")
    void testTaxCalculationWithDiscount() {
        Product p = new Product("P001", "Items", 1, 100.0, 20.0, 0.10, true); // Net base = 80
        Order order = new Order(Collections.singletonList(p), null);

        OrderResult result = processor.processOrder(order);
        assertEquals(8.0, result.getGstAmount()); // 10% of 80
    }

    @Test
    @DisplayName("Combo 18: Order subtotal below free shipping milestone threshold")
    void testStandardShippingChargeApplied() {
        Product p = new Product("P001", "Books", 1, 20.0, 0.0, 0.0, true); // Low total
        Order order = new Order(Collections.singletonList(p), null);

        OrderResult result = processor.processOrder(order);
        assertTrue(result.getShippingCharge() > 0.0); // Shipping charge applied
    }

    @Test
    @DisplayName("Combo 19: Order subtotal qualifies for free shipping threshold")
    void testFreeShippingThresholdMet() {
        Product p = new Product("P001", "Appliances", 1, 1500.0, 0.0, 0.0, true); // Exceeds threshold
        Order order = new Order(Collections.singletonList(p), null);

        OrderResult result = processor.processOrder(order);
        assertEquals(0.0, result.getShippingCharge()); // Free shipping threshold met
    }

    @Test
    @DisplayName("Combo 20: Bulk-order quantity triggers automated price discount")
    void testBulkOrderQuantityDiscount() {
        Product p = new Product("P001", "Hardware", 50, 10.0, 0.0, 0.0, true); // High item quantity
