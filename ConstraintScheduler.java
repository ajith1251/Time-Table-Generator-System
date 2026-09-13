import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contains the logic for the Constraint Satisfaction Scheduling Algorithm.
 * Medium-level version:
 *  - Respects teacher availability (days + slots)
 *  - Avoids teacher clashes
 *  - Avoids classroom clashes
 *  - Avoids two subjects of same (dept, semester) at same slot
 *  - Tries to assign hours_per_week for each subject
 */
public class ConstraintScheduler {

    // These lists hold all constraints from DB
    private List<Teacher> teachers;
    private List<Subject> subjects;
    private List<Classroom> classrooms;
    private List<Timeslot> timeslots;

    public ConstraintScheduler() {
        teachers = new ArrayList<>();
        subjects = new ArrayList<>();
        classrooms = new ArrayList<>();
        timeslots = new ArrayList<>();
    }

    /**
     * Main method to execute the timetable generation.
     * @return true on success, false on failure
     */
    public boolean generate() {
        try {
            // STEP 1: Load data
            loadDataFromDatabase();

            System.out.println("Algorithm: Starting medium-level timetable generation...");

            // STEP 2: Open a DB connection for writing timetable
            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false); // we want all-or-nothing

                // Clear old timetable entries so we can regenerate
                clearExistingTimetable(conn);

                // Helper maps & sets for constraints
                Map<String, Teacher> teacherById = buildTeacherMap();
                Map<String, List<Classroom>> classroomsByDept = buildClassroomMap();

                // Track used combinations to avoid clashes
                // key format examples: "T1|SLOT1", "C101|SLOT2", "CSE-AIML-01|3|SLOT1"
                Set<String> usedTeacherSlot = new HashSet<>();
                Set<String> usedRoomSlot = new HashSet<>();
                Set<String> usedDeptSemSlot = new HashSet<>();

                // Prepared statement for inserting rows into Timetable
                String insertSql = "INSERT INTO Timetable (class_id, subject_id, teacher_id, slot_id, day) "
                        + "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

                    // STEP 3: Main scheduling loop
                    for (Subject subject : subjects) {
                        Teacher teacher = teacherById.get(subject.teacherId);
                        if (teacher == null) {
                            System.out.println("Warning: No teacher found for subject " + subject.name
                                    + " (id=" + subject.id + "). Skipping.");
                            continue;
                        }

                        // Try to use classrooms from the same dept first
                        List<Classroom> candidateRooms = classroomsByDept.get(subject.deptId);
                        if (candidateRooms == null || candidateRooms.isEmpty()) {
                            // Fallback: use any classroom
                            candidateRooms = classrooms;
                        }
                        if (candidateRooms.isEmpty()) {
                            System.out.println("Warning: No classrooms available at all. Skipping subject " + subject.name);
                            continue;
                        }

                        int hoursToAssign = subject.hoursPerWeek;
                        int assigned = 0;

                        // Try to assign this subject to multiple timeslots (hours_per_week)
                        for (Timeslot slot : timeslots) {
                            if (assigned >= hoursToAssign) {
                                break; // done for this subject
                            }

                            // Check if teacher is available in this slot
                            if (!isTeacherAvailableInSlot(teacher, slot)) {
                                continue;
                            }

                            // Check clashes: teacher already used at this slot?
                            String teacherSlotKey = teacher.id + "|" + slot.id;
                            if (usedTeacherSlot.contains(teacherSlotKey)) {
                                continue;
                            }

                            // Check clashes: another subject of same (dept, semester) at this slot?
                            String deptSemSlotKey = subject.deptId + "|" + subject.semester + "|" + slot.id;
                            if (usedDeptSemSlot.contains(deptSemSlotKey)) {
                                continue;
                            }

                            // Find a free classroom for this slot
                            Classroom chosenRoom = null;
                            for (Classroom room : candidateRooms) {
                                String roomSlotKey = room.id + "|" + slot.id;
                                if (!usedRoomSlot.contains(roomSlotKey)) {
                                    chosenRoom = room;
                                    break;
                                }
                            }
                            if (chosenRoom == null) {
                                // No free classroom for this slot
                                continue;
                            }

                            // We found a valid assignment -> insert into Timetable
                            insertTimetableRow(insertStmt, chosenRoom, subject, teacher, slot);

                            // Mark combinations as used
                            usedTeacherSlot.add(teacherSlotKey);
                            usedRoomSlot.add(chosenRoom.id + "|" + slot.id);
                            usedDeptSemSlot.add(deptSemSlotKey);

                            assigned++;

                            System.out.println("Assigned: Subject " + subject.name
                                    + " -> Teacher " + teacher.name
                                    + " -> Room " + chosenRoom.name
                                    + " -> " + slot.toString());
                        }

                        if (assigned < hoursToAssign) {
                            System.out.println("Notice: Could only assign " + assigned + " / " + hoursToAssign
                                    + " hours for subject " + subject.name);
                        }
                    }

                    // Execute all inserts and commit
                    conn.commit();
                    System.out.println("Algorithm: Timetable generation finished and saved.");

                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            }

