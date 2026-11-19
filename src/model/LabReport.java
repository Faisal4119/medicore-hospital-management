package model;

// a lab test report for a patient
public class LabReport extends MedicalRecord {
    private String testName;
    private String result;
    private String referenceRange;

    public LabReport(String recordId, String date, String patientId, String testName, String result, String referenceRange) {
        super(recordId, date, patientId);
        this.testName = testName;
        this.result = result;
        this.referenceRange = referenceRange;
    }

    @Override
    public String getSummary() {
        return "Lab Report: " + testName + " = " + result + " (normal range: " + referenceRange + ")";
    }

    public String getTestName() {
        return testName;
    }

    public String getResult() {
        return result;
    }

    public String getReferenceRange() {
        return referenceRange;
    }
}
