package com.hardwarehub.seed;

/**
 * Raw shape of one row in seed-data.json.
 * purchaseDate and status are kept as String on purpose: the seed data is known
 * to contain a malformed date and an invalid status value, and this class must
 * be able to deserialize every row without throwing. Normalizing those fields
 * into real types happens later, in code that logs each correction.
 */
public class SeedRecord {

    private Long id;
    private String name;
    private String brand;
    private String purchaseDate;
    private String status;
    private String notes;
    private String assignedTo;
    private String history;

    public SeedRecord() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }
}