            return true; // success

        } catch (SQLException e) {
            System.err.println("Error during timetable generation: " + e.getMessage());
            e.printStackTrace();
            return false; // failure
        }
    }

    // ----------------------------------------------------
    // DATA LOADING
    // ----------------------------------------------------

    /**
     * Loads all necessary data from the database into lists.
     */
    private void loadDataFromDatabase() throws SQLException {
        System.out.println("Algorithm: Fetching data from database...");

        try (Connection conn = DBConnection.getConnection()) {
            loadTeachers(conn);
            loadClassrooms(conn);
            loadSubjects(conn);
            loadTimeslots(conn);

            System.out.println("--- Data Loaded Successfully ---");
            System.out.println("Teachers:   " + teachers.size());
            System.out.println("Classrooms: " + classrooms.size());
            System.out.println("Subjects:   " + subjects.size());
            System.out.println("Timeslots:  " + timeslots.size());
            System.out.println("---------------------------------");
        } catch (SQLException e) {
            System.err.println("Database connection or query failed while loading data!");
            throw e;
        }
    }

    private void loadTeachers(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Teacher";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            teachers.clear();
            while (rs.next()) {
                teachers.add(new Teacher(
                        rs.getString("teacher_id"),
                        rs.getString("teacher_name"),
                        rs.getString("dep_id"),
                        rs.getString("available_days"),   // NEW: from DB
                        rs.getString("available_slots")   // NEW: from DB
                ));
            }
        }
    }

    private void loadClassrooms(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Classroom";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            classrooms.clear();
            while (rs.next()) {
                classrooms.add(new Classroom(
                        rs.getString("class_id"),
                        rs.getString("class_name"),
                        rs.getInt("capacity"),
                        rs.getString("dep_id")
                ));
            }
        }
    }

    private void loadSubjects(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Subject";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            subjects.clear();
            while (rs.next()) {
                subjects.add(new Subject(
                        rs.getString("sub_id"),
                        rs.getString("sub_name"),
                        rs.getString("dep_id"),
                        rs.getString("teacher_id"),
                        rs.getInt("sem"),
                        rs.getInt("hours_per_week")
                ));
            }
        }
    }

    private void loadTimeslots(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Timeslot";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            timeslots.clear();
            while (rs.next()) {
                timeslots.add(new Timeslot(
                        rs.getString("slot_id"),
                        rs.getString("day"),
                        rs.getTime("start_time"),
                        rs.getTime("end_time")
                ));
            }
        }
    }

    // ----------------------------------------------------
    // HELPER METHODS
    // ----------------------------------------------------

    private void clearExistingTimetable(Connection conn) throws SQLException {
        System.out.println("Algorithm: Clearing old records from Timetable table...");
        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Timetable")) {
            pstmt.executeUpdate();
        }
    }

    private Map<String, Teacher> buildTeacherMap() {
        Map<String, Teacher> map = new HashMap<>();
        for (Teacher t : teachers) {
            map.put(t.id, t);
        }
        return map;
    }

    private Map<String, List<Classroom>> buildClassroomMap() {
        Map<String, List<Classroom>> map = new HashMap<>();
        for (Classroom c : classrooms) {
            if (c.deptId == null) {
                continue;
            }
            map.computeIfAbsent(c.deptId, k -> new ArrayList<>()).add(c);
        }
        return map;
    }

    /**
     * Inserts one row into Timetable using a prepared statement.
     */
    private void insertTimetableRow(PreparedStatement insertStmt,
                                    Classroom room,
                                    Subject subject,
                                    Teacher teacher,
                                    Timeslot slot) throws SQLException {

        insertStmt.setString(1, room.id);
        insertStmt.setString(2, subject.id);
        insertStmt.setString(3, teacher.id);
        insertStmt.setString(4, slot.id);
        insertStmt.setString(5, slot.day);
        insertStmt.executeUpdate();
    }

    /**
     * Checks if a teacher is available at a given timeslot
     * using available_days and available_slots from the Teacher table.
     */
    private boolean isTeacherAvailableInSlot(Teacher teacher, Timeslot slot) {
        // Parse CSV strings into sets
        Set<String> days = parseCsvToSet(teacher.availableDays);
        Set<String> slots = parseCsvToSet(teacher.availableSlots);

        // If no availability given, assume always available
        if (days.isEmpty() && slots.isEmpty()) {
            return true;
        }

        // If days list is not empty, slot.day must be present
        if (!days.isEmpty() && !days.contains(slot.day)) {
            return false;
        }

        // If slots list is not empty, slot.id must be present
        if (!slots.isEmpty() && !slots.contains(slot.id)) {
            return false;
        }

        return true;
    }

    /**
     * Utility: parse a comma-separated string like "1,2,4"
     * into a Set<String>.
     */
    private Set<String> parseCsvToSet(String csv) {
        Set<String> set = new HashSet<>();
        if (csv == null) {
            return set;
        }

        String[] parts = csv.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                set.add(trimmed);
            }
        }
        return set;
    }
}
