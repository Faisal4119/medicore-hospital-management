# Viva Preparation — MediCore Hospital Management System

This file has two parts:

1. **How to present/demo the project** (the order that impresses the examiner)
2. **Likely viva questions with answers**, grounded in this exact codebase

---

## Part 1: How to Present the Project (Best Demo Flow)

Total demo should take **5–7 minutes**. Practice it twice before the viva so it's smooth.
Golden rule: **show a feature working, then show the code behind it.** Examiners give marks
for connecting the running program to the OOP concept.

### Step 0 — Before the viva

- Run `compile.bat` and `run.bat` once at home to confirm everything works.
- Open these files in tabs beforehand: `Person.java`, `Patient.java`, `MedicalRecord.java`,
  `Prescription.java`, `HospitalManager.java`, `Billable.java`, the two exception classes.
- Delete/backup test junk from `data/*.csv` so the demo data looks clean.
- Know your **one-line pitch**: *"MediCore is a JavaFX hospital management system that manages
  patients, doctors, appointments, medical records and billing, with CSV file persistence,
  built around an OOP class hierarchy."*

### Step 1 — Open with the architecture (30 seconds)

Show the package structure and say:

> "The project is split into 5 packages: `model` (the data classes), `interfaces`, `exceptions`,
> `storage` (file I/O + business logic), and `gui` (JavaFX views). The GUI never touches files
> directly — it goes through `HospitalManager`, which uses `FileManager` for CSV persistence."

This one sentence signals *separation of concerns* — examiners love it.

### Step 2 — Login/Register (1 min)

- Register a user, then log in.
- Open `data/users.csv` in Notepad/Excel and show the password is stored as ASCII codes
  (`97-98-99-100`), not plain text. Mention `encodePassword()` in `Authentication.java`.

### Step 3 — Patients (1 min)

- Register a patient. Show it appears in the list.
- **Close the app, reopen it** — show the patient is still there. Say: *"persistence via
  `FileManager.loadAll()` / `saveAll()` on CSV files."* This is a strong moment — do not skip it.

### Step 4 — Appointment conflict (1–2 min) ⭐ highlight of the demo

- Book an appointment with a doctor at a time slot.
- Book the **same doctor at the same slot** again → conflict error appears.
- Say: *"This error is my custom checked exception, `AppointmentConflictException`.
  `HospitalManager.bookAppointment()` checks the patient's slots, and `Doctor.book()` checks
  the doctor's booked slots — either can throw it, and the GUI catches it and shows the dialog."*
- Then show the `bookAppointment()` code (HospitalManager.java, ~line 83).

### Step 5 — Medical records = polymorphism (1 min)

- Add one Prescription and one LabReport for the same patient. Show the list where both display
  their summaries differently.
- Say: *"Both are stored in one `ArrayList<MedicalRecord>`. Calling `getSummary()` on each
  gives different output — that's runtime polymorphism through the abstract method."*

### Step 6 — Billing (30 sec)

- Show a patient's unpaid bill, mark as paid.
- Say: *"`Patient.calculateBill()` sums unpaid, non-cancelled appointments. Both `Patient`
  and `Appointment` implement the `Billable` interface."*

### Step 7 — Close with the OOP summary (30 sec)

End with: *"So the project covers inheritance (`Person` and `MedicalRecord` hierarchies),
interfaces (`Billable`, `Schedulable`), polymorphism (`getSummary`, `calculateBill`),
encapsulation (private fields + getters/setters), custom checked exceptions, and file I/O."*

### Demo tips

- If something crashes, stay calm: explain what the code *should* do and where. Panic loses more marks than bugs.
- Never say "I don't know why this line is here." If unsure, say "let me trace it" and read it aloud.
- Let the examiner drive after the demo — answer only what is asked, don't over-explain.

---

## Part 2: Likely Viva Questions & Answers

### A. Core OOP — guaranteed questions

**Q1. What OOP concepts did you use in this project? Point to where.**
> - **Encapsulation**: all model fields are `private`/`protected` with getters/setters (e.g. `Person.java`).
> - **Inheritance**: `Patient`, `Doctor`, `Staff` extend abstract `Person`; `Prescription`, `LabReport` extend abstract `MedicalRecord`.
> - **Polymorphism**: `getSummary()` is abstract in `MedicalRecord` and overridden differently in `Prescription` and `LabReport`; `calculateBill()` behaves differently in `Patient` vs `Appointment`.
> - **Abstraction**: abstract classes `Person`, `MedicalRecord`; interfaces `Billable`, `Schedulable`.

**Q2. Why is `Person` abstract? Can you create a `Person` object?**
> No — `new Person(...)` won't compile. A "person" in the hospital is always concretely a patient, doctor, or staff; an abstract class prevents creating a meaningless generic person while still sharing the common fields (`id`, `name`, `age`, `phone`) and their getters/setters.

