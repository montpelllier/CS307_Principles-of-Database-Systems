package referencecode;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Instructor;
import cn.edu.sustech.cs307.dto.Student;
import cn.edu.sustech.cs307.dto.User;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.service.UserService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReferenceUserService implements UserService {

    private Connection con = null;
    private ResultSet resultSet;

    private final String host = "localhost";
    private final String dbname = "postgres";
    private final String user = "postgres";
    //private final String pwd = "521459";
    private final String pwd = "123456";
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
    public void removeUser(int userId) {
//        try(Connection connection = SQLDataSource.getInstance().getSQLConnection();
//            PreparedStatement stmt1=connection.prepareStatement("select *from project2.instructor where id=?;");
//            PreparedStatement stmt11=connection.prepareStatement("delete from project2.instructor where id=?;");
//            PreparedStatement stmt12=connection.prepareStatement("delete from project2.course_section_class where instructor_id=?;");
//            PreparedStatement stmt2=connection.prepareStatement("select *from project2.student where id=?;");
//            PreparedStatement stmt21=connection.prepareStatement("delete from project2.student where id=?;");
//            PreparedStatement stmt22=connection.prepareStatement("delete from project2.student_course_section where student_id=?;")) {
//            stmt1.setInt(1, userId);
//            stmt11.setInt(1, userId);
//            stmt12.setInt(1, userId);
//            stmt2.setInt(1, userId);
//            stmt21.setInt(1, userId);
//            stmt22.setInt(1, userId);
//            ResultSet R1 = stmt1.executeQuery();
//            ResultSet R2 = stmt2.executeQuery();
//            if (R1.next()) {
//                stmt11.executeQuery();
//                stmt12.executeQuery();
//            }
//            else if (R2.next()) {
//                stmt21.executeQuery();
//                stmt22.executeQuery();
//            }
//            else
//                throw new EntityNotFoundException();
//            R1.close();
//            R2.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        try(Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt=connection.prepareStatement("select *from project2.instructor where id= ?;");
            PreparedStatement student=connection.prepareStatement("select *from project2.student where id= ?;");
            PreparedStatement stmt_I=connection.prepareStatement("delete from project2.instructor where id= ?;");
            PreparedStatement stmt_sc=connection.prepareStatement("delete from project2.coursesectionclass where instructor_id= ?;");
            PreparedStatement stmt_s=connection.prepareStatement("delete from project2.student where id= ?;");
            PreparedStatement stmt_scs=connection.prepareStatement("delete from project2.student_coursesection where student_id= ?;")) {
            int flag=0;
            stmt.setInt(1,userId);
            student.setInt(1,userId);
            ResultSet R=stmt.executeQuery();
            if (R.next()){
                stmt_sc.setInt(1,userId);
                stmt_sc.executeUpdate();
                stmt_I.setInt(1,userId);
                stmt_I.executeUpdate();
                flag=1;
            }
            R=student.executeQuery();
            if (R.next()){
                stmt_scs.setInt(1,userId);
                stmt_scs.executeUpdate();
                stmt_s.setInt(1,userId);
                stmt_s.executeUpdate();
                flag=1;
            }
            R.close();
            if (flag==0){
                throw new EntityNotFoundException();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<User> getAllUsers() {
//        openDatabase();
//        PreparedStatement stmt1 = con.prepareStatement("SELECT * FROM Instructor");
//        ResultSet sqlRst = stmt1.executeQuery();
//        List<User> result = new ArrayList<>();
//        while (sqlRst.next()) {
//            User temp = new Instructor();
//            temp.id = sqlRst.getInt("id");
//            temp.fullName = sqlRst.getString("first_name")+sqlRst.getString("last_name");
//            result.add(temp);
//        }
//
//        PreparedStatement stmt2 = con.prepareStatement("SELECT * FROM Student");
//        sqlRst = stmt2.executeQuery();
//        while (sqlRst.next()) {
//            User temp = new Student();
//            temp.id = sqlRst.getInt("id");
//            temp.fullName = sqlRst.getString("first_name")+sqlRst.getString("last_name");
//            result.add(temp);
//        }
//
//        closeDatasource();
//        return result;
        try(Connection connection= SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt1=connection.prepareStatement("select * from project2.instructor;");
            PreparedStatement stmt2=connection.prepareStatement("select * from project2.student;")) {
            List<User> result = new ArrayList<>();
            ResultSet R1 = stmt1.executeQuery();
            ResultSet R2 = stmt2.executeQuery();
            while (R1.next()) {
                User temp = new Instructor();
                temp.id = R1.getInt(1);
                String first_name = R1.getString(2);
                String last_name = R1.getString(3);
                temp.fullName=Function.getFullName(first_name,last_name);
                result.add(temp);
            }
            while (R2.next()) {
                User temp = new Student();
                temp.id = R1.getInt(1);
                String first_name = R2.getString(2);
                String last_name = R2.getString(3);
                temp.fullName=Function.getFullName(first_name,last_name);
                result.add(temp);
            }
            R1.close();
            R2.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User getUser(int userId) {
//        openDatabase();
//        PreparedStatement stmt = con.prepareStatement("SELECT * FROM Instructor where id=?");
//        stmt.setInt(1, userId);
//        ResultSet sqlRst = stmt.executeQuery();
//        while (sqlRst.next()) {
//            Instructor result = new Instructor();
//            result.id = sqlRst.getInt("id");
//            result.fullName = sqlRst.getString("first_name")+sqlRst.getString("last_name");
//            closeDatasource();
//            return result;
//        }
//
//        stmt = con.prepareStatement("SELECT * FROM Student where id=?");
//        stmt.setInt(1, userId);
//        sqlRst = stmt.executeQuery();
//        while (sqlRst.next()) {
//            Student result = new Student();
//            result.id = sqlRst.getInt("id");
//            result.fullName = sqlRst.getString("first_name")+sqlRst.getString("last_name");
//            closeDatasource();
//            return result;
//        }
//        return null;
        try(Connection connection= SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt1=connection.prepareStatement("select *from project2.instructor where id=?;");
            PreparedStatement stmt2=connection.prepareStatement("select *from project2.student where id=?;")) {
            stmt1.setInt(1, userId);
            stmt2.setInt(1, userId);
            ResultSet R1 = stmt1.executeQuery();
            ResultSet R2 = stmt2.executeQuery();
            User result = null;
            if (R1.next()) {
                result = new Instructor();
                result.id = R1.getInt("id");
                String first_name = R1.getString(2);
                String lase_name = R1.getString(3);
                result.fullName=Function.getFullName(first_name,lase_name);
            }
            else if (R2.next()) {
                result = new Student();
                result.id = R2.getInt("id");
                String first_name = R2.getString(2);
                String lase_name = R2.getString(3);
                result.fullName=Function.getFullName(first_name,lase_name);
            }
            else
                throw new EntityNotFoundException();
            R1.close();
            R2.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
