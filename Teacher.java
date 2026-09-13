public class Teacher {
    String id;
    String name;
    String deptId;
    String availableDays;
    String availableSlots;

    public Teacher(String id, String name, String deptId, String availableDays, String availableSlots) {
        this.id = id;
        this.name = name;
        this.deptId = deptId;
        this.availableDays = availableDays;
        this.availableSlots = availableSlots;
    }

    @Override
    public String toString() {
        return name;
    }
}
