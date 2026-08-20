import java.util.Arrays;
import java.util.Collections;

public class HospitalManagementQA {
    public static void main(String[] args) {
        System.out.println("--- Running Hospital Management Billing QA Tests ---\n");

        // Scenario 1: Standard Patient (Regular, No Insurance, No Special Discounts)
        HospitalManagement.PatientDetails p1 = new HospitalManagement.PatientDetails("John Doe", 35, false, false);
        HospitalManagement.InsuranceInformation i1 = new HospitalManagement.InsuranceInformation(false, "None", 0.0);
        HospitalManagement.BillInvoice bill1 = HospitalManagement.calculateBill(
                p1, "Dr. Smith", "General Medicine", "Routine Checkup", 15,
                Arrays.asList(40.0, 60.0), Arrays.asList(25.50, 14.50), i1
        );
        System.out.println("Scenario 1 [Standard Patient]:");
        System.out.println(bill1);
        // Verification logic
        assert bill1.consultationFee == 80.0 : "Test Failed: Consultation unexpected";
        assert bill1.patientPayableAmount == 220.0 : "Test Failed: Payable amount mismatch";
        System.out.println("-> PASS\n");


        // Scenario 2: Emergency Patient (Urgent status premium applied)
        HospitalManagement.PatientDetails p2 = new HospitalManagement.PatientDetails("Alex Swift", 28, true, false);
        HospitalManagement.InsuranceInformation i2 = new HospitalManagement.InsuranceInformation(false, "None", 0.0);
        HospitalManagement.BillInvoice bill2 = HospitalManagement.calculateBill(
                p2, "Dr. Jones", "ER", "Emergency", 10,
                Arrays.asList(150.0), Arrays.asList(50.0), i2
        );
        System.out.println("Scenario 2 [Emergency Patient]:");
        System.out.println(bill2);
        assert bill2.consultationFee == 170.0 : "Test Failed: Emergency premium missing";
        System.out.println("-> PASS\n");


        // Scenario 3: Senior Citizen (15% overall age discount)
        HospitalManagement.PatientDetails p3 = new HospitalManagement.PatientDetails("Robert Eld", 67, false, false);
        HospitalManagement.InsuranceInformation i3 = new HospitalManagement.InsuranceInformation(false, "None", 0.0);
        HospitalManagement.BillInvoice bill3 = HospitalManagement.calculateBill(
                p3, "Dr. Adams", "Cardiology", "Consultation", 20,
                Collections.emptyList(), Arrays.asList(100.0), i3
        );
        System.out.println("Scenario 3 [Senior Citizen]:");
        System.out.println(bill3);
        // Base gross: 90 (fee) + 100 (meds) = 190. Senior Discount = 190 * 0.15 = 28.5. Net = 161.5
        assert bill3.patientPayableAmount == 161.50 : "Test Failed: Senior discount calculation wrong";
        System.out.println("-> PASS\n");


        // Scenario 4: Insured Patient (80% coverage)
        HospitalManagement.PatientDetails p4 = new HospitalManagement.PatientDetails("Emily Cover", 40, false, false);
        HospitalManagement.InsuranceInformation i4 = new HospitalManagement.InsuranceInformation(true, "BlueCross", 0.80);
        HospitalManagement.BillInvoice bill4 = HospitalManagement.calculateBill(
                p4, "Dr. Evans", "Pediatrics", "Routine Checkup", 15,
                Arrays.asList(100.0), Arrays.asList(20.0), i4
        );
        System.out.println("Scenario 4 [Insurance Covered]:");
        System.out.println(bill4);
        // Total gross: 80 (fee) + 100 (labs) + 20 (meds) = 200. Insurance covers 160. Patient pays 40.
        assert bill4.insuranceCoverage == 160.0 : "Test Failed: Insurance payout incorrect";
        assert bill4.patientPayableAmount == 40.0 : "Test Failed: Insured patient payable mismatch";
        System.out.println("-> PASS\n");


        // Scenario 5: Follow-Up Consultation (50% fee discount)
        HospitalManagement.PatientDetails p5 = new HospitalManagement.PatientDetails("Clara Trace", 30, false, true);
        HospitalManagement.InsuranceInformation i5 = new HospitalManagement.InsuranceInformation(false, "None", 0.0);
        HospitalManagement.BillInvoice bill5 = HospitalManagement.calculateBill(
                p5, "Dr. Smith", "General Medicine", "Follow-up", 10,
                Collections.emptyList(), Collections.emptyList(), i5
        );
        System.out.println("Scenario 5 [Follow-Up]:");
        System.out.println(bill5);
        // Base fee: 50 + (10*2) = 70. Follow-up 50% discount -> 35.0
        assert bill5.consultationFee == 35.0 : "Test Failed: Follow-up markdown omitted";
        System.out.println("-> PASS\n");

        System.out.println("--- All billing scenarios verified successfully! ---");
    }
}