**Q3. Difference between an abstract class and an interface? Why did you use both?**
> An abstract class can have fields, constructors, and implemented methods; a class extends only one. An interface (classically) only declares behavior; a class can implement many.
> In this project: `Person`/`MedicalRecord` are abstract classes because subclasses **share state** (fields + constructor). `Billable`/`Schedulable` are interfaces because they describe a **capability** cutting across unrelated classes — `Appointment` implements both, `Patient` implements `Billable`, `Doctor` implements `Schedulable`. Java has no multiple inheritance of classes, but `Appointment implements Schedulable, Billable` is fine.

**Q4. Show me runtime polymorphism in your code.**
> `HospitalManager` keeps `ArrayList<MedicalRecord> records` holding both `Prescription` and `LabReport` objects. When the GUI loops and calls `record.getSummary()`, the JVM picks the override based on the actual object type at runtime — Prescription prints medicine/dosage/duration, LabReport prints test/result. That's dynamic method dispatch.

**Q5. What is `super(id, name, age, phone)` doing in `Patient`'s constructor?**
> It calls the parent (`Person`) constructor to initialize the inherited fields. It must be the first statement in the constructor. Without it (and since `Person` has no no-arg constructor), the code wouldn't compile.

**Q6. Why are `Person`'s fields `protected` instead of `private`?**
> `protected` lets subclasses (`Patient`, `Doctor`, `Staff`) access them directly. (Honest follow-up if pushed: `private` + getters would be stricter encapsulation; `protected` was a convenience choice.)

**Q7. What does `@Override` do?**
> It's an annotation telling the compiler "this method must override a superclass/interface method." If the signature doesn't match anything (e.g. typo), compilation fails. Pure safety — removing it doesn't change behavior.

**Q8. What is an enum and why use `AppointmentStatus`?**
> A fixed set of named constants (`SCHEDULED`, `CANCELLED`, ...). Safer than strings: no typos like `"schduled"`, compiler-checked, and comparable with `==`.

### B. Exceptions — very likely

**Q9. What custom exceptions did you make, and why custom ones?**
> `InvalidPatientIDException` (thrown by `HospitalManager.findPatient()` when the ID is empty or not found) and `AppointmentConflictException` (thrown by `bookAppointment()`/`Doctor.book()` when a slot is taken). Custom exceptions give the failure a **meaningful name** and force callers (the GUI) to handle these specific business errors and show a proper dialog, instead of crashing or using generic exceptions.

**Q10. Checked vs unchecked exceptions — which are yours?**
> Both extend `Exception`, so they're **checked**: the compiler forces every caller to either `try/catch` or declare `throws`. Unchecked ones extend `RuntimeException` and don't need declaring. I made them checked deliberately — a booking conflict is a recoverable business situation the GUI *must* handle, not a programming bug.

