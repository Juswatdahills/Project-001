

import java.util.Scanner;

public class Main {

    // ===== Constants =====
    static final int MAX_ROOMS = 10;
    static final int MAX_HISTORY = 100;
    static final double LATE_PENALTY_RATE = 0.05; // 5% of monthly rent
    static final String ADMIN_USER = "admin";
    static final String ADMIN_PASS = "1234";

    static Scanner sc = new Scanner(System.in);

    // ===== Room data (parallel arrays, index 0 = Room 1) =====
    static String[] roomKind = new String[MAX_ROOMS];   // "Solo" or "Bed Spacer"
    static boolean[] aircon = new boolean[MAX_ROOMS];
    static double[] rent = new double[MAX_ROOMS];

    // ===== Tenant data (null name = vacant) =====
    static String[] tenantName = new String[MAX_ROOMS];
    static String[] moveIn = new String[MAX_ROOMS];
    static double[] deposit = new double[MAX_ROOMS];
    static double[] balance = new double[MAX_ROOMS];

    // ===== Past tenants log =====
    static String[] history = new String[MAX_HISTORY];
    static int historyCount = 0;

    // ================================================================
    public static void main(String[] args) {
        setupRooms();

        if (!login()) {
            System.out.println("Too many failed attempts. Program closed.");
            return;
        }

        int choice;
        do {
            System.out.println("\n===== BOARDING HOUSE SYSTEM =====");
            System.out.println("1. Room Status Indicator");
            System.out.println("2. Tenant Management");
            System.out.println("3. Rent & Payment");
            System.out.println("4. Room Tiers Inventory");
            System.out.println("0. Exit");
            choice = readInt("Choose: ", 0, 4);

            switch (choice) {
                case 1: showRoomStatus(); break;
                case 2: tenantMenu(); break;
                case 3: paymentMenu(); break;
                case 4: showTiers(); break;
                case 0: System.out.println("Goodbye!"); break;
            }
        } while (choice != 0);
    }

