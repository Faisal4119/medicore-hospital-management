# MediCore – Full Code Explanation (File by File)

This explains **every file** inside `src`, with the actual code and what each part does, and **why the file exists** in the project. Use it side-by-side with the code.

**How the whole thing connects (read this first):**

```
main/Main.java          → starts the app, loads files, opens login screen
exceptions/             → 2 custom error types the app throws on bad input
interfaces/             → 2 contracts (Billable, Schedulable) that classes promise to follow
model/                  → the "things" of the hospital: people, appointments, records
storage/                → the "engine": keeps all lists, does login, reads/writes csv files
gui/                    → 6 JavaFX screens the user actually sees
```

---

# 1. Package `exceptions`

Java's built-in exceptions (like `NullPointerException`) don't say anything hospital-specific. We make our own exception classes so an error message like *"No patient found with ID: P99"* travels from the logic code up to the GUI, where it's shown in a popup. Because they extend `Exception` (checked), the compiler **forces** every caller to catch them — the app can never crash silently from a bad ID or double booking.

## 1.1 `InvalidPatientIDException.java`

```java
package exceptions;

// custom exception for when a patient id is wrong or not found
public class InvalidPatientIDException extends Exception {

    public InvalidPatientIDException(String message) {
        super(message);
    }
}
```

**What each part does:**
- `extends Exception` → makes this a *checked* exception. Any method that throws it must write `throws InvalidPatientIDException`, and callers must use try/catch.
- The constructor takes a message and passes it to `super(message)` — the parent `Exception` class stores it, so later `e.getMessage()` returns it in the GUI popup.

**Why it is here:** patient lookup is the most common operation (appointments, records, billing all need it). One dedicated exception means *every* screen handles a wrong/missing patient ID the same way. It is thrown in exactly one place: `HospitalManager.findPatient()` — if the ID is empty or no patient matches.

**Where it's caught:** `AppointmentView` (booking), `RecordView` (show/add records), `BillingView` (show bill) — each shows the message in an `Alert`.

## 1.2 `AppointmentConflictException.java`

```java
package exceptions;

// custom exception for when an appointment time slot is already taken
public class AppointmentConflictException extends Exception {

    public AppointmentConflictException(String message) {
        super(message);
    }
}
```

**What it does:** exactly the same structure as above — only the *meaning* is different. Two different classes exist because a wrong ID and a time clash are different problems needing different messages.

**Why it is here:** double booking is the main business rule of the system. It's thrown in **two** places:
1. `Doctor.book()` — the *doctor* already has this time slot taken.
2. `HospitalManager.bookAppointment()` — the *patient* already has an appointment at this time.

**Where it's caught:** `AppointmentView`'s Book button — shows the conflict message instead of booking.

---

# 2. Package `interfaces`

An interface is a **contract**: a list of methods with no bodies. A class that `implements` an interface promises to provide those methods. We use interfaces (instead of putting these methods in `Person`) because the abilities cut across the family tree: an `Appointment` is not a person but is billable and schedulable; a `Doctor` is a person but is not billable.

## 2.1 `Billable.java`

```java
package interfaces;

// interface for anything that generates a cost in the hospital
public interface Billable {
    double calculateBill();
}
```

**What it does:** one method, no body. Any class implementing it must write its own `calculateBill()` that returns a `double`.

**Why it is here:** the billing screen shouldn't care *what* it's billing — it just calls `calculateBill()`. Implemented by:
- `Patient` → total of all its unpaid, non-cancelled appointments.
- `Appointment` → its own fee (0 if cancelled).

## 2.2 `Schedulable.java`

```java
package interfaces;

import exceptions.AppointmentConflictException;

// interface for booking, cancelling and rescheduling time slots
public interface Schedulable {
    void book(String timeSlot) throws AppointmentConflictException;

    void cancel(String timeSlot);

    void reschedule(String oldSlot, String newSlot) throws AppointmentConflictException;
}
```

**What each part does:**
- `book` and `reschedule` declare `throws AppointmentConflictException` → the interface itself says "booking might fail with a conflict". Implementing classes are *allowed* (not forced) to throw it.
- `cancel` never conflicts, so no throws clause.

**Why it is here:** anything with time slots follows one contract. Implemented by:
- `Doctor` → keeps a list of taken slots, does the real conflict checking.
- `Appointment` → updates its own slot/status when booked/cancelled/rescheduled.

---

# 3. Package `model`

The "things" of the hospital. Two inheritance trees live here: **people** (`Person` → Patient/Doctor/Staff) and **documents** (`MedicalRecord` → Prescription/LabReport), plus `Appointment` (connects a patient to a doctor) and `AppointmentStatus` (enum).

## 3.1 `Person.java` (abstract)

```java
package model;

// abstract superclass for all people in the hospital (patients, doctors, staff)
public abstract class Person {
    protected String id;
    protected String name;
    protected int age;
    protected String phone;

    public Person(String id, String name, int age, String phone) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.phone = phone;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
```

