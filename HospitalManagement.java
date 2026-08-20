import java.util.List;

public class HospitalManagement {

    // Helper classes to hold input structure
    public static class PatientDetails {
        public String name;
        public int age;
        public boolean isEmergency;
        public boolean isFollowUp;

        public PatientDetails(String name, int age, boolean isEmergency, boolean isFollowUp) {
            this.name = name;
            this.age = age;
            this.isEmergency = isEmergency;
            this.isFollowUp = isFollowUp;
        }
    }

    public static class InsuranceInformation {
        public boolean hasInsurance;
        public String provider;
        public double coveragePercentage; // e.g., 0.80 for 80%

        public InsuranceInformation(boolean hasInsurance, String provider, double coveragePercentage) {
            this.hasInsurance = hasInsurance;
            this.provider = provider;
            this.coveragePercentage = coveragePercentage;
        }
    }

    public static class BillInvoice {
        public double consultationFee;
        public double labCharges;
        public double medicineCharges;
        public double insuranceCoverage;
        public double patientPayableAmount;

        @Override
        public String toString() {
            return String.format(
                "Consultation: $%.2f | Labs: $%.2f | Medicines: $%.2f | Insurance Cover: -$%.2f | Patient Pays: $%.2f",
                consultationFee, labCharges, medicineCharges, insuranceCoverage, patientPayableAmount
            );
        }
    }

    // Main system parameters and calculation engine
    public static BillInvoice calculateBill(
            PatientDetails patient,
            String doctor,
            String department,
            String appointmentType,
            int consultationDurationMin,
            List<Double> labTestCosts,
            List<Double> medicineCosts,
            InsuranceInformation insurance) {

        BillInvoice bill = new BillInvoice();

        // 1. Calculate base Consultation Fee ($50 base + $2 per minute)
        double baseConsultation = 50.0 + (consultationDurationMin * 2.0);
        
        // Apply rule: Follow-up consultations get 50% off
        if (patient.isFollowUp) {
            baseConsultation *= 0.5;
        }
        // Apply rule: Emergency premium (+ $100 flat fee)
        if (patient.isEmergency) {
            baseConsultation += 100.0;
        }
        bill.consultationFee = baseConsultation;

        // 2. Calculate Lab Charges
        double totalLabs = 0;
        for (double cost : labTestCosts) {
            totalLabs += cost;
        }
        bill.labCharges = totalLabs;

        // 3. Calculate Medicine Charges
        double totalMedicines = 0;
        for (double cost : medicineCosts) {
            totalMedicines += cost;
        }
        bill.medicineCharges = totalMedicines;

        // Total gross amount before discounts or insurance
        double grossTotal = bill.consultationFee + bill.labCharges + bill.medicineCharges;

        // 4. Apply special rule: Senior Citizen discount (Age >= 60 gets 15% off gross bill before insurance)
        if (patient.age >= 60) {
            double seniorDiscount = grossTotal * 0.15;
            grossTotal -= seniorDiscount;
        }

        // 5. Calculate Insurance Coverage
        if (insurance != null && insurance.hasInsurance) {
            // Insurance covers a percentage of the remaining total gross amount
            bill.insuranceCoverage = grossTotal * insurance.coveragePercentage;
        } else {
            bill.insuranceCoverage = 0.0;
        }

        // 6. Calculate Patient Payable Amount
        bill.patientPayableAmount = grossTotal - bill.insuranceCoverage;
        if (bill.patientPayableAmount < 0) {
            bill.patientPayableAmount = 0;
        }

        return bill;
    }
}
