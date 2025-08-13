package com.scheduler;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class Scheduler {
    private final List<Appointment> appointments = new ArrayList<>();
    private final File file;

    public Scheduler(String fileName) {
        this.file = new File(fileName);
        loadAppointments();
    }

    // Add with double-booking prevention
    public boolean addAppointment(String clientName, LocalDateTime dateTime, String notes) {
        if (isSlotTaken(dateTime)) return false;
        appointments.add(Appointment.create(clientName, dateTime, notes));
        saveAppointments();
        return true;
    }

    // Edit by ID (null means "no change")
    public boolean editAppointment(String id, String newClientName, LocalDateTime newDateTime, String newNotes) {
        Appointment a = findById(id);
        if (a == null) return false;

        // If time changes, ensure no conflict with others
        if (newDateTime != null && !a.getDateTime().equals(newDateTime)) {
            if (isSlotTakenExcluding(newDateTime, a.getId())) return false;
            a.setDateTime(newDateTime);
        }
        if (newClientName != null && !newClientName.isBlank()) a.setClientName(newClientName);
        if (newNotes != null) a.setNotes(newNotes);

        saveAppointments();
        return true;
    }

    public boolean deleteAppointment(String id) {
        Iterator<Appointment> it = appointments.iterator();
        boolean removed = false;
        while (it.hasNext()) {
            if (it.next().getId().equals(id)) {
                it.remove();
                removed = true;
                break;
            }
        }
        if (removed) saveAppointments();
        return removed;
    }

    public List<Appointment> listAll() {
        List<Appointment> copy = new ArrayList<>(appointments);
        copy.sort(Comparator.comparing(Appointment::getDateTime));
        return copy;
    }

    public List<Appointment> findByDate(LocalDate date) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment a : appointments) {
            if (a.getDateTime().toLocalDate().equals(date)) result.add(a);
        }
        result.sort(Comparator.comparing(Appointment::getDateTime));
        return result;
    }

    private boolean isSlotTaken(LocalDateTime dt) {
        for (Appointment a : appointments) {
            if (a.getDateTime().equals(dt)) return true;
        }
        return false;
    }

    private boolean isSlotTakenExcluding(LocalDateTime dt, String excludeId) {
        for (Appointment a : appointments) {
            if (!a.getId().equals(excludeId) && a.getDateTime().equals(dt)) return true;
        }
        return false;
    }

    private Appointment findById(String id) {
        for (Appointment a : appointments) {
            if (a.getId().equals(id)) return a;
        }
        return null;
    }

    private void saveAppointments() {
        // overwrite all to keep it simple & consistent
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Appointment a : listAll()) {
                bw.write(a.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving appointments: " + e.getMessage());
        }
    }

    private void loadAppointments() {
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    appointments.add(Appointment.fromFileString(line));
                } catch (Exception ignored) {
                    System.out.println("Skipping bad line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading appointments: " + e.getMessage());
        }
    }
}