**What each part does:**
- `abstract class` → you can never write `new Person(...)`. A person in a hospital is always a specific kind — patient, doctor, or staff.
- `protected` fields → subclasses can use `name`, `id` etc. directly (e.g. `Doctor.book()` uses `name` in its error message), but outside classes must use getters.
- The constructor is called by subclasses via `super(id, name, age, phone)`.
- Getters/setters = **encapsulation**: fields can't be modified randomly from outside; they go through methods.

**Why it is here:** id/name/age/phone would otherwise be copy-pasted into 3 classes. Writing them once in a superclass is the whole point of **inheritance**.

## 3.2 `Patient.java` (extends Person, implements Billable)

```java
public class Patient extends Person implements Billable {
    private String disease;
    private ArrayList<MedicalRecord> records;
    private ArrayList<Appointment> appointments;

    public Patient(String id, String name, int age, String phone, String disease) {
        super(id, name, age, phone);
        this.disease = disease;
        this.records = new ArrayList<MedicalRecord>();
        this.appointments = new ArrayList<Appointment>();
    }

    // total bill = sum of all unpaid appointments of this patient
    @Override
    public double calculateBill() {
        double total = 0;
        for (int i = 0; i < appointments.size(); i++) {
            Appointment a = appointments.get(i);
            if (!a.isPaid() && a.getStatus() != AppointmentStatus.CANCELLED) {
                total = total + a.calculateBill();
            }
        }
        return total;
    }

    public void addRecord(MedicalRecord record) { records.add(record); }
    public void addAppointment(Appointment a) { appointments.add(a); }
    // ... getters for records, appointments, disease
}
```

**What each part does:**
- `super(...)` in the constructor → hands the shared fields to `Person`; then sets its own `disease` and creates two empty lists.
- `ArrayList<MedicalRecord> records` → holds **both** prescriptions and lab reports in one list, because both *are* `MedicalRecord`s (polymorphism).
- `calculateBill()` → loops the patient's appointments, skips paid ones and cancelled ones, sums the rest. `@Override` marks it as the `Billable` method.
- `addRecord`/`addAppointment` → called by `HospitalManager` and `FileManager` to attach things to this patient.

**Why it is here:** the central "customer" object. Records and appointments hang off the patient so that RecordView/BillingView can ask *one object* for everything about a person.

## 3.3 `Doctor.java` (extends Person, implements Schedulable)

```java
public class Doctor extends Person implements Schedulable {
    private String specialization;
    private double consultationFee;
    private ArrayList<String> timeSlots; // slots that are already booked

    // book a slot for this doctor, throws exception if already taken
    @Override
    public void book(String timeSlot) throws AppointmentConflictException {
        for (int i = 0; i < timeSlots.size(); i++) {
            if (timeSlots.get(i).equals(timeSlot)) {
                throw new AppointmentConflictException("Dr. " + name + " already has an appointment at " + timeSlot);
            }
        }
        timeSlots.add(timeSlot);
    }

    @Override
    public void cancel(String timeSlot) {
        timeSlots.remove(timeSlot);
    }

    @Override
    public void reschedule(String oldSlot, String newSlot) throws AppointmentConflictException {
        // first check the new slot is free, then move
        for (int i = 0; i < timeSlots.size(); i++) {
            if (timeSlots.get(i).equals(newSlot)) {
                throw new AppointmentConflictException("Dr. " + name + " already has an appointment at " + newSlot);
            }
        }
        timeSlots.remove(oldSlot);
        timeSlots.add(newSlot);
    }
    // ... getters/setters for specialization, consultationFee, timeSlots
}
```

**What each part does:**
- `timeSlots` holds strings like `"2026-08-22 10:00 AM"` — every slot in this list is **taken**.
- `book()` → this is the doctor-side **conflict check**: loop the list, if the slot is already there → throw; otherwise add it. Note it uses `name` directly (allowed, because `name` is `protected` in `Person`).
- `cancel()` → removes the slot from the list, so someone else can book it.
- `reschedule()` → checks the *new* slot first (so we never lose the old one if the new is busy), then swaps.
- `consultationFee` → copied into every new appointment as its fee.

**Why it is here:** the doctor "owns" their calendar. Keeping the taken-slot list inside `Doctor` means the conflict rule lives with the data it protects.

## 3.4 `Staff.java` (extends Person)

```java
public class Staff extends Person {
    private String role;
    private double salary;

    public Staff(String id, String name, int age, String phone, String role, double salary) {
        super(id, name, age, phone);
        this.role = role;
        this.salary = salary;
    }
    // ... getters/setters
}
```

**What it does:** the simplest subclass — just adds `role` (e.g. "Nurse") and `salary` on top of `Person`.

**Why it is here:** the proposal requires non-doctor employees in the inheritance tree. `FileManager` loads/saves `staff.csv`, and `StaffView` shows the list on screen. Staff data comes directly from the `data/staff.csv` file (there is no registration form for staff — the file was filled by hand). Note: deleting the `data` folder deletes the staff list too; doctors reseed automatically, staff won't.

## 3.5 `AppointmentStatus.java` (enum)

```java
package model;

// status of an appointment
public enum AppointmentStatus {
    SCHEDULED, COMPLETED, CANCELLED
}
```

**What it does:** defines the only three legal states of an appointment.

