public class Subject {
    String id;
    String name;
    String deptId;
    String teacherId;
    int semester;
    int hoursPerWeek;

    public Subject(String id, String name, String deptId, String teacherId, int semester, int hoursPerWeek) {
        this.id = id;
        this.name = name;
        this.deptId = deptId;
        this.teacherId = teacherId;
        this.semester = semester;
        this.hoursPerWeek = hoursPerWeek;
    }
    
    @Override
    public String toString() {
        return name;
    }
}