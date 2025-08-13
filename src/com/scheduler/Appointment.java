package com.scheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Appointment {
    private final String id; // UUID string
    private String clientName;
    private LocalDateTime dateTime;
    private String notes;

    private static final DateTimeFormatter FILE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public Appointment(String id, String clientName, LocalDateTime dateTime, String notes) {
        this.id = id;
        this.clientName = clientName;
        this.dateTime = dateTime;
        this.notes = notes == null ? "" : notes;
    }

    public static Appointment create(String clientName, LocalDateTime dateTime, String notes) {
        return new Appointment(java.util.UUID.randomUUID().toString(), clientName, dateTime, notes);
    }

    public String getId() { return id; }
    public String getClientName() { return clientName; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getNotes() { return notes; }

    public void setClientName(String clientName) { this.clientName = clientName; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public void setNotes(String notes) { this.notes = notes == null ? "" : notes; }

    public String shortId() {
        return id.length() >= 8 ? id.substring(0, 8) : id;
    }

    public String toFileString() {
        // sanitize semicolons to keep CSV-ish format stable
        String safeName = clientName.replace(";", ",");
        String safeNotes = notes.replace(";", ",");
        return id + ";" + safeName + ";" + dateTime.format(FILE_FMT) + ";" + safeNotes;
    }

    public static Appointment fromFileString(String line) {
        String[] parts = line.split(";", -1); // keep empty notes
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid line: " + line);
        }
        String id = parts[0];
        String name = parts[1];
        LocalDateTime dt = LocalDateTime.parse(parts[2], FILE_FMT);
        String notes = parts[3];
        return new Appointment(id, name, dt, notes);
    }

    @Override
    public String toString() {
        return shortId() + " | " + clientName + " | " + dateTime.format(DISPLAY_FMT) + (notes.isBlank() ? "" : " | " + notes);
    }
}