**Why it is here (instead of a String):** with a String, a typo like `"CANCELED"` compiles fine and breaks logic silently. With an enum, `AppointmentStatus.CANCELED` is a **compile error**. Comparisons use `==` safely (`a.getStatus() != AppointmentStatus.CANCELLED`). For file saving it converts both ways: `toString()` writes `"SCHEDULED"`, `AppointmentStatus.valueOf("SCHEDULED")` reads it back.

## 3.6 `Appointment.java` (implements Schedulable AND Billable)

```java
public class Appointment implements Schedulable, Billable {
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String timeSlot;
    private AppointmentStatus status;
    private double fee;
    private boolean paid;

    public Appointment(String appointmentId, String patientId, String doctorId, String timeSlot, double fee) {
        // ...
        this.status = AppointmentStatus.SCHEDULED;
        this.paid = false;
    }

    @Override
    public double calculateBill() {
        // cancelled appointments dont cost anything
        if (status == AppointmentStatus.CANCELLED) {
            return 0;
        }
        return fee;
    }

    @Override
    public void book(String timeSlot) throws AppointmentConflictException {
        this.timeSlot = timeSlot;
        this.status = AppointmentStatus.SCHEDULED;
    }

    @Override
    public void cancel(String timeSlot) {
        this.status = AppointmentStatus.CANCELLED;
    }

    @Override
    public void reschedule(String oldSlot, String newSlot) throws AppointmentConflictException {
        this.timeSlot = newSlot;
        this.status = AppointmentStatus.SCHEDULED;
    }
    // ... getters, setStatus, setPaid, isPaid
}
```

**What each part does:**
- Stores `patientId`/`doctorId` as **IDs, not object references** → makes saving to a csv file trivial (write the string), and loading simple (look the objects up again by ID).
- Constructor always starts `SCHEDULED` + unpaid — a new booking is by definition active and not yet paid.
- `calculateBill()` (Billable) → the fee, but 0 if cancelled.
- `book/cancel/reschedule` (Schedulable) → update **this appointment's own state** (e.g. `cancel` flips status to `CANCELLED`). The conflict *checking* is NOT here — that's `Doctor`'s and `HospitalManager`'s job. This class only records what happened to itself.

**Why it is here:** it's the link object of the whole system — one row = one visit = one bill. Implementing **two interfaces at once** is also a required showcase from the proposal.

## 3.7 `MedicalRecord.java` (abstract)

```java
public abstract class MedicalRecord {
    protected String recordId;
    protected String date;
    protected String patientId;

    public MedicalRecord(String recordId, String date, String patientId) { /* ... */ }

    // every record type has to say what it contains in its own way
    public abstract String getSummary();

    // ... getters
}
```

**What each part does:**
- Common fields every clinical document has: `recordId`, `date`, `patientId`.
- `public abstract String getSummary();` → **no body**. Every subclass is *forced* by the compiler to write its own version.

**Why it is here:** so `RecordView` and `Patient` can hold prescriptions and lab reports in **one** `ArrayList<MedicalRecord>` and print any of them with one call — the polymorphism backbone of the project.

## 3.8 `Prescription.java` (extends MedicalRecord)

```java
public class Prescription extends MedicalRecord {
    private String medicineName;
    private String dosage;
    private String duration;

    @Override
    public String getSummary() {
        return "Prescription: " + medicineName + " (" + dosage + ") for " + duration;
    }
    // ... constructor with super(...), getters
}
```

**What it does:** adds the medicine details; its `getSummary()` prints e.g. `Prescription: Napa (500mg) for 5 days`.

**Why it is here:** one of the two concrete record types. Its own getters (`getMedicineName()` etc.) are used by `FileManager` when writing `prescriptions.csv`.

## 3.9 `LabReport.java` (extends MedicalRecord)

```java
public class LabReport extends MedicalRecord {
    private String testName;
    private String result;
    private String referenceRange;

    @Override
    public String getSummary() {
        return "Lab Report: " + testName + " = " + result + " (normal range: " + referenceRange + ")";
    }
    // ... constructor with super(...), getters
}
```

**What it does:** the other record type; `getSummary()` prints e.g. `Lab Report: CBC = Normal (normal range: 4-11)`.

**Why two subclasses matter:** in `RecordView` this single loop prints both types correctly — Java picks the right `getSummary()` at **runtime**:

```java
for (int i = 0; i < records.size(); i++) {
    MedicalRecord r = records.get(i);
    recordList.getItems().add("[" + r.getDate() + "] " + r.getSummary());  // ← polymorphism
}
```

---

# 4. Package `storage`

No GUI, no models — the working engine: account handling, the master lists + rules, and file persistence.

## 4.1 `Authentication.java`

