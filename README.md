# Timetable Generator System

## Overview
This is an Automatic Timetable Generator system built with Java Swing and SQLite database. The application allows users to generate optimized timetables for educational institutions based on various constraints.

## Features
- Interactive GUI for managing timetables
- Constraint-based scheduling algorithm
- Export timetable to CSV and HTML formats
- Database integration (SQLite/MySQL)
- User-friendly interface with drag-and-drop capabilities

## Files Included
- `TimetableApp.java` - Main application file
- Various .java source files for different modules
- Compiled .class files for immediate execution
- JDBC drivers for database connectivity
- SQLite database file (timetable.db)
- Sample data and configuration files

## How to Run
1. Ensure you have Java JDK installed (version 8 or higher)
2. Navigate to the project directory
3. Run the application using:
   java -cp ".;sqlite-jdbc-3.36.0.3.jar;mysql-connector-j-9.5.0.jar" TimetableApp
4. The application will launch and you can start generating timetables

## Database
The system uses SQLite database (`timetable.db`) by default. Database schema can be found in `timetable.sql`.

## Dependencies
- SQLite JDBC Driver (sqlite-jdbc-3.36.0.3.jar)
- MySQL Connector/J (mysql-connector-j-9.5.0.jar)

## Notes
- The application is designed for educational institution timetable generation
- Includes export functionality to CSV and HTML formats
- Built with Java Swing for cross-platform compatibility

## Contributing
Feel free to fork this repository and submit pull requests for any improvements.

## License
This project is open source and available for modification and distribution.