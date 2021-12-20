package referencecode;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.prerequisite.AndPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.CoursePrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.OrPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.Prerequisite;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.CourseService;

import javax.annotation.Nullable;
import java.sql.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReferenceCourseService implements CourseService {
    @Override
    public void addCourse(String courseId, String courseName, int credit, int classHour, Course.CourseGrading grading, @Nullable Prerequisite prerequisite)  {
        try(Connection connection= SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt=connection.prepareStatement("insert into project2.course " +
                    "(id, name, credit, classhour, grading) values ( ? , ? , ? , ? , ? );");
            PreparedStatement stmtPrerequisite=connection.prepareStatement("insert into project2.prerequisite " +
                    "(course_id, value, children) values ( ? , ? , ? );");
            PreparedStatement stmtGetNID=connection.prepareStatement("select node_id from project2.prerequisite " +
                    "where course_id=? and value=? and children=?;")){
            stmt.setString(1,courseId);
            stmt.setString(2,courseName);
            stmt.setInt(3,credit);
            stmt.setInt(4,classHour);
            stmt.setString(5,Function.getGradingByCourseGrading(grading));
            stmtPrerequisite.setString(1,courseId);
            stmtGetNID.setString(1,courseId);
            stmt.executeUpdate();
            addCourseDFS(prerequisite,stmtPrerequisite,stmtGetNID);
        }catch (SQLException e){
            throw new IntegrityViolationException(e.getMessage());
        }
    }

    @Override
    public int addCourseSection(String courseId, int semesterId, String sectionName, int totalCapacity) {
        try(Connection connection= SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt=connection.prepareStatement("insert into project2.coursesection " +
                    "    (name, totalcapacity, leftcapacity, course_id, semester_id) values ( ? , ? , ? , ? , ? );");
            PreparedStatement stmt_get=connection.prepareStatement("select * from project2.coursesection " +
                    "where course_id=? and semester_id=? and name=? ;")){
            stmt.setString(1,sectionName);
            stmt.setInt(2,totalCapacity);
            stmt.setInt(3,totalCapacity);
            stmt.setString(4,courseId);
            stmt.setInt(5,semesterId);
            stmt_get.setString(1,courseId);
            stmt_get.setInt(2,semesterId);
            stmt_get.setString(3,sectionName);
            stmt.executeUpdate();
            ResultSet R = stmt_get.executeQuery();
            int result = 0;
            if (R.next()){
                result = R.getInt("id");
            }
            R.close();
            return result;
        }catch (SQLException e){
            throw new IntegrityViolationException(e.getMessage());
        }
    }

    @Override
    public int addCourseSectionClass(int sectionId, int instructorId, DayOfWeek dayOfWeek, Set<Short> weekList, short classStart, short classEnd, String location) {
        try(Connection connection= SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt=connection.prepareStatement("insert into project2.coursesectionclass " +
                    "(instructor_id, dayofweek, weeklist, classbegin, classend, location, section_id) values ( ? , ? , ? , ? , ? , ? , ? );");
            PreparedStatement stmt_get=connection.prepareStatement("select * from project2.coursesectionclass " +
                    "where instructor_id = ? and dayofweek = ? and weeklist = ? and classbegin = ? and classend = ? and location = ? and section_id = ? ;")) {
            stmt.setInt(1, instructorId);
            stmt.setString(2, Function.dayOfWeekToString(dayOfWeek));
            stmt.setString(3, Function.weekListToString(weekList));
            stmt.setInt(4, classStart);
            stmt.setInt(5, classEnd);
            stmt.setString(6, location);
            stmt.setInt(7, sectionId);

            stmt_get.setInt(1,instructorId);
            stmt_get.setString(2,Function.dayOfWeekToString(dayOfWeek));
            stmt_get.setString(3,Function.weekListToString(weekList));
            stmt_get.setInt(4,classStart);
            stmt_get.setInt(5,classEnd);
            stmt_get.setString(6,location);
            stmt_get.setInt(7,sectionId);

            ResultSet R = stmt_get.executeQuery();

            stmt.executeUpdate();
            int result = 0;
            if (R.next()) {
                result = R.getInt("id");
            }
            R.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void removeCourse(String courseId) {
        try(Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt_get=connection.prepareStatement("select *from project2.course where id= ?;");
            PreparedStatement stmt_getC =connection.prepareStatement("select id from project2.coursesection where course_id= ? ;");
            PreparedStatement stmt_c = connection.prepareStatement("delete from project2.course where id = ?;");
            PreparedStatement stmt_cm=connection.prepareStatement("delete from project2.course_major where course_id = ?;");
            PreparedStatement stmt_p =connection.prepareStatement("delete from project2.prerequisite where course_id = ?;")) {
            stmt_get.setString(1, courseId);
            stmt_getC.setString(1, courseId);
            stmt_c.setString(1, courseId);
            stmt_cm.setString(1, courseId);
            stmt_p.setString(1, courseId);
            ResultSet R1 = stmt_get.executeQuery();
            if (R1.next()) {
                ResultSet R2 = stmt_getC.executeQuery();
                while (R2.next())
                    removeCourseSection(R2.getInt("id"));
                R2.close();
                stmt_cm.executeUpdate();
                stmt_p.executeUpdate();
                stmt_c.executeUpdate();
            }
            else {
                R1.close();
                throw new EntityNotFoundException();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeCourseSection(int sectionId) {
        try(Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt_get=connection.prepareStatement("select *from project2.coursesection where id=?;");
            PreparedStatement stmt = connection.prepareStatement("delete from project2.coursesection where id=?;");
            PreparedStatement stmt_csc = connection.prepareStatement("delete from project2.coursesectionclass where section_id=?;");
            PreparedStatement stmt_scs = connection.prepareStatement("delete from project2.student_coursesection where coursesection_id=?;")) {
            stmt.setInt(1, sectionId);
            stmt_get.setInt(1, sectionId);
            stmt_csc.setInt(1, sectionId);
            stmt_scs.setInt(1, sectionId);
            ResultSet R = stmt_get.executeQuery();
            if (R.next()) {
                stmt_csc.executeUpdate();
                stmt_scs.executeUpdate();
                stmt.executeUpdate();
                R.close();
            }
            else {
                R.close();
                throw new EntityNotFoundException();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeCourseSectionClass(int classId) {
        try(Connection connection = SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt = connection.prepareStatement("delete from project2.coursesectionclass where id=?;");
            PreparedStatement stmt_get=connection.prepareStatement("select * from project2.coursesectionclass where id=?;")) {
            stmt.setInt(1, classId);
            stmt_get.setInt(1, classId);
            ResultSet R1 = stmt_get.executeQuery();
            if (R1.next())
                stmt.executeQuery();
            else
                throw new EntityNotFoundException();
            R1.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Course> getAllCourses() {
        try(Connection connection= SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt=connection.prepareStatement("select * from project2.course;")) {
            ResultSet R = stmt.executeQuery();
            List<Course> result = new ArrayList<>();
            while (R.next()) {
                Course temp = new Course();
                temp.id = R.getString("id");
                temp.name = R.getString("name");
                temp.credit = R.getInt("credit");
                temp.classHour = R.getInt("classhour");
                temp.grading = Function.getGradingByString(R.getString("grading"));
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
    public List<CourseSection> getCourseSectionsInSemester(String courseId, int semesterId) {
        testCourse(courseId);
        testSemester(semesterId);
        try(Connection connection= SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt=connection.prepareStatement("select * from project2.coursesection where semester_id=? and course_id=? ;")) {
            stmt.setInt(1, semesterId);
            stmt.setString(2, courseId);
            ResultSet R = stmt.executeQuery();
            List<CourseSection> result = new ArrayList<>();
            while (R.next()) {
                CourseSection temp = new CourseSection();
                temp.id = R.getInt("id");
                temp.name = R.getString(("name"));
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

    @Override
    public Course getCourseBySection(int sectionId) {
        try(Connection connection= SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt=connection.prepareStatement("select c.id,c.name,c.credit,c.classhour,c.grading " +
                    "from project2.course c join project2.coursesection cs on c.id = cs.course_id where cs.id=?;")) {
            stmt.setInt(1, sectionId);
            ResultSet R = stmt.executeQuery();
            Course result = new Course();
            if (R.next()) {
                result.id = R.getString("id");
                result.name = R.getString("name");
                result.classHour = R.getInt("classhour");
                result.credit = R.getInt("credit");
                result.grading = Function.getGradingByString(R.getString("grading"));
            }
            else {
                throw new EntityNotFoundException();
            }
            R.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<CourseSectionClass> getCourseSectionClasses(int sectionId) {
        testSection(sectionId);
        try(Connection connection= SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt=connection.prepareStatement(
                    "select * from project2.coursesectionclass csc join project2.instructor i on csc.instructor_id = i.id where section_id=? ;")) {
            stmt.setInt(1, sectionId);
            ResultSet R = stmt.executeQuery();
            List<CourseSectionClass> result = new ArrayList<>();
            while(R.next()) {
                CourseSectionClass temp = new CourseSectionClass();
                Instructor instructor=new Instructor();
                instructor.id=R.getInt(9);
                instructor.fullName=Function.getFullName(R.getString(10),R.getString(11));
                temp.id=R.getInt(1);
                String dayOfWeek=R.getString(3);
                temp.dayOfWeek=Function.getDayOfWeek(dayOfWeek);
                temp.weekList=Function.getWeekList(R.getString(4));
                temp.classBegin=R.getShort(5);
                temp.classEnd=R.getShort(6);
                temp.location=R.getString(7);
                temp.instructor=instructor;
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
    public CourseSection getCourseSectionByClass(int classId) {
//        openDatabase();
//        PreparedStatement stmt = con.prepareStatement("SELECT * FROM coursesectionclass where id=?");
//        stmt.setInt(1, classId);
//        ResultSet sqlRst = stmt.executeQuery();
//        int need = sqlRst.getInt("sectionid");
//
//        stmt = con.prepareStatement("SELECT * FROM coursesection where id=?");
//        stmt.setInt(1, need);
//        sqlRst = stmt.executeQuery();
//        CourseSection result = new CourseSection();
//
//        result.id = sqlRst.getInt("id");
//        result.leftCapacity = sqlRst.getInt("leftcapacity");
//        result.totalCapacity = sqlRst.getInt("totalcapacity");
//        result.name = sqlRst.getString("name");
//
//        closeDatasource();
//        return result;
        try(Connection connection= SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt=connection.prepareStatement("select cs.id,cs.name, cs.totalcapacity,cs.leftcapacity " +
                    "from project2.coursesection cs join project2.coursesectionclass csc on cs.id = csc.coursesection_id where csc.id=? ;")) {
            stmt.setInt(1, classId);
            ResultSet R = stmt.executeQuery();
            if (R.next()) {
                CourseSection result = new CourseSection();
                result.id = R.getInt("id");
                result.name = R.getString("name");
                result.totalCapacity = R.getInt("totalcapacity");
                result.leftCapacity = R.getInt("leftcapacity");
                R.close();
                return result;
            }
            else {
                R.close();
                throw new EntityNotFoundException();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Student> getEnrolledStudentsInSemester(String courseId, int semesterId) {
        testCourse(courseId);
        testSemester(semesterId);
        try(Connection connection= SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt=connection.prepareStatement("select s.id,s.first_name,s.last_name,s.enrolleddate, m.id,m.name,d.id,d.name " +
                    "from project2.student_coursesection scs " +
                    "    join project2.student s on scs.student_id = s.id " +
                    "    join project2.course_section cs on scs.coursesection_id = cs.id " +
                    "    join project2.major m on s.major_id = m.id " +
                    "    join project2.department d on d.id = m.department_id " +
                    "where cs.course_id=? and cs.semester_id=? ;")) {
            stmt.setString(1, courseId);
            stmt.setInt(2, semesterId);
            ResultSet R = stmt.executeQuery();
            List<Student> result = new ArrayList<>();
            while (R.next()) {
                Student temp = new Student();
                Department department=new Department();
                Major major=new Major();
                temp.id = R.getInt("id");
                temp.fullName = Function.getFullName(R.getString("first_name"), R.getString("last_name"));
                temp.enrolledDate = R.getDate("enrolleddate");
                department.id=R.getInt(7);
                department.name=R.getString(8);
                major.id=R.getInt(5);
                major.name=R.getString(6);
                major.department=department;
                temp.major=major;
                result.add(temp);
            }
            R.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void testCourse(String courseId) {
        try(Connection connection= SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt=connection.prepareStatement("select *from project2.course where id=?;")){
            stmt.setString(1,courseId);
            ResultSet rs=stmt.executeQuery();
            if (!rs.next()){
                rs.close();
                throw new EntityNotFoundException();
            }
            rs.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void testSection(int sectionId){
        try(Connection connection= SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt=connection.prepareStatement("select *from project2.course_section where id=?;")){
            stmt.setInt(1,sectionId);
            ResultSet rs=stmt.executeQuery();
            if (!rs.next()){
                rs.close();
                throw new EntityNotFoundException();
            }
            rs.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void testSemester(int semesterId){
        try(Connection connection= SQLDataSource.getInstance().getSQLConnection();
            PreparedStatement stmt=connection.prepareStatement("select *from project2.semester where id=?;")){
            stmt.setInt(1,semesterId);
            ResultSet rs=stmt.executeQuery();
            if (!rs.next()){
                rs.close();
                throw new EntityNotFoundException();
            }
            rs.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    private int addCourseDFS(Prerequisite prerequisite,PreparedStatement stmt,PreparedStatement stmtGetNID) throws SQLException{
        if (prerequisite==null) {
            stmt.setString(2,"none");
            stmt.setString(3,"-2");
            stmt.executeUpdate();
            stmtGetNID.setString(2,"none");
            stmtGetNID.setString(3,"-2");
            ResultSet rs=stmtGetNID.executeQuery();
            int nid=0;
            if (rs.next()){
                nid=rs.getInt(1);
            }
            rs.close();
            return nid;
        }

        String thisType=prerequisite.when(new Prerequisite.Cases<String>() {
            @Override
            public String match(AndPrerequisite self) {
                return "AND";
            }
            @Override
            public String match(OrPrerequisite self) {
                return "OR";
            }
            @Override
            public String match(CoursePrerequisite self) {
                return self.courseID;
            }
        });

        int nid = 0;
        if (thisType.equals("OR")){
            OrPrerequisite or=(OrPrerequisite) prerequisite;

            int size=or.terms.size();
            StringBuilder array=new StringBuilder();
            for (int i=0;i<size-1;i++){
                array.append(addCourseDFS(or.terms.get(i),stmt,stmtGetNID)).append(",");
            }
            array.append(addCourseDFS(or.terms.get(size-1),stmt,stmtGetNID));
            stmt.setString(2,"or");
            stmt.setString(3,array.toString());
            stmt.executeUpdate();
            stmtGetNID.setString(2,"or");
            stmtGetNID.setString(3,array.toString());
            ResultSet rs=stmtGetNID.executeQuery();
            if (rs.next()){
                nid=rs.getInt(1);
            }
            rs.close();
            return nid;
        }
        else if (thisType.equals("AND")){
            AndPrerequisite and=(AndPrerequisite) prerequisite;
            int size=and.terms.size();
            StringBuilder array= new StringBuilder();
            for (int i=0;i<size-1;i++){
                array.append(addCourseDFS(and.terms.get(i), stmt, stmtGetNID)).append(",");
            }
            array.append(addCourseDFS(and.terms.get(size - 1), stmt, stmtGetNID));
            stmt.setString(2,"and");
            stmt.setString(3, array.toString());
            stmt.executeUpdate();
            stmtGetNID.setString(2,"and");
            stmtGetNID.setString(3, array.toString());
            ResultSet rs=stmtGetNID.executeQuery();
            if (rs.next()){
                nid=rs.getInt(1);
            }
            rs.close();
            return nid;
        }
        else {
            CoursePrerequisite course=(CoursePrerequisite) prerequisite;
            stmt.setString(2,course.courseID);
            stmt.setString(3,"-1");
            stmt.executeUpdate();
            stmtGetNID.setString(2,course.courseID);
            stmtGetNID.setString(3,"-1");
            ResultSet rs=stmtGetNID.executeQuery();
            if (rs.next()){
                nid=rs.getInt(1);
            }
            rs.close();
        }

        return nid;
    }
}