```java
public class Authentication {

    // each entry is "username,encodedPassword" (password stored as ascii codes)
    private static ArrayList<String> users = new ArrayList<String>();

    public static ArrayList<String> getUsers() { return users; }

    // converts a password to ascii codes joined with dashes, e.g. "abcd" -> "97-98-99-100"
    private static String encodePassword(String password) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            if (i > 0) {
                sb.append("-");
            }
            sb.append((int) password.charAt(i));
        }
        return sb.toString();
    }

    // true if the stored password field is already in ascii-code form
    private static boolean isEncoded(String stored) {
        return stored.matches("\\d+(-\\d+)+");
    }

    // converts old plaintext passwords in the list to ascii-code form and saves the file
    public static void migrateOldPasswords() {
        boolean changed = false;
        for (int i = 0; i < users.size(); i++) {
            String[] parts = users.get(i).split(",", 2);
            if (parts.length == 2 && !isEncoded(parts[1])) {
                users.set(i, parts[0] + "," + encodePassword(parts[1]));
                changed = true;
            }
        }
        if (changed) {
            FileManager.saveUsers();
        }
    }

    // returns an error message, or null if registration worked
    public static String register(String username, String password) {
        if (username == null || username.trim().equals("")) {
            return "Username cannot be empty!";
        }
        if (password == null || password.trim().equals("")) {
            return "Password cannot be empty!";
        }
        if (username.contains(",") || password.contains(",")) {
            return "Username and password cannot contain commas!";
        }
        if (password.length() < 4) {
            return "Password must be at least 4 characters!";
        }
        // check if username already exists
        for (int i = 0; i < users.size(); i++) {
            String[] parts = users.get(i).split(",");
            if (parts[0].equals(username)) {
                return "Username already taken!";
            }
        }
        users.add(username + "," + encodePassword(password));
        FileManager.saveUsers();
        return null;
    }

    public static boolean login(String username, String password) {
        for (int i = 0; i < users.size(); i++) {
            String[] parts = users.get(i).split(",");
            if (parts[0].equals(username) && parts[1].equals(encodePassword(password))) {
                return true;
            }
        }
        return false;
    }
}
```

**What each part does:**
- `static ArrayList<String> users` → one shared account list for the whole program; each entry is one file line `"username,encodedPassword"`.
- `encodePassword()` → the password is **never stored as readable text**. Each character becomes its ASCII number, joined with dashes: `1234` → `49-50-51-52`, `abcd` → `97-98-99-100`. Dashes (not commas) are used so the encoded form never breaks the `split(",")` file format.
- `isEncoded()` → recognizes the encoded form with the regex `\d+(-\d+)+` (digits with at least one dash). Since a password must be ≥ 4 characters, an encoded password always contains dashes — that's how old plaintext lines are told apart from new encoded ones.
- `migrateOldPasswords()` → runs once at startup (called from `FileManager.loadAll()`). Any old plaintext entry left in `users.csv` is converted to ASCII form and the file is re-saved — but only if something actually changed, so a second run does nothing (idempotent). Old users keep logging in with the same password they always had.
- `register()` returns a **String error or `null` for success** — the GUI shows whatever comes back. Checks in order: not empty → no commas (a comma would break `split(",")` when reading the file back!) → password length → username not taken. All checks run on the **raw** password; only the final stored line gets the encoded form.
- `login()` → splits each stored line, `parts[0]` is username; the typed password is encoded and compared **encoded-to-encoded** against `parts[1]`. The stored password is never decoded back.

**Why it is here:** proposal requires "user registration, login, and credential validation" as its own class. The ASCII encoding means nobody sees a password by just opening `users.csv`. **Viva note:** this is *encoding* (reversible obfuscation), not *hashing* — real systems use one-way hashes like SHA-256 so even the stored form can't be turned back into the password. Say "encoding" if asked, and mention hashing as the production-grade upgrade.

## 4.2 `HospitalManager.java` — the brain

```java
public class HospitalManager {
    private static ArrayList<Patient> patients = new ArrayList<Patient>();
    private static ArrayList<Doctor> doctors = new ArrayList<Doctor>();
    private static ArrayList<Staff> staffList = new ArrayList<Staff>();
    private static ArrayList<Appointment> appointments = new ArrayList<Appointment>();
    private static ArrayList<MedicalRecord> records = new ArrayList<MedicalRecord>();
    // ... getters for all five lists
```

**Why static:** exactly one copy of each list exists, and any screen can call `HospitalManager.getPatients()` without creating/passing objects around.

**ID generation:**

```java
    public static String nextPatientId() {
        return "P" + (patients.size() + 1);   // P1, P2, P3 ...
    }
```
Same pattern gives `A1...` for appointments, `R1...` for records. Simple because nothing is ever deleted.

**The one patient lookup (and the exception source):**

```java
    public static Patient findPatient(String id) throws InvalidPatientIDException {
        if (id == null || id.trim().equals("")) {
            throw new InvalidPatientIDException("Patient ID is empty!");
        }
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getId().equals(id)) {
                return patients.get(i);
            }
        }
        throw new InvalidPatientIDException("No patient found with ID: " + id);
    }
```
Linear search; throws our custom exception on empty or unknown ID. *Every* lookup in the app funnels through this one method — that's why the exception protects everything. (`findDoctor` returns `null` instead, because doctor IDs come from combo boxes and can't really be typed wrong.)

**The most important method — booking:**

