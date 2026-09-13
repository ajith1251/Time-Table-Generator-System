import java.sql.Time;

public class Timeslot {
    String id;
    String day;
    Time startTime;
    Time endTime;

    public Timeslot(String id, String day, Time startTime, Time endTime) {
        this.id = id;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    @Override
    public String toString() {
        return day + " (" + startTime + " - " + endTime + ")";
    }
}