**Q11. Walk me through what happens when a user books a doctor who is busy.**
> GUI calls `HospitalManager.bookAppointment(patientId, doctorId, timeSlot)` →
> 1. `findPatient()` validates the patient (may throw `InvalidPatientIDException`).
> 2. Loop over the patient's own appointments — if same slot & `SCHEDULED`, throw `AppointmentConflictException`.
> 3. `doctor.book(timeSlot)` — `Doctor` keeps an `ArrayList<String> timeSlots` of booked slots and throws if the slot is taken.
> 4. If no exception: create `Appointment` (status `SCHEDULED`, fee = doctor's consultation fee), add to lists, `FileManager.saveAll()`.
> The GUI's `catch` block turns the exception message into the error dialog the user sees.

### C. File I/O & persistence

**Q12. How does your data persist?**
> `FileManager` writes each entity list to a CSV file in `data/` (patients.csv, doctors.csv, appointments.csv, ...). On startup, `Main.start()` calls `FileManager.loadAll()`, which reads every file line-by-line with `BufferedReader`, splits on commas, and reconstructs the objects. Every mutation (add patient, book appointment, pay bill) calls `saveAll()`, which rewrites the files with `PrintWriter`/`FileWriter`.

**Q13. Why CSV and not a database / serialization?**
> Scope-appropriate: human-readable, opens in Excel, no external dependency, and demonstrates manual file I/O which was a course requirement. A database (JDBC) would be the natural upgrade for concurrent access and querying; Java serialization would be binary and not human-readable.

**Q14. What happens if the data folder or a file doesn't exist?**
> `checkFolder()` creates `data/` if missing; `readLines()` returns an empty list if the file doesn't exist. So first run starts clean without crashing, and `seedDoctors()` adds 4 default doctors.

**Q15. How are passwords stored?**
> Not plaintext — `Authentication.encodePassword()` converts each character to its ASCII code joined by dashes (`"abcd"` → `"97-98-99-100"`). There's also `migrateOldPasswords()` which upgrades any old plaintext entries on load.
> **Be honest if pushed**: this is *encoding*, not encryption/hashing — it's trivially reversible. Real systems use salted hashing (bcrypt). Say you know this; it earns marks.

### D. Code-specific "explain this line" questions

**Q16. How is a new patient ID generated?**
> `nextPatientId()` returns `"P" + (patients.size() + 1)`. Same pattern for appointments (`"A" + ...`).
> *Possible follow-up: what's the weakness?* If deletion were ever added, IDs could collide (size shrinks). Fine here because entities are never deleted; a stored counter or max-ID scan would be more robust.

**Q17. Why are all the lists and methods in `HospitalManager` static?**
> There's exactly one hospital state shared by all GUI views, so static members act as a single global access point (a simple singleton-like design). Trade-off: harder to unit test and can't have two hospitals — an instance-based singleton would be the refined version.

**Q18. How does `Patient.calculateBill()` work?**
> Iterates the patient's appointments, sums `calculateBill()` of each one that is **not paid** and **not cancelled**. `Appointment.calculateBill()` itself returns 0 for cancelled appointments, else the fee. So the interface method composes: the patient's bill is built from the appointments' bills.

**Q19. Where does the doctor's fee come from when booking?**
> `bookAppointment()` passes `doctor.getConsultationFee()` into the `Appointment` constructor, so each appointment snapshots the fee at booking time.

**Q20. What does `ArrayList<MedicalRecord>` in `Patient` demonstrate?**
> Upcasting + polymorphic collections: subclasses (`Prescription`, `LabReport`) stored via their abstract parent type. Also composition — a Patient *has* records and appointments ("has-a"), while Patient *is-a* Person ("is-a").

### E. Java general knowledge (asked in almost every viva)

**Q21. What does `public static void main(String[] args)` mean, and why does your `Main` extend `Application`?**
> `main` is the JVM entry point. This project is JavaFX, so `Main extends javafx.application.Application` and `main()` calls `launch(args)`, which starts the JavaFX runtime and calls `start(Stage stage)` on the JavaFX Application Thread. In `start()` we load the CSV data, seed doctors, and show `LoginView`.

**Q22. Difference between `==` and `.equals()` for Strings?**
> `==` compares references; `.equals()` compares contents. That's why `findPatient` uses `patients.get(i).getId().equals(id)`.

**Q23. ArrayList vs array?**
> ArrayList grows dynamically, has helper methods (`add`, `remove`, `size`), and works with generics; arrays are fixed-size. All entity lists here are ArrayLists because counts change at runtime.

**Q24. What is method overloading vs overriding?**
> Overloading: same name, different parameters, same class, resolved at compile time. Overriding: subclass redefines a superclass method with the same signature, resolved at runtime. This project mainly uses **overriding** (`getSummary`, `calculateBill`, `book`).

**Q25. Can an interface have a constructor? Can you instantiate one?**
> No constructors, no direct instantiation. You instantiate implementing classes; you *can* use an interface as a reference type (`Billable b = new Appointment(...)`).

### F. "Weakness" questions — prepare honest answers

Examiners often ask: *"What are the limitations? What would you improve?"* A confident, honest answer scores high.

1. **Password security** — ASCII encoding is reversible; would replace with salted hashing (e.g. bcrypt) — see Q15.
2. **`saveAll()` on every change** — rewrites all files each time; fine at this scale, inefficient for large data. Would save only the changed file or move to a database.
3. **ID generation** — `size() + 1` breaks if deletion is ever added (Q16).
4. **Static state** — `HospitalManager`/`Authentication` are all-static; hard to test, not extensible (Q17).
5. **CSV has no comma escaping** — a name containing a comma would corrupt a row (registration blocks commas for users, but that's mitigation not escaping). Proper fix: quote/escape fields or use a CSV library.
6. **No time-slot overlap logic** — slots are exact-string matches; "10:00" vs "10:30" for a 1-hour visit don't conflict. Real scheduling needs time ranges.

Saying "I know these limitations and here's how I'd fix them" is the single best viva move.

### G. Rapid-fire likely one-liners

| Question | Answer |
|---|---|
| Which class is the entry point? | `main.Main` (extends `Application`, `main()` → `launch()` → `start()`) |
| How many packages? | 5: main, model, interfaces, storage, exceptions (+ gui = 6 with GUI) |
| Which classes implement `Billable`? | `Patient` and `Appointment` |
| Which classes implement `Schedulable`? | `Doctor` and `Appointment` |
| Which classes are abstract? | `Person`, `MedicalRecord` |
| Where is the enum? | `AppointmentStatus` (SCHEDULED / CANCELLED / etc.) |
| Where is data stored? | CSV files in `data/` folder |
| What GUI toolkit? | JavaFX (`javafx.controls` module) |
| Who throws `InvalidPatientIDException`? | `HospitalManager.findPatient()` |
| Who throws `AppointmentConflictException`? | `HospitalManager.bookAppointment()`/`rescheduleAppointment()` and `Doctor.book()`/`reschedule()` |

---

## Final checklist (day before viva)

- [ ] `compile.bat` + `run.bat` work on the machine you'll demo on (JavaFX path correct!)
- [ ] Clean demo data in `data/` (no silly test names)
- [ ] Can draw the class hierarchy on paper from memory (Person → Patient/Doctor/Staff; MedicalRecord → Prescription/LabReport; interfaces on the side)
- [ ] Can explain `bookAppointment()` line by line
- [ ] Can state 3 limitations + fixes without hesitating
- [ ] README updated with correct course code and all group member names