```java
    public static Appointment bookAppointment(String patientId, String doctorId, String timeSlot)
            throws InvalidPatientIDException, AppointmentConflictException {

        Patient patient = findPatient(patientId);          // (1) may throw InvalidPatientIDException
        Doctor doctor = findDoctor(doctorId);

        // (2) check if the patient already has an appointment at this time
        ArrayList<Appointment> patientAppts = patient.getAppointments();
        for (int i = 0; i < patientAppts.size(); i++) {
            Appointment a = patientAppts.get(i);
            if (a.getTimeSlot().equals(timeSlot) && a.getStatus() == AppointmentStatus.SCHEDULED) {
                throw new AppointmentConflictException("Patient " + patient.getName()
                        + " already has an appointment at " + timeSlot);
            }
        }

        // (3) this will throw AppointmentConflictException if the doctor is busy
        doctor.book(timeSlot);

        // (4) only reached if all checks passed
        Appointment appt = new Appointment(nextAppointmentId(), patientId, doctorId,
                timeSlot, doctor.getConsultationFee());
        appointments.add(appt);
        patient.addAppointment(appt);
        FileManager.saveAll();
        return appt;
    }
```
Four steps: find patient → patient-side conflict check → doctor-side conflict check (inside `Doctor.book`) → create, link to both lists, save. The fee is copied from the doctor at booking time.

**Rescheduling:**

```java
    public static void rescheduleAppointment(Appointment appt, String newSlot)
            throws InvalidPatientIDException, AppointmentConflictException {
        Patient patient = findPatient(appt.getPatientId());
        // (1) patient-side check: any OTHER scheduled appointment at the new time?
        //     (a != appt skips the appointment we are moving)
        // (2) doctor.reschedule(oldSlot, newSlot)
        //     → doctor throws if the new slot is taken, otherwise swaps the slots
        // (3) appt.reschedule(...) updates the appointment's own timeSlot
        FileManager.saveAll();
    }
```
Same two conflict checks as booking, in the same order. Note the `a != appt` detail: without it, the appointment being moved could collide with itself when the form still shows its current slot. The doctor's `reschedule` frees the old slot, so someone else can book it afterwards.

**Cancelling:**

```java
    public static void cancelAppointment(Appointment appt) {
        Doctor doctor = findDoctor(appt.getDoctorId());
        if (doctor != null) {
            doctor.cancel(appt.getTimeSlot());   // frees the doctor's slot
        }
        appt.cancel(appt.getTimeSlot());          // sets status to CANCELLED
        FileManager.saveAll();
    }
```
Two things must happen together: the slot becomes free again AND the appointment is marked cancelled (so billing ignores it).

**Adding a record:**

```java
    public static void addRecord(MedicalRecord record) throws InvalidPatientIDException {
        Patient patient = findPatient(record.getPatientId()); // verify patient exists first
        patient.addRecord(record);                            // attach to the patient
        records.add(record);                                  // and to the global list
        FileManager.saveAll();
    }
```

**Seeding:**

```java
    public static void seedDoctors() {
        if (doctors.size() == 0) {   // only on the very first run
            addDoctor(new Doctor("D1", "Dr. Rahman", 45, "01711111111", "Cardiology", 1500));
            // ... 3 more doctors
            FileManager.saveAll();
        }
    }
```
**Why:** there is no doctor-registration screen (not in the proposal), so without seeding, appointments could never be tested. The `size() == 0` check makes it run only once ever.

## 4.3 `FileManager.java` — persistence

**The file format** — one folder `data/`, one **CSV** file per type, one object per line, fields separated by commas (the files open directly in Excel):

| File | Line format | Example |
|---|---|---|
| `users.csv` | `username,encodedPassword` | `faisal,49-50-51-52` |
| `patients.csv` | `id,name,age,phone,disease` | `P1,Rahim,30,01700000000,Fever` |
| `doctors.csv` | `id,name,age,phone,specialization,fee` | `D1,Dr. Rahman,45,...,Cardiology,1500.0` |
| `staff.csv` | `id,name,age,phone,role,salary` | |
| `appointments.csv` | `id,patientId,doctorId,timeSlot,status,fee,paid` | `A1,P1,D1,2026-08-22 10:00 AM,SCHEDULED,1500.0,false` |
| `prescriptions.csv` | `recordId,date,patientId,medicine,dosage,duration` | |
| `labreports.csv` | `recordId,date,patientId,testName,result,range` | |

**The two workhorse helpers:**

```java
    // reads all lines from a file, returns empty list if file doesnt exist yet
    private static ArrayList<String> readLines(String fileName) {
        ArrayList<String> lines = new ArrayList<String>();
        File file = new File(DATA_FOLDER + File.separator + fileName);
        if (!file.exists()) {
            return lines;                       // first run: no file, no problem
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                if (!line.trim().equals("")) {
                    lines.add(line);            // skip blank lines
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    // writes all lines to a file (overwrites the old file)
    private static void writeLines(String fileName, ArrayList<String> lines) {
        checkFolder();                          // creates data/ if missing
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(DATA_FOLDER + File.separator + fileName));
            for (int i = 0; i < lines.size(); i++) {
                writer.println(lines.get(i));
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
```
Every load method uses `readLines`, every save method uses `writeLines`. Saving **overwrites the whole file** each time — simple and can't get out of sync.

