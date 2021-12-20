package referencecode;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.Department;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.DepartmentService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReferenceDepartmentService implements DepartmentService {

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
    public synchronized int addDepartment(String name) {
        try(Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = connection.prepareStatement("insert into project2.department(name) values (?);");
            PreparedStatement stmtGet=connection.prepareStatement("select id from project2.department where name=?;")) {
            List<Department> de = getAllDepartments();
            int id=0;
            for(int i=0;i<id;i++){
                if (de.get(i).name.equals(name))
                    throw new IntegrityViolationException();
            }
            stmt.setString(1,name);
            stmtGet.setString(1,name);
            stmt.executeUpdate();
            ResultSet R=stmtGet.executeQuery();
            if (R.next()){
                id=R.getInt(1);
            }
            R.close();
            return id;
        } catch (SQLException e) {
            throw new IntegrityViolationException(e.getMessage());
        }
//        try(Connection connection = SQLDataSource.getInstance().getSQLConnection();
//            PreparedStatement stmt = connection.prepareStatement("insert into project2.department(name) values (?);");
//            PreparedStatement stmt_check = connection.prepareStatement("select * from project2.department where name=?;");
//            PreparedStatement stmt_get=connection.prepareStatement("select id from project2.department where name=?;"))
    }

    @Override
    public void removeDepartment(int departmentId) {
//        openDatabase();
//        PreparedStatement stmt = con.prepareStatement("DELETE FROM department WHERE id=?");
//        stmt.setInt(1, departmentId);
//        stmt.executeQuery();
//        closeDatasource();
        try(Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = connection.prepareStatement("delete from project2.department where id=? ;");
            PreparedStatement stmt_get = connection.prepareStatement("select id from project2.major where department_id=?;");
            PreparedStatement stmt_check=connection.prepareStatement("select *from project2.department where id=?;")) {
            stmt.setInt(1, departmentId);
            stmt_get.setInt(1, departmentId);
            stmt_check.setInt(1, departmentId);
            ResultSet R1 = stmt_check.executeQuery();
            if (!R1.next())
                throw new EntityNotFoundException();
            R1.close();

            ResultSet rs = stmt_get.executeQuery();
            List<Integer> majorId = new ArrayList<>();
            while (rs.next()) {
                majorId.add(rs.getInt(1));
            }
            rs.close();
            ReferenceMajorService service = new ReferenceMajorService();
            for (int i = 0; i < majorId.size(); i++) {
                service.removeMajor(majorId.get(i));
            }
            stmt.setInt(1, departmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Department> getAllDepartments() {
//        openDatabase();
//        PreparedStatement stmt = con.prepareStatement("SELECT * FROM department");
//        ResultSet sqlRst = stmt.executeQuery();
//        List<Department> result = new ArrayList<>();
//        while (sqlRst.next()) {
//            Department temp = new Department();
//            temp.id = sqlRst.getInt("id");
//            temp.name = sqlRst.getString("name");
//            result.add(temp);
//        }
//        closeDatasource();
//        return result;
        try(Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = connection.prepareStatement("select * from project2.department;")) {
            ResultSet R = stmt.executeQuery();
            List<Department> result = new ArrayList<>();
            while (R.next()) {
                Department temp = new Department();
                temp.id = R.getInt("id");
                temp.name = R.getString("name");
                result.add(temp);
            }
            R.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Department getDepartment(int departmentId) {
//        openDatabase();
//        PreparedStatement stmt = con.prepareStatement("SELECT * FROM department where id=?");
//        stmt.setInt(1, departmentId);
//        ResultSet sqlRst = stmt.executeQuery();
//        Department result = new Department();
//        result.id = sqlRst.getInt("id");
//        result.name = sqlRst.getString("name");
//        closeDatasource();
//        return result;
        try(Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = connection.prepareStatement("select*from project2.department where id=?;")) {
            ResultSet R = stmt.executeQuery();
            if (R.next()) {
                Department result = new Department();
                result.id = R.getInt("id");
                result.name = R.getString("name");
                R.close();
                return result;
            }
            else {
                throw new EntityNotFoundException();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
