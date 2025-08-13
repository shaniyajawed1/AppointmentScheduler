package com.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final DateTimeFormatter INPUT_DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter INPUT_D_FMT  = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        Scheduler scheduler = new Scheduler("appointments.csv");
        Scanner sc = new Scanner(System.in);

        while (true) {
            printMenu();
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1": addFlow(sc, scheduler); break;
                case "2": viewAllFlow(scheduler); break;
                case "3": searchByDateFlow(sc, scheduler); break;
                case "4": editFlow(sc, scheduler); break;
                case "5": deleteFlow(sc, scheduler); break;
                case "6":
                    System.out.println("Goodbye!");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n=== Appointment Scheduler ===");
        System.out.println("1) Add Appointment");
        System.out.println("2) View All Appointments");
        System.out.println("3) Search Appointments by Date");
        System.out.println("4) Edit Appointment");
        System.out.println("5) Delete Appointment");
        System.out.println("6) Exit");
        System.out.print("Choose: ");
    }

    private static void addFlow(Scanner sc, Scheduler scheduler) {
        System.out.print("Client name: ");
        String name = sc.nextLine().trim();
        if (name.isBlank()) { System.out.println("Client name is required."); return; }

        System.out.print("Date & time (yyyy-MM-dd HH:mm): ");
        String dtStr = sc.nextLine().trim();
        LocalDateTime dt;
        try { dt = LocalDateTime.parse(dtStr, INPUT_DT_FMT); }
        catch (Exception e) { System.out.println("Invalid date/time format."); return; }

        System.out.print("Notes (optional): ");
        String notes = sc.nextLine();

        boolean ok = scheduler.addAppointment(name, dt, notes);
        if (ok) System.out.println("✅ Appointment added.");
        else    System.out.println("❌ That time slot is already booked.");
    }

    private static void viewAllFlow(Scheduler scheduler) {
        List<Appointment> all = scheduler.listAll();
        if (all.isEmpty()) { System.out.println("No appointments."); return; }
        printTable(all);
    }

    private static void searchByDateFlow(Scanner sc, Scheduler scheduler) {
        System.out.print("Date (yyyy-MM-dd): ");
        String dStr = sc.nextLine().trim();
        LocalDate date;
        try { date = LocalDate.parse(dStr, INPUT_D_FMT); }
        catch (Exception e) { System.out.println("Invalid date format."); return; }

        List<Appointment> list = scheduler.findByDate(date);
        if (list.isEmpty()) System.out.println("No appointments on " + date + ".");
        else printTable(list);
    }

    private static void editFlow(Scanner sc, Scheduler scheduler) {
        List<Appointment> all = scheduler.listAll();
        if (all.isEmpty()) { System.out.println("No appointments to edit."); return; }
        printTable(all);

        System.out.print("Enter Appointment ID (full or first 8 chars): ");
        String idOrShort = sc.nextLine().trim();

        // Find full ID by short prefix fallback
        String fullId = null;
        for (Appointment a : all) {
            if (a.getId().equals(idOrShort) || a.shortId().equals(idOrShort)) {
                fullId = a.getId(); break;
            }
        }
        if (fullId == null) { System.out.println("ID not found."); return; }

        System.out.print("New client name (blank to keep): ");
        String newName = sc.nextLine().trim();
        if (newName.isBlank()) newName = null;

        System.out.print("New date & time yyyy-MM-dd HH:mm (blank to keep): ");
        String newDtStr = sc.nextLine().trim();
        LocalDateTime newDt = null;
        if (!newDtStr.isBlank()) {
            try { newDt = LocalDateTime.parse(newDtStr, INPUT_DT_FMT); }
            catch (Exception e) { System.out.println("Invalid date/time format."); return; }
        }

        System.out.print("New notes (blank to keep): ");
        String newNotes = sc.nextLine();
        if (newNotes.isBlank()) newNotes = null;

        boolean ok = scheduler.editAppointment(fullId, newName, newDt, newNotes);
        if (ok) System.out.println("✅ Updated.");
        else    System.out.println("❌ Update failed (maybe time slot conflict).");
    }

    private static void deleteFlow(Scanner sc, Scheduler scheduler) {
        List<Appointment> all = scheduler.listAll();
        if (all.isEmpty()) { System.out.println("No appointments to delete."); return; }
        printTable(all);

        System.out.print("Enter Appointment ID (full or first 8 chars): ");
        String idOrShort = sc.nextLine().trim();

        String fullId = null;
        for (Appointment a : all) {
            if (a.getId().equals(idOrShort) || a.shortId().equals(idOrShort)) {
                fullId = a.getId(); break;
            }
        }
        if (fullId == null) { System.out.println("ID not found."); return; }

        System.out.print("Are you sure you want to delete? (y/N): ");
        String ans = sc.nextLine().trim().toLowerCase();
        if (!ans.equals("y")) { System.out.println("Cancelled."); return; }

        boolean ok = scheduler.deleteAppointment(fullId);
        if (ok) System.out.println("🗑️ Deleted.");
        else    System.out.println("❌ Delete failed.");
    }

    private static void printTable(List<Appointment> list) {
        System.out.printf("%-10s %-22s %-17s %s%n", "ID", "Client", "DateTime", "Notes");
        System.out.println("---------------------------------------------------------------------");
        for (Appointment a : list) {
            String dt = a.getDateTime().format(INPUT_DT_FMT);
            String notes = a.getNotes() == null ? "" : a.getNotes();
            if (notes.length() > 40) notes = notes.substring(0, 37) + "...";
            System.out.printf("%-10s %-22s %-17s %s%n", a.shortId(), a.getClientName(), dt, notes);
        }
    }
}

