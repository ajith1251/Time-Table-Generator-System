public class Classroom {
    String id;
    String name;
    int capacity;
    String deptId;

    public Classroom(String id, String name, int capacity, String deptId) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.deptId = deptId;
    }
    
    @Override
    public String toString() {
        return name + " (Cap: " + capacity + ")";
    }
}