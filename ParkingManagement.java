// ParkingManagement.java
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;

enum VehicleType {
    BIKE, CAR, SUV, TRUCK, ELECTRIC_VEHICLE
}

class Ticket {
    private final String ticketId;
    private final String vehicleNumber;
    private final VehicleType vehicleType;
    private final LocalDateTime entryTime;
    private final boolean isVIP;
    private boolean isLost;

    public Ticket(String vehicleNumber, VehicleType vehicleType, boolean isVIP) {
        this.ticketId = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.entryTime = LocalDateTime.now();
        this.isVIP = isVIP;
        this.isLost = false;
    }

    // Constructor for testing specific times (Overnight, peak-hours, early exit, etc.)
    public Ticket(String vehicleNumber, VehicleType vehicleType, boolean isVIP, LocalDateTime entryTime) {
        this.ticketId = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.entryTime = entryTime;
        this.isVIP = isVIP;
        this.isLost = false;
    }

    public String getTicketId() { return ticketId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public VehicleType getVehicleType() { return vehicleType; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public boolean isVIP() { return isVIP; }
    public boolean isLost() { return isLost; }
    public void markLost() { this.isLost = true; }
}

class ParkingSlot {
    private final int slotId;
    private final VehicleType supportedType;
    private boolean isOccupied;

    public ParkingSlot(int slotId, VehicleType supportedType) {
        this.slotId = slotId;
        this.supportedType = supportedType;
        this.isOccupied = false;
    }

    public int getSlotId() { return slotId; }
    public VehicleType getSupportedType() { return supportedType; }
    public boolean isOccupied() { return isOccupied; }
    public void setOccupied(boolean occupied) { isOccupied = occupied; }
}

public class ParkingManagement {
    private final List<ParkingSlot> slots = new ArrayList<>();
    private final Map<String, ParkingSlot> activeOccupancy = new HashMap<>(); // VehicleNumber -> Slot
    private final Map<String, Ticket> activeTickets = new HashMap<>();       // TicketId -> Ticket

    // Base rates per hour
    private static final Map<VehicleType, Double> BASE_RATES = Map.of(
        VehicleType.BIKE, 10.0,
        VehicleType.CAR, 20.0,
        VehicleType.SUV, 30.0,
        VehicleType.TRUCK, 50.0,
        VehicleType.ELECTRIC_VEHICLE, 25.0
    );

    public ParkingManagement(int bikeSlots, int carSlots, int suvSlots, int truckSlots, int evSlots) {
        int id = 1;
        for (int i = 0; i < bikeSlots; i++) slots.add(new ParkingSlot(id++, VehicleType.BIKE));
        for (int i = 0; i < carSlots; i++) slots.add(new ParkingSlot(id++, VehicleType.CAR));
        for (int i = 0; i < suvSlots; i++) slots.add(new ParkingSlot(id++, VehicleType.SUV));
        for (int i = 0; i < truckSlots; i++) slots.add(new ParkingSlot(id++, VehicleType.TRUCK));
        for (int i = 0; i < evSlots; i++) slots.add(new ParkingSlot(id++, VehicleType.ELECTRIC_VEHICLE));
    }

    // Vehicle Entry: Automatically assigns an optimal matching slot
    public Ticket vehicleEntry(String vehicleNumber, VehicleType type, boolean isVIP) {
        return vehicleEntry(vehicleNumber, type, isVIP, LocalDateTime.now());
    }

    public Ticket vehicleEntry(String vehicleNumber, VehicleType type, boolean isVIP, LocalDateTime entryTime) {
        if (activeOccupancy.containsKey(vehicleNumber)) {
            System.out.println("Entry Denied: Vehicle " + vehicleNumber + " is already inside the parking lot.");
            return null;
        }

        ParkingSlot allocatedSlot = findAvailableSlot(type, isVIP);
        if (allocatedSlot == null) {
            System.out.println("Entry Denied: No available slot for " + type + (isVIP ? " (VIP Priority)" : "") + ".");
            return null;
        }

        allocatedSlot.setOccupied(true);
        activeOccupancy.put(vehicleNumber, allocatedSlot);
        Ticket ticket = new Ticket(vehicleNumber, type, isVIP, entryTime);
        activeTickets.put(ticket.getTicketId(), ticket);

        System.out.println("Vehicle " + vehicleNumber + " parked at Slot " + allocatedSlot.getSlotId() + " (" + type + ").");
        return ticket;
    }

    private ParkingSlot findAvailableSlot(VehicleType type, boolean isVIP) {
        // VIP handling strategy: Looks for the exact type match first.
        // Can be expanded to allow VIPs to claim any larger universal slot if needed.
        for (ParkingSlot slot : slots) {
            if (!slot.isOccupied() && slot.getSupportedType() == type) {
                return slot;
            }
        }
        return null;
    }

    // Vehicle Exit & Fee Calculation
    public double vehicleExit(String ticketId, LocalDateTime exitTime) {
        Ticket ticket = activeTickets.get(ticketId);
        if (ticket == null) {
            System.out.println("Exit Denied: Invalid Ticket ID.");
            return -1.0;
        }

        ParkingSlot slot = activeOccupancy.get(ticket.getVehicleNumber());
        if (slot == null || slot.getSupportedType() != ticket.getVehicleType()) {
            System.out.println("Error: Wrong vehicle-slot consistency check failed.");
            return -1.0;
        }

        double fee = calculateFee(ticket, exitTime);

        // Free up resources
        slot.setOccupied(false);
        activeOccupancy.remove(ticket.getVehicleNumber());
        activeTickets.remove(ticketId);

        System.out.printf("Vehicle %s cleared from Slot %d. Total Fee: $%.2f%n", 
                ticket.getVehicleNumber(), slot.getSlotId(), fee);
        return fee;
    }

    public double calculateFee(Ticket ticket, LocalDateTime exitTime) {
        if (ticket.isLost()) {
            return 100.0; // Lost ticket flat fee penalty
        }

        long minutes = Duration.between(ticket.getEntryTime(), exitTime).toMinutes();
        if (minutes <= 0) {
            minutes = 1; // Early exit / edge cases round up safely
        }
        
        double hours = Math.ceil(minutes / 60.0);
        double baseRate = BASE_RATES.get(ticket.getVehicleType());
        double totalFee = 0;

        LocalDateTime trackingTime = ticket.getEntryTime();
        for (int i = 0; i < hours; i++) {
            double hourlyRate = baseRate;
            int currentHour = trackingTime.getHour();

            // Peak-hour pricing factor: 8 AM - 11 AM & 5 PM - 8 PM (1.5x price increase)
            if ((currentHour >= 8 && currentHour < 11) || (currentHour >= 17 && currentHour < 20)) {
                hourlyRate *= 1.5;
            }

            // EV Charging addition ($5 flat extra per hour while occupying the slot)
            if (ticket.getVehicleType() == VehicleType.ELECTRIC_VEHICLE) {
                hourlyRate += 5.0;
            }

            totalFee += hourlyRate;
            trackingTime = trackingTime.plusHours(1);
        }

        // VIP Discount handling (20% Off total calculated parking stay)
        if (ticket.isVIP()) {
            totalFee *= 0.80;
        }

        return totalFee;
    }

    public void triggerLostTicket(String ticketId) {
        Ticket ticket = activeTickets.get(ticketId);
        if (ticket != null) {
            ticket.markLost();
            System.out.println("Ticket " + ticketId + " flagged as LOST.");
        }
    }
}
