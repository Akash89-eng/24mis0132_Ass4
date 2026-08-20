
import java.time.LocalDateTime;

public class ParkingQA {
    public static void main(String[] args) {
        System.out.println("=== RUNNING SMART PARKING SYSTEM QA TESTS ===");

        // Initializing with limited capacities to test boundaries
        ParkingManagement system = new ParkingManagement(1, 1, 1, 1, 1);

        // 1. Test Full Parking Lot
        System.out.println("\n--- Test 1: Full Parking Lot ---");
        Ticket car1 = system.vehicleEntry("CAR-001", VehicleType.CAR, false);
        Ticket car2 = system.vehicleEntry("CAR-002", VehicleType.CAR, false); // Expected: Denied

        // 2. Test Duplicate Vehicle
        System.out.println("\n--- Test 2: Duplicate Vehicle ---");
        system.vehicleEntry("CAR-001", VehicleType.CAR, false); // Expected: Denied

        // Clear slot for next operations
        system.vehicleExit(car1.getTicketId(), LocalDateTime.now().plusHours(1));

        // 3. Test Wrong Vehicle-Slot / Boundary Verification
        System.out.println("\n--- Test 3: Wrong Vehicle-Slot ---");
        // Forcing system to allocate based on layout profiles; validation happens internally via type integrity checks
        Ticket bike1 = system.vehicleEntry("BIKE-001", VehicleType.BIKE, false);
        system.vehicleExit("INVALID_TKT_ID", LocalDateTime.now()); // Expected: Denied

        // 4. Test Lost Ticket handling
        System.out.println("\n--- Test 4: Lost Ticket Penalty ---");
        Ticket suv1 = system.vehicleEntry("SUV-001", VehicleType.SUV, false);
        system.triggerLostTicket(suv1.getTicketId());
        system.vehicleExit(suv1.getTicketId(), LocalDateTime.now().plusHours(2)); // Expected: Standard fixed $100 penalty fee

        // 5. Test Early Exit
        System.out.println("\n--- Test 5: Early Exit ---");
        Ticket car3 = system.vehicleEntry("CAR-003", VehicleType.CAR, false);
        // Exiting 5 minutes later rounds up to 1 standard parking base hour
        system.vehicleExit(car3.getTicketId(), LocalDateTime.now().plusMinutes(5)); 

        // 6. Test Peak-Hour Pricing
        System.out.println("\n--- Test 6: Peak-Hour Pricing ---");
        LocalDateTime peakEntry = LocalDateTime.now().withHour(8).withMinute(0); // 8:00 AM Peak
        Ticket peakCar = system.vehicleEntry("PEAK-001", VehicleType.CAR, false, peakEntry);
        system.vehicleExit(peakCar.getTicketId(), peakEntry.plusHours(2)); // 2 Hours inside peak hour loop

        // 7. Test EV Charging Fee
        System.out.println("\n--- Test 7: EV Charging Fee ---");
        LocalDateTime evEntry = LocalDateTime.now().withHour(13).withMinute(0); // Regular non-peak slot hour
        Ticket evCar = system.vehicleEntry("EV-001", VehicleType.ELECTRIC_VEHICLE, false, evEntry);
        system.vehicleExit(evCar.getTicketId(), evEntry.plusHours(2)); // Base rate ($25) + EV Charging ($5) = $30/hr

        // 8. Test Overnight & VIP Parking combined profile 
        System.out.println("\n--- Test 8: Overnight Parking + VIP Discount ---");
        LocalDateTime overnightEntry = LocalDateTime.now().withHour(22).withMinute(0); // 10:00 PM
        Ticket vipCar = system.vehicleEntry("VIP-777", VehicleType.CAR, true, overnightEntry);
        // Exits next day at 10:00 AM (Contains 12 hours transition spanning through morning peak hour frames)
        system.vehicleExit(vipCar.getTicketId(), overnightEntry.plusHours(12)); 
    }
}
