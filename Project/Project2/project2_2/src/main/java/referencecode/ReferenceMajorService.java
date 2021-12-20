package referencecode;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Department;
import cn.edu.sustech.cs307.dto.Major;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.MajorService;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class ReferenceMajorService implements MajorService {

    private IntegrityViolationException integrityViolationException = new IntegrityViolationException();
    private EntityNotFoundException entityNotFoundException = new EntityNotFoundException();

    @Override
    public int addMajor(String name, int departmentId) {
        String sql = "INSERT INTO project2.Major(name, department_id) VALUES (?,?)";
        int id = -1;
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            pstmt.setString(1, name);
            pstmt.setInt(2, departmentId);
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
    public void removeMajor(int majorId) {
        try(Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmtGetStudent = connection.prepareStatement("select id from project2.student where major_id= ? ;");
            PreparedStatement stmtMajor= connection.prepareStatement("delete from project2.major where id= ? ;");
            PreparedStatement stmtMajorCourse = connection.prepareStatement("delete from project2.course_major where major_id= ? ;");
            PreparedStatement stmtStudent= connection.prepareStatement("delete from project2.student where major_id= ? ;");
            PreparedStatement stmtStudentSection=connection.prepareStatement("delete from project2.student_coursesection where student_id= ? ;");
            PreparedStatement stmtGet=connection.prepareStatement("select *from project2.major where id= ?;")) {
            stmtGet.setInt(1,majorId);
            ResultSet rsGet=stmtGet.executeQuery();
            if (rsGet.next()) {
                stmtGetStudent.setInt(1, majorId);
                ResultSet rs = stmtGetStudent.executeQuery();
                List<Integer> studentId = new ArrayList<>();
                while (rs.next()) {
                    studentId.add(rs.getInt(1));
                }
                rs.close();
                for (int i = 0; i < studentId.size(); i++) {
                    stmtStudentSection.setInt(1, studentId.get(i));
                    stmtStudentSection.executeUpdate();
                }
                stmtStudent.setInt(1, majorId);
                stmtStudent.executeUpdate();
                stmtMajorCourse.setInt(1, majorId);
                stmtMajorCourse.executeUpdate();
                stmtMajor.setInt(1, majorId);
                stmtMajor.executeUpdate();
            }else {
                rsGet.close();
                throw entityNotFoundException;
            }
            rsGet.close();
        } catch (SQLException e) {
            throw entityNotFoundException;
        }
    }

    @Override
    public List<Major> getAllMajors() {
        String sql = "SELECT * FROM project2.Major";
        List<Major> MajorSet = new Vector<>();//VECTOR多线程；ARRAYLIST单线程；LINKEDLIST增删快
        Major major;
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            //pstmt.setInt(1, majorId);
            pstmt.execute();
            ResultSet rs = pstmt.executeQuery(sql);
            ReferenceDepartmentService ds = new ReferenceDepartmentService();
            while (rs.next()) {
                major = new Major();
                major.id = rs.getInt("id");
                major.name = rs.getString("name");
                int department_id = rs.getInt("department_id");
                major.department = ds.getDepartment(department_id);
                MajorSet.add(major);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return MajorSet;
    }

    @Override
    public Major getMajor(int majorId) {
        String sql = "SELECT * FROM project2.Major WHERE id = ?";
        Major major = null;
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, majorId);
            pstmt.execute();
            ResultSet rs = pstmt.executeQuery(sql);
            ReferenceDepartmentService ds = new ReferenceDepartmentService();

            major = new Major();
            major.id = rs.getInt("id");
            major.name = rs.getString("name");
            int department_id = rs.getInt("department_id");
            major.department = ds.getDepartment(department_id);
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return major;
    }

    @Override
    public void addMajorCompulsoryCourse(int majorId, String courseId) {
        String sql = "INSERT INTO project2.course_major(major_id, course_id, iscompulsory) VALUES (?,?,?)";
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, majorId);
            pstmt.setString(2, courseId);
            pstmt.setBoolean(3,true);
            pstmt.execute();
        } catch(SQLException e) {
            e.printStackTrace();
            throw integrityViolationException;
        }
    }

    @Override
    public void addMajorElectiveCourse(int majorId, String courseId) {
        String sql = "INSERT INTO project2.course_major(major_id, course_id, iscompulsory) VALUES (?,?,?)";
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, majorId);
            pstmt.setString(2, courseId);
            pstmt.setBoolean(3,false);
            pstmt.execute();
        } catch(SQLException e) {
            e.printStackTrace();
            throw integrityViolationException;
        }
    }

}
