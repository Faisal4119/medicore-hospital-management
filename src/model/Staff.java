package model;

// represents non-doctor employees like nurses and receptionists
public class Staff extends Person {
    private String role;
    private double salary;

    public Staff(String id, String name, int age, String phone, String role, double salary) {
        super(id, name, age, phone);
        this.role = role;
        this.salary = salary;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }
}
