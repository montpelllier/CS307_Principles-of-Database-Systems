package referencecode;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Major;
import cn.edu.sustech.cs307.dto.Semester;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.SemesterService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ReferenceSemesterService implements SemesterService {
    private IntegrityViolationException integrityViolationException = new IntegrityViolationException();
    private EntityNotFoundException entityNotFoundException = new EntityNotFoundException();

    @Override
    public int addSemester(String name, Date begin, Date end) {
        String sql = "INSERT INTO project2.semester(name , begin_date, end_date) VALUES (?,?,?)";
        int id = -1;
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            if (begin.compareTo(end) >= 0) throw integrityViolationException;
            pstmt.setString(1, name);
            pstmt.setDate(2, begin);
            pstmt.setDate(3, end);
            pstmt.execute();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();//得到返回结果的集合
            if (generatedKeys.next()) id = generatedKeys.getInt(1);
        } catch(SQLException e) {
            e.printStackTrace();
            throw integrityViolationException;
        }

        return id;
    }

    @Override
    public void removeSemester(int semesterId) {
        String Semester = "DELETE FROM project2.semester WHERE id = ?";
        String getCourseSection = "SELECT idd FROM " +
                "(select c.id idd, major_id from project2.coursesection c join project2.course_major cm on c.course_id=cm.course_id) x where major_id = ?";
        String CourseSection = "DELETE FROM project2.coursesection where semester_id = ?";
        String CourseSectionClass = "DELETE FROM project2.CourseSectionClass where coursesection_id = ?";
        String StudentCourseSelection = "DELETE FROM project2.Student_CourseSelection where coursesection_id = ?";


        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmtSemester = connection.prepareStatement(Semester);
             PreparedStatement pstmtGetCourseSection = connection.prepareStatement(getCourseSection);
             PreparedStatement pstmtCourseSection = connection.prepareStatement(CourseSection);
             PreparedStatement pstmtCourseSectionClass = connection.prepareStatement(CourseSectionClass);
             PreparedStatement pstmtStudentCourseSelection = connection.prepareStatement(StudentCourseSelection);
        ) {
            pstmtGetCourseSection.setInt(1, semesterId);
            ResultSet rs = pstmtGetCourseSection.executeQuery();
            List<Integer> CourseSectionId = new ArrayList<>();
            while (rs.next()) CourseSectionId.add(rs.getInt(1));
            for (int i=0;i<CourseSectionId.size();i++){
                pstmtCourseSectionClass.setInt(1, CourseSectionId.get(i));
                pstmtCourseSectionClass.execute();
                pstmtStudentCourseSelection.setInt(1, CourseSectionId.get(i));
                pstmtStudentCourseSelection.execute();
                pstmtCourseSection.setInt(1,CourseSectionId.get(i));
                pstmtCourseSection.execute();
            }
            pstmtSemester.setInt(1,semesterId);
            pstmtSemester.execute();
        } catch(SQLException e) {
            e.printStackTrace();
            throw integrityViolationException;
        }
    }

    @Override
    public List<Semester> getAllSemesters() {
        String sql = "SELECT * FROM project2.semester";
        List<Semester> SemesterSet = new Vector<>();//VECTOR多线程；ARRAYLIST单线程；LINKEDLIST增删快
        Semester semester;
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            ResultSet rs = pstmt.executeQuery();
            ReferenceDepartmentService ds = new ReferenceDepartmentService();
            while (rs.next()) {
                semester = new Semester();
                semester.id = rs.getInt("id");
                semester.name = rs.getString("name");
                semester.begin = rs.getDate("begin_date");
                semester.end = rs.getDate("end_date");
                SemesterSet.add(semester);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return SemesterSet;
    }

    @Override
    public Semester getSemester(int semesterId) {
        String sql = "SELECT * FROM project2.semester WHERE id = ?";
        Semester semester = null;
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, semesterId);
            pstmt.execute();
            ResultSet rs = pstmt.executeQuery(sql);
            ReferenceDepartmentService ds = new ReferenceDepartmentService();

            semester = new Semester();
            if (rs.next()) {
                semester.id = rs.getInt("id");
                semester.name = rs.getString("name");
                semester.begin = rs.getDate("begin_date");
                semester.end = rs.getDate("end_date");
                rs.close();
            }else {
                throw entityNotFoundException;
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return semester;
    }
}