    // ================================================================
    // SETUP
    // ================================================================
    static void setupRooms() {
        for (int i = 0; i < MAX_ROOMS; i++) {
            if (i < 3) {            // Rooms 1-3: Solo Aircon
                roomKind[i] = "Solo";       aircon[i] = true;   rent[i] = 4500.00;
            } else if (i < 5) {     // Rooms 4-5: Solo Non-Aircon
                roomKind[i] = "Solo";       aircon[i] = false;  rent[i] = 3000.00;
            } else if (i < 😎 {     // Rooms 6-8: Bed Spacer Aircon
                roomKind[i] = "Bed Spacer"; aircon[i] = true;   rent[i] = 2500.00;
            } else {                // Rooms 9-10: Bed Spacer Non-Aircon
                roomKind[i] = "Bed Spacer"; aircon[i] = false;  rent[i] = 1500.00;
            }
        }
    }

    // ================================================================
    // MODULE 1: LOGIN
    // ================================================================
    static boolean login() {
        int attempts = 0;
        System.out.println("===== ADMIN LOGIN =====");
        while (attempts < 3) {
            System.out.print("Username: ");
            String user = sc.nextLine().trim();
            System.out.print("Password: ");
            String pass = sc.nextLine().trim();

            if (user.equals(ADMIN_USER) && pass.equals(ADMIN_PASS)) {
                System.out.println("Login successful.");
                return true;
            }
            attempts++;
            System.out.println("Wrong username or password. Attempts left: " + (3 - attempts));
        }
        return false;
    }

    // ================================================================
    // MODULE 2: ROOM STATUS INDICATOR
    // ================================================================
    static void showRoomStatus() {
        System.out.println("\n--- ROOM STATUS ---");
        int vacant = 0;
        for (int i = 0; i < MAX_ROOMS; i++) {
            String status;
            if (tenantName[i] == null) {
                status = "VACANT (ready for next tenant)";
                vacant++;
            } else {
                status = "OCCUPIED by " + tenantName[i];
            }
            System.out.printf("Room %-2d | %-10s | %-3s | PHP %8.2f | %s%n",
                    i + 1, roomKind[i], aircon[i] ? "A/C" : "Fan", rent[i], status);
        }
        System.out.println("Vacant rooms: " + vacant + " / " + MAX_ROOMS);
    }

    // ================================================================
    // MODULE 3: TENANT MANAGEMENT (CRUD) + HISTORY
    // ================================================================
    static void tenantMenu() {
        int choice;
        do {
            System.out.println("\n--- TENANT MANAGEMENT ---");
            System.out.println("1. Add tenant (Create)");
            System.out.println("2. View tenants (Read)");
            System.out.println("3. Edit tenant (Update)");
            System.out.println("4. Move out tenant (Delete)");
            System.out.println("5. Tenant history");
            System.out.println("0. Back");
            choice = readInt("Choose: ", 0, 5);

            switch (choice) {
                case 1: addTenant(); break;
                case 2: viewTenants(); break;
                case 3: updateTenant(); break;
                case 4: moveOutTenant(); break;
                case 5: showHistory(); break;
            }
        } while (choice != 0);
    }

    static void addTenant() {
        showRoomStatus();
        int r = readInt("Room number to assign: ", 1, MAX_ROOMS) - 1;
        if (tenantName[r] != null) {
            System.out.println("Room " + (r + 1) + " is already occupied.");
            return;
        }
        String name = readText("Tenant name: ");
        String date = readText("Move-in date (e.g. Nov 13 2026): ");
        double dep = readDouble("Security deposit: ", 0);

        tenantName[r] = name;
        moveIn[r] = date;
        deposit[r] = dep;
        balance[r] = rent[r];   // first month's rent is due on move-in

        System.out.println(name + " added to Room " + (r + 1)
                + ". First month rent due: PHP " + rent[r]);
    }

    static void viewTenants() {
        System.out.println("\n--- ACTIVE TENANTS ---");
        boolean found = false;
        for (int i = 0; i < MAX_ROOMS; i++) {
            if (tenantName[i] != null) {
                System.out.printf("Room %-2d | %-15s | In: %-12s | Deposit: %8.2f | Balance: %8.2f%n",
                        i + 1, tenantName[i], moveIn[i], deposit[i], balance[i]);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No active tenants.");
        }
    }

    static void updateTenant() {
        int r = readInt("Room number to edit: ", 1, MAX_ROOMS) - 1;
        if (tenantName[r] == null) {
            System.out.println("Room " + (r + 1) + " is vacant. Nothing to edit.");
            return;
        }
        System.out.println("Current name: " + tenantName[r] + " | Move-in: " + moveIn[r]);
        System.out.print("New name (press Enter to keep): ");
        String newName = sc.nextLine().trim();
        System.out.print("New move-in date (press Enter to keep): ");
        String newDate = sc.nextLine().trim();

        if (!newName.isEmpty()) {
            tenantName[r] = newName;
        }
        if (!newDate.isEmpty()) {
            moveIn[r] = newDate;
        }
        System.out.println("Tenant record updated.");
    }

    static void moveOutTenant() {
        int r = readInt("Room number to move out: ", 1, MAX_ROOMS) - 1;
        if (tenantName[r] == null) {
            System.out.println("Room " + (r + 1) + " is already vacant.");
            return;
        }
        if (balance[r] > 0) {
            System.out.println("Warning: unpaid balance of PHP " + balance[r]);
        }
        System.out.print("Confirm move out of " + tenantName[r] + "? (y/n): ");
        String confirm = sc.nextLine().trim();
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Cancelled.");
            return;
        }

        String date = readText("Move-out date: ");

        // String concatenation builds the history log entry
        if (historyCount < MAX_HISTORY) {
            history[historyCount] = tenantName[r] + " | Room " + (r + 1)
                    + " | In: " + moveIn[r] + " | Out: " + date + " | Status: Moved out";
            historyCount++;
        }

        System.out.println(tenantName[r] + " moved out. Deposit left: PHP " + deposit[r]);

        // Delete the record from the active arrays
        tenantName[r] = null;
        moveIn[r] = null;
        deposit[r] = 0;
        balance[r] = 0;
    }

    static void showHistory() {
        System.out.println("\n--- TENANT HISTORY ---");
        boolean any = false;

        for (int i = 0; i < MAX_ROOMS; i++) {
            if (tenantName[i] != null) {
                System.out.println(tenantName[i] + " | Room " + (i + 1)
                        + " | In: " + moveIn[i] + " | Status: Active");
                any = true;
            }
        }
        for (int i = 0; i < historyCount; i++) {
            System.out.println(history[i]);
            any = true;
        }
        if (!any) {
            System.out.println("No records yet.");
        }
    }

    // ================================================================
    // MODULE 4: RENT & PAYMENT
    // ================================================================
    static void paymentMenu() {
        int choice;
        do {
            System.out.println("\n--- RENT & PAYMENT ---");
            System.out.println("1. Bill monthly rent (all tenants)");
            System.out.println("2. Record payment");
            System.out.println("3. Add late penalty");
            System.out.println("4. View balances");
            System.out.println("0. Back");
            choice = readInt("Choose: ", 0, 4);

            switch (choice) {
                case 1: billMonthlyRent(); break;
                case 2: recordPayment(); break;
                case 3: addLatePenalty(); break;
                case 4: viewTenants(); break;
            }
        } while (choice != 0);
    }

    static void billMonthlyRent() {
        int billed = 0;
        for (int i = 0; i < MAX_ROOMS; i++) {
            if (tenantName[i] != null) {
                balance[i] += rent[i];
                billed++;
            }
        }
        System.out.println("Monthly rent added to " + billed + " tenant(s).");
    }

    static void recordPayment() {
        int r = readInt("Room number: ", 1, MAX_ROOMS) - 1;
        if (tenantName[r] == null) {
            System.out.println("Room " + (r + 1) + " is vacant.");
            return;
        }
        System.out.printf("%s | Balance: PHP %.2f | Deposit: PHP %.2f%n",
                tenantName[r], balance[r], deposit[r]);

        double paid = readDouble("Amount paid: ", 0);
        double depositUsed = readDouble("Deposit used: ", 0);

        if (depositUsed > deposit[r]) {
            System.out.println("Deposit used is more than the deposit available.");
            return;
        }
        if (paid + depositUsed > balance[r]) {
            System.out.println("Payment is more than the balance. Cancelled.");
            return;
        }

        balance[r] -= (paid + depositUsed);
        deposit[r] -= depositUsed;

        if (balance[r] == 0) {
            System.out.println("Status: FULLY PAID");
        } else {
            System.out.printf("Status: PARTIAL. Remaining balance: PHP %.2f%n", balance[r]);
        }
    }

    static void addLatePenalty() {
        int r = readInt("Room number: ", 1, MAX_ROOMS) - 1;
        if (tenantName[r] == null) {
            System.out.println("Room " + (r + 1) + " is vacant.");
            return;
        }
        double penalty = rent[r] * LATE_PENALTY_RATE;
        balance[r] += penalty;
        System.out.printf("Late penalty of PHP %.2f added. New balance: PHP %.2f%n",
                penalty, balance[r]);
    }

    // ================================================================
    // MODULE 5: ROOM TIERS INVENTORY
    // ================================================================
    static void showTiers() {
        String[] kinds = {"Solo", "Solo", "Bed Spacer", "Bed Spacer"};
        boolean[] acs = {true, false, true, false};

        System.out.println("\n--- ROOM TIERS INVENTORY ---");
        for (int t = 0; t < kinds.length; t++) {
            int total = 0;
            int available = 0;
            for (int i = 0; i < MAX_ROOMS; i++) {
                if (roomKind[i].equals(kinds[t]) && aircon[i] == acs[t]) {
                    total++;
                    if (tenantName[i] == null) {
                        available++;
                    }
                }
            }
            System.out.printf("Type: %-10s | Aircon: %-3s | Available: %d of %d%n",
                    kinds[t], acs[t] ? "Yes" : "No", available, total);
        }
    }

    // ================================================================
    // INPUT HELPERS (handle invalid input)
    // ================================================================
    static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Enter a number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Numbers only.");
            }
        }
    }

    static double readDouble(String prompt, double min) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                double value = Double.parseDouble(line);
                if (value >= min) {
                    return value;
                }
                System.out.println("Value must be at least " + min + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a number.");
            }
        }
    }

    static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("This cannot be empty.");
        }
    }
}
