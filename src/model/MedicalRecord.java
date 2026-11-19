package model;

// abstract base class for all clinical documents like prescriptions and lab reports
public abstract class MedicalRecord {
    protected String recordId;
    protected String date;
    protected String patientId;

    public MedicalRecord(String recordId, String date, String patientId) {
        this.recordId = recordId;
        this.date = date;
        this.patientId = patientId;
    }

    // every record type has to say what it contains in its own way
    public abstract String getSummary();

    public String getRecordId() {
        return recordId;
    }

    public String getDate() {
        return date;
    }

    public String getPatientId() {
        return patientId;
    }
}