**Loading — `loadAll()` and why the ORDER matters:**

```java
    public static void loadAll() {
        checkFolder();
        loadUsers();
        Authentication.migrateOldPasswords(); // convert any old plaintext passwords once
        loadDoctors();       // 1) independent things first
        loadPatients();
        loadStaff();
        loadAppointments();  // 2) needs patients + doctors already loaded!
        loadPrescriptions(); // 3) needs patients already loaded!
        loadLabReports();
    }
```

The `migrateOldPasswords()` call sits right after `loadUsers()`: users are loaded first, then any line still holding a plaintext password gets converted to ASCII form and the file re-saved (see `Authentication` above).

A typical load method — parsing one line back into an object:

```java
    private static void loadDoctors() {
        ArrayList<String> lines = readLines("doctors.csv");
        for (int i = 0; i < lines.size(); i++) {
            String[] p = lines.get(i).split(",");
            // id,name,age,phone,specialization,fee
            Doctor d = new Doctor(p[0], p[1], Integer.parseInt(p[2]), p[3], p[4], Double.parseDouble(p[5]));
            HospitalManager.getDoctors().add(d);
        }
    }
```
`split(",")` cuts the line into pieces; numbers are converted back with `Integer.parseInt` / `Double.parseDouble`. **This split is why every GUI form blocks commas in input** — a comma inside a name would shift all the pieces.

**The clever part — re-linking in `loadAppointments()`:**

```java
            Appointment a = new Appointment(p[0], p[1], p[2], p[3], Double.parseDouble(p[5]));
            a.setStatus(AppointmentStatus.valueOf(p[4]));   // String → enum
            a.setPaid(Boolean.parseBoolean(p[6]));
            HospitalManager.getAppointments().add(a);

            // link the appointment back to the patient and block the doctors slot
            try {
                Patient patient = HospitalManager.findPatient(a.getPatientId());
                patient.addAppointment(a);
            } catch (Exception e) { /* patient not found, skip */ }
            Doctor d = HospitalManager.findDoctor(a.getDoctorId());
            if (d != null && a.getStatus() == AppointmentStatus.SCHEDULED) {
                d.getTimeSlots().add(a.getTimeSlot());       // re-block the slot!
            }
```
Files only store IDs, so after loading, the object connections must be rebuilt: put the appointment back into the patient's list, and — if still SCHEDULED — put its slot back into the doctor's taken-list. **This is why the app still refuses a double booking after you restart it.** Prescriptions/lab reports get re-attached to their patient the same way.

**Saving records — where `instanceof` earns its place:**

```java
    private static void saveRecords() {
        ArrayList<String> presLines = new ArrayList<String>();
        ArrayList<String> labLines = new ArrayList<String>();
        ArrayList<MedicalRecord> records = HospitalManager.getRecords();
        for (int i = 0; i < records.size(); i++) {
            MedicalRecord r = records.get(i);
            if (r instanceof Prescription) {
                Prescription p = (Prescription) r;       // downcast to reach subclass getters
                presLines.add(p.getRecordId() + "," + ... + p.getMedicineName() + ...);
            } else if (r instanceof LabReport) {
                LabReport l = (LabReport) r;
                labLines.add(l.getRecordId() + "," + ... + l.getTestName() + ...);
            }
        }
        writeLines("prescriptions.csv", presLines);
        writeLines("labreports.csv", labLines);
    }
```
All records live in ONE mixed list, but they go to TWO different files with different columns — so we check the real type with `instanceof` and **downcast** to call the subclass-only getters.

**Why this class exists:** all file I/O in one place. Models never touch files; GUI never touches files. If we ever changed the format, only this file changes.

---

# 5. Package `gui`

Six screens. **Every screen follows the same pattern** — learn it once:

```java
public class SomeView {
    public static void show(Stage stage, String username) {
        // 1. create controls  (Label, TextField, Button, TableView...)
        // 2. arrange them     (GridPane for forms, VBox/HBox for stacking)
        // 3. wire buttons     (btn.setOnAction(e -> { ... });)
        // 4. swap the scene   (stage.setScene(new Scene(root, w, h));)
    }
}
```
- There is only **one window** (`Stage`). "Navigation" = putting a new `Scene` on the same stage. Back button = calling the previous view's `show()`.
- `username` is passed along only so every Back button can return to a Dashboard that still says "Welcome, username".
- Every screen has private `showError(msg)` / `showInfo(msg)` helpers that pop up an `Alert`.
- Tables use `PropertyValueFactory<>("fieldName")` — JavaFX finds the getter by name: `"appointmentId"` → calls `getAppointmentId()` on each row object. **The string must match the getter name.**

## 5.1 `LoginView.java`

The first screen. Key pieces:

```java
        PasswordField passField = new PasswordField();   // masks typing with dots
```

```java
        loginBtn.setOnAction(e -> {
            if (Authentication.login(userField.getText(), passField.getText())) {
                DashboardView.show(stage, userField.getText());  // go to menu
            } else {
                // error Alert: "Wrong username or password!"
            }
        });

        registerBtn.setOnAction(e -> {
            String error = Authentication.register(userField.getText(), passField.getText());
            if (error == null) { /* success popup */ } else { /* show the error string */ }
        });

        exitBtn.setOnAction(e -> stage.close());   // closes the window = ends the program
```

