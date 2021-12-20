package referencecode;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.CourseSection;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.InstructorService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReferenceInstructorService implements InstructorService {

    private Connection con = null;
    private ResultSet resultSet;

    private final String host = "localhost";
    private final String dbname = "postgres";
    private final String user = "postgres";
    private final String pwd = "521459";
    private final String port = "5432";
    public void openDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (Exception e) {
            System.err.println("Cannot find the PostgreSQL driver. Check CLASSPATH.");
            System.exit(1);
        }

        try {
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
            con = DriverManager.getConnection(url, user, pwd);

        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
    public void  closeDatasource() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addInstructor(int userId, String firstName, String lastName) {
//        openDatabase();
//        try {
//            PreparedStatement stmt = con.prepareStatement("INSERT INTO department (name) VALUES (?,?,?) returning integer");
//            stmt.setInt(1, userId);
//            stmt.setString(2, firstName);
//            stmt.setString(3, lastName);
//            ResultSet sqlRst = stmt.executeQuery();
//            sqlRst.next();
//            int r = sqlRst.getInt("id");
//            closeDatasource();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try(Connection connection= SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt=connection.prepareStatement("insert into project2.instructor (id, first_name, last_name) values (?,?,?);")) {
            stmt.setInt(1, userId);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IntegrityViolationException(e.getMessage());
        }
    }

    @Override
    public List<CourseSection> getInstructedCourseSections(int instructorId, int semesterId) {
//        openDatabase();
//        PreparedStatement stmt = con.prepareStatement(
//                "SELECT * FROM (coursesectionclass left join coursesection on coursesectionclass.section_id=coursesection.id)" +
//                "where instructor_id=? and semester_id=?");
//        stmt.setInt(1, instructorId);
//        stmt.setInt(2, semesterId);
//        ResultSet sqlRst = stmt.executeQuery();
//        List<CourseSection> result = new ArrayList<>();
//        while(sqlRst.next()) {
//            CourseSection temp = new CourseSection();
//            temp.id=sqlRst.getInt("section_id");
//            temp.name=sqlRst.getString("name");
//            temp.totalCapacity=sqlRst.getInt("totalCapacity");
//            temp.leftCapacity=sqlRst.getInt("leftCapacity");
//            result.add(temp);
//        }
//        closeDatasource();
//        return result;
        try(Connection connection= SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt=connection.prepareStatement("select cs.id, cs.name, cs.totalcapacity, cs.leftcapacity " +
                    "from project2.coursesection cs join project2.coursesectionclass csc on cs.id = csc.section_id where cs.semester_id=? and csc.instructor_id=? ;")) {
            stmt.setInt(1, semesterId);
            stmt.setInt(2, instructorId);
            ResultSet R = stmt.executeQuery();
            List<CourseSection> result = new ArrayList<>();
            while (R.next()) {
                CourseSection temp = new CourseSection();
                temp.id = R.getInt("id");
                temp.name = R.getString("name");
                temp.totalCapacity = R.getInt("totalcapacity");
                temp.leftCapacity = R.getInt("leftcapacity");
                result.add(temp);
            }
            R.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
