package model;

// a prescription given by a doctor with medicine name, dosage and duration
public class Prescription extends MedicalRecord {
    private String medicineName;
    private String dosage;
    private String duration;

    public Prescription(String recordId, String date, String patientId, String medicineName, String dosage, String duration) {
        super(recordId, date, patientId);
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.duration = duration;
    }

    @Override
    public String getSummary() {
        return "Prescription: " + medicineName + " (" + dosage + ") for " + duration;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public String getDuration() {
        return duration;
    }
}