**Why it is here:** proposal requires "Login, Register, and Exit options". It contains **zero logic** itself — it only calls `Authentication` and shows the result. GUI and logic stay separated.

## 5.2 `DashboardView.java`

```java
        patientBtn.setOnAction(e -> PatientView.show(stage, username));
        appointmentBtn.setOnAction(e -> AppointmentView.show(stage, username));
        recordBtn.setOnAction(e -> RecordView.show(stage, username));
        billingBtn.setOnAction(e -> BillingView.show(stage, username));
        logoutBtn.setOnAction(e -> LoginView.show(stage));   // logout = back to login
```

**Why it is here:** the proposal's "main menu after login, access point for all features". Pure navigation — five buttons, five `show()` calls, nothing else.

## 5.3 `PatientView.java`

Three parts: a form (top), buttons (middle), a `TableView<Patient>` of everyone (bottom).

**The table setup (pattern used in every table screen):**

```java
        TableColumn<Patient, String> nameCol = new TableColumn<Patient, String>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<Patient, String>("name")); // → getName()
        ...
        table.setItems(FXCollections.observableArrayList(HospitalManager.getPatients()));
```
`FXCollections.observableArrayList(...)` wraps our plain ArrayList into the list type TableView needs.

**Register button:**

```java
        addBtn.setOnAction(e -> {
            String error = checkInputs(nameField.getText(), ageField.getText(),
                                       phoneField.getText(), diseaseField.getText());
            if (error != null) { showError(error); return; }   // stop if invalid
            Patient p = new Patient(HospitalManager.nextPatientId(), ... );
            HospitalManager.addPatient(p);                     // adds + saves to file
            table.setItems(...);                               // reload the table
            // clear the form, show "registered with ID: P1"
        });
```

**Update flow (two buttons):** `Load Selected` copies the selected row's values *into* the form; the user edits; `Update Selected` writes the form back through the patient's **setters**, saves, and calls `table.refresh()` so the row redraws.

**The validator:**

```java
    private static String checkInputs(String name, String age, String phone, String disease) {
        // all fields filled?  no commas (would corrupt the csv file)!
        // age parses as int and is 1–150?
        // returns the problem as a String, or null if everything is fine
    }
```

**Why it is here:** proposal requires "Register new patients and view/update patient details". It's also where patient IDs are born (`nextPatientId()`).

## 5.4 `AppointmentView.java`

**Picking without typing** — combo boxes are pre-filled with readable strings:

```java
        for (int i = 0; i < patients.size(); i++) {
            patientBox.getItems().add(patients.get(i).getId() + " - " + patients.get(i).getName());
        }   // shows "P1 - Rahim"
```

**Book button — where both custom exceptions surface:**

```java
        bookBtn.setOnAction(e -> {
            // 1. all four inputs selected?
            // 2. recover the IDs: "P1 - Rahim" → split(" - ")[0] → "P1"
            String patientId = patientBox.getValue().split(" - ")[0];
            String doctorId = doctorBox.getValue().split(" - ")[0];
            // 3. build the slot string: "2026-08-22" + " " + "10:00 AM"
            String timeSlot = datePicker.getValue().toString() + " " + timeBox.getValue();
            try {
                Appointment appt = HospitalManager.bookAppointment(patientId, doctorId, timeSlot);
                // refresh table, success popup with the new ID
            } catch (InvalidPatientIDException ex) {
                showError(ex.getMessage());          // bad patient
            } catch (AppointmentConflictException ex) {
                showError(ex.getMessage());          // double booking
            }
        });
```
One method call, two separate catch blocks — this is the demo moment for the custom exceptions.

**Reschedule button:** select a row in the table, pick a **new** date + time in the same form, press Reschedule Selected. Checks: a row is selected, it isn't `CANCELLED` (book a new one instead), a date/time is picked, and the new slot differs from the current one. Then one call — `HospitalManager.rescheduleAppointment(selected, newSlot)` — inside the same double try/catch as booking, because moving can hit the same two conflicts (doctor busy / patient busy).

**Cancel button:** takes the selected table row, refuses if already `CANCELLED`, otherwise `HospitalManager.cancelAppointment(selected)` + `table.refresh()`.

**Why it is here:** proposal requires "Book, view, and cancel appointments with available doctors". Reschedule was added on top so the `Schedulable.reschedule()` method is reachable from the GUI too.

## 5.5 `RecordView.java`

**Top — the polymorphism showcase:** pick a patient, press Show Records:

```java
        ArrayList<MedicalRecord> records = p.getRecords();
        for (int i = 0; i < records.size(); i++) {
            MedicalRecord r = records.get(i);
            // polymorphism - getSummary() works differently for each record type
            recordList.getItems().add("[" + r.getDate() + "] " + r.getSummary());
        }
```
One loop; prescriptions and lab reports each print their own format.

**Bottom — two side-by-side mini-forms.** Both work identically; only the class they build differs:

```java
            Prescription pres = new Prescription(HospitalManager.nextRecordId(),
                    LocalDate.now().toString(),   // today's date, automatic
                    patientId, medField.getText().trim(), ...);
            HospitalManager.addRecord(pres);      // may throw InvalidPatientIDException
```
The lab-report button is the same but `new LabReport(...)` with testName/result/range. Both validate empty fields and commas first (the `hasComma()` helper).

**Why it is here:** proposal requires "Browse a patient's prescriptions and lab reports" — plus the forms to create them, otherwise there'd be nothing to browse.

## 5.6 `BillingView.java`

**The core is `refreshTable()`** (used by both Show Bill and after paying):

```java
    private static void refreshTable(ComboBox<String> patientBox, TableView<Appointment> table, Label totalLabel) {
        String patientId = patientBox.getValue().split(" - ")[0];
        try {
            Patient p = HospitalManager.findPatient(patientId);
            ArrayList<Appointment> unpaid = new ArrayList<Appointment>();
            ArrayList<Appointment> all = p.getAppointments();
            for (int i = 0; i < all.size(); i++) {
                Appointment a = all.get(i);
                if (!a.isPaid() && a.getStatus() != AppointmentStatus.CANCELLED) {
                    unpaid.add(a);                      // only unpaid + not cancelled
                }
            }
            table.setItems(FXCollections.observableArrayList(unpaid));
            // calculateBill() comes from the Billable interface
            totalLabel.setText("Total Due: " + p.calculateBill() + " Tk");
        } catch (InvalidPatientIDException ex) {
            showError(ex.getMessage());
        }
    }
```
Builds the itemized unpaid list, and gets the total from `p.calculateBill()` — the `Billable` interface in action.

**Mark Selected as Paid:**

```java
            selected.setPaid(true);
            FileManager.saveAll();
            refreshTable(...);   // row disappears, total drops
```

**Why it is here:** proposal requires "Shows itemized bills and marks them as paid".

## 5.7 `DoctorView.java`

Read-only list screen. One `TableView<Doctor>` with two columns:
- **Name** → `PropertyValueFactory("name")` → `getName()` (inherited from `Person`)
- **Department** → `PropertyValueFactory("specialization")` → `getSpecialization()` — the column *header* says "Department" but the underlying field is `specialization`; the factory string must match the getter, not the header.

Filled once from `HospitalManager.getDoctors()`. No buttons except Back. **Why it is here:** quick directory of doctors and their departments without going through the appointment form.

## 5.8 `StaffView.java`

Same pattern as `DoctorView`, but `TableView<Staff>` with four columns: **ID**, **Name**, **Phone** (getters inherited from `Person`) and **Role** (Staff's own `getRole()`), so this screen needed zero model changes. Data comes from `HospitalManager.getStaffList()`, which `FileManager.loadStaff()` filled from the hand-written `data/staff.csv`. **Why it is here:** makes the `Staff` class visible in the app — the model class existed from the start, this screen displays it.

---

# 6. Package `main`

## 6.1 `Main.java`

```java
package main;

import javafx.application.Application;
import javafx.stage.Stage;
import gui.LoginView;
import storage.FileManager;
import storage.HospitalManager;

// main class that starts the whole program
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // load everything from the csv files first
        FileManager.loadAll();
        // add default doctors if this is the first run
        HospitalManager.seedDoctors();
        LoginView.show(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

**What each part does:**
- `extends Application` → required by JavaFX; makes this class a JavaFX app.
- `main()` calls `launch(args)` → JavaFX starts its own engine, creates the window (`Stage`), and then calls **our** `start(stage)` method. We never call `start` ourselves.
- Inside `start`: load saved data → seed doctors (first run only) → show the login screen. Order matters: data must be in memory *before* any screen tries to display it.

**Why it is here:** the proposal's "Initializes the system and launches the JavaFX interface" — that is literally its three lines.

---

# Quick Lookup: "Which file is for what?"

| I want to find... | Look in |
|---|---|
| Why a wrong patient ID shows an error popup | `exceptions/InvalidPatientIDException` + `HospitalManager.findPatient()` |
| Why double booking is blocked | `exceptions/AppointmentConflictException` + `Doctor.book()` + `HospitalManager.bookAppointment()` |
| How the bill total is computed | `interfaces/Billable` + `Patient.calculateBill()` + `Appointment.calculateBill()` |
| Slot booking rules | `interfaces/Schedulable` + `Doctor` (real checking) + `Appointment` (own state) |
| Shared person fields | `model/Person` |
| Patient's records & appointments | `model/Patient` |
| Doctor's calendar/fee | `model/Doctor` |
| Appointment states | `model/AppointmentStatus` (enum) |
| Prescription vs lab report printing | `model/Prescription`, `model/LabReport` → `getSummary()` |
| Login/registration rules, password encoding | `storage/Authentication` |
| All lists + business rules | `storage/HospitalManager` |
| CSV reading/writing, file formats | `storage/FileManager` |
| Doctor directory / staff directory | `gui/DoctorView`, `gui/StaffView` (read-only tables) |
| Any screen | `gui/*View` (name matches the screen) |
| Program startup order | `main/Main` |
