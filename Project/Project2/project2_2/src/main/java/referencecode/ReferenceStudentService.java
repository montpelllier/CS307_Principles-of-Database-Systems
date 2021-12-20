package referencecode;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.grade.Grade;
import cn.edu.sustech.cs307.dto.prerequisite.AndPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.CoursePrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.OrPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.Prerequisite;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.StudentService;

import javax.annotation.Nullable;
import java.sql.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import static referencecode.Function.*;

public class ReferenceStudentService implements StudentService {

    private EntityNotFoundException entityNotFoundException = new EntityNotFoundException();
    private IntegrityViolationException integrityViolationException = new IntegrityViolationException();

    @Override
    public void addStudent(int userId, int majorId, String firstName, String lastName, Date enrolledDate) {
        String sql = "INSERT INTO project2.Student(id, first_name, last_name, enrolledDate, major_id) VALUES (?,?,?,?,?)";
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.setDate(4, enrolledDate);
            pstmt.setInt(5, majorId);
            pstmt.execute();
        } catch (SQLException e) {

            e.printStackTrace();
            throw integrityViolationException;
        }
    }

    @Override
    public List<CourseSearchEntry> searchCourse(int studentId, int semesterId, @Nullable String searchCid, @Nullable String searchName, @Nullable String searchInstructor, @Nullable DayOfWeek searchDayOfWeek, @Nullable Short searchClassTime, @Nullable List<String> searchClassLocations, CourseType searchCourseType, boolean ignoreFull, boolean ignoreConflict, boolean ignorePassed, boolean ignoreMissingPrerequisites, int pageSize, int pageIndex) {
        List<CourseSearchEntry> searchResult = new Vector<>();
//写个鬼
        return searchResult;
    }

    @Override
    public EnrollResult enrollCourse(int studentId, int sectionId) {//缺少先修课判断
        String CourseSection = "insert into student_coursesection (student_id, coursesection_id, grade) values (?,?,?)";
        String isFull = "select leftcapacity from coursesection where id = ?";

        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmtCourseSection = connection.prepareStatement(CourseSection);
             PreparedStatement findCourseSection = connection.prepareStatement("select * from project2.course_section where id = ?;");
             PreparedStatement searchStudentSection = connection.prepareStatement("select * from project2.student_course_section where student_id =?;");
             PreparedStatement searchGrade = connection.prepareStatement("select scs.grade\n" +
                     "from project2.student_course_section scs\n" +
                     "join project2.course_section cs on cs.id = scs.course_section_id\n" +
                     "where cs.course_id= ? and student_id= ?;");
             PreparedStatement getSectionInSemester = connection.prepareStatement("select cs.id\n" +
                     "from project2.student_course_section scs \n" +
                     "join project2.course_section cs on scs.course_section_id = cs.id\n" +
                     "join project2.course_section_class csc on cs.id = csc.course_section_id\n" +
                     "where scs.student_id= ? and cs.semester_id= ?;");
             PreparedStatement courseConflict = connection.prepareStatement("select scs.grade \n" +
                     "from project2.student_course_section scs\n" +
                     "join project2.course_section cs on cs.id = scs.course_section_id\n" +
                     "where course_id= ? and student_id = ? and semester_id= ?;");
             PreparedStatement update = connection.prepareStatement("update course_section \n" +
                     "set left_capacity=left_capacity-1 where id= ?;")
        ) {

        } catch (SQLException e) {

        }

        return null;
    }

    @Override
    public void dropCourse(int studentId, int sectionId) throws IllegalStateException {
        String getGrade = "select grade from project2.student_coursesection where coursesection_id = ? and student_id = ?";
        String update = "update project2.CourseSection set leftCapacity = leftCapacity + 1 where id = ?";
        String StudentCourseSection = "delete from project2.student_coursesection where coursesection_id = ? and student_id = ?";
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmtGetGrade = connection.prepareStatement(getGrade);
             PreparedStatement pstmtStudentCourseSection = connection.prepareStatement(StudentCourseSection);
             PreparedStatement pstmtUpdate = connection.prepareStatement(update)
        ) {
            pstmtGetGrade.setInt(1,sectionId);
            pstmtGetGrade.setInt(2,studentId);
            ResultSet rs = pstmtGetGrade.executeQuery();
            if (rs.next()) throw new IllegalStateException();
            else {
                pstmtStudentCourseSection.setInt(1,sectionId);
                pstmtStudentCourseSection.setInt(2,studentId);
                pstmtStudentCourseSection.execute();
                pstmtUpdate.setInt(1,sectionId);
                pstmtUpdate.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addEnrolledCourseWithGrade(int studentId, int sectionId, @Nullable Grade grade) {


    }

    @Override
    public void setEnrolledCourseGrade(int studentId, int sectionId, Grade grade) {

    }

    @Override
    public Map<Course, Grade> getEnrolledCoursesAndGrades(int studentId, @Nullable Integer semesterId) {

        return null;
    }

    @Override
    public CourseTable getCourseTable(int studentId, Date date) {
        CourseTable ct = new CourseTable();
        //String

        return ct;
    }

    @Override//dfs
    public boolean passedPrerequisitesForCourse(int studentId, String courseId) {
        testStudent(studentId);
        testCourse(courseId);
        return passedPrerequisitesForCourseWithoutCheck(studentId,courseId);
    }

    private boolean passedPrerequisitesForCourseWithoutCheck(int studentId, String courseId){
        try (Connection connection= SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmt=connection.prepareStatement("select scs.grade" +
                     "from project2.studentcoursesection scs join project2.coursesection cs on scs.section_id = cs.id " +
                     "where cs.course_id= ? and scs.student_id= ?;")){
            Prerequisite prerequisite=getPrerequisite(courseId);
            stmt.setInt(2,studentId);
            return checkPassPrerequisiteDFS(stmt,prerequisite);
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    private boolean checkPassPrerequisiteDFS(PreparedStatement stmt,Prerequisite node)throws SQLException{
        if (node==null){
            return true;
        }
        String thisType=node.when(new Prerequisite.Cases<String>() {
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
        if (thisType.equals("AND")){
            AndPrerequisite and=(AndPrerequisite) node;
            int size=and.terms.size();
            int flag=0;
            for (int i=0;i<size;i++){
                if (checkPassPrerequisiteDFS(stmt,and.terms.get(i))){
                    flag++;
                }
            }
            return flag == size;
        }else if (thisType.equals("OR")){
            OrPrerequisite or=(OrPrerequisite) node;
            int size=or.terms.size();
            for (int i=0;i<size;i++){
                if (checkPassPrerequisiteDFS(stmt,or.terms.get(i))){
                    return true;
                }
            }
            return false;
        }else {
            stmt.setString(1,thisType);
            ResultSet rs=stmt.executeQuery();
            boolean pass=false;
            while (rs.next()){
                pass=pass(rs.getString(1));
            }
            rs.close();
            return pass;
        }
    }

    public Prerequisite getPrerequisite(String courseId){//通过courseId获得先修课的根节点
        try (Connection connection= SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement stmtGetRoot=connection.prepareStatement("select * from project2.prerequisite " +
                     "where course_id= ? order by node_id desc limit 1;");
             PreparedStatement stmtDFS=connection.prepareStatement("select * from project2.prerequisite where node_id= ? ;")){
            stmtGetRoot.setString(1,courseId);
            ResultSet rs=stmtGetRoot.executeQuery();
            Prerequisite prerequisite=null;
            if (rs.next()){
                int nodeId=rs.getInt(2);
                prerequisite=getPrerequisiteDFS(stmtDFS,nodeId);
            }
            rs.close();
            return prerequisite;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    private Prerequisite getPrerequisiteDFS(PreparedStatement stmtDFS,int node)throws SQLException{
        stmtDFS.setInt(1,node);
        ResultSet rs=stmtDFS.executeQuery();
        if (rs.next()){
            String value=rs.getString(3);
            String child=rs.getString(4);
            List<Integer> childList=cast(child);
            int size=childList.size();
            if (value.equals("none")){
                return null;
            }else if (value.equals("and")){
                List<Prerequisite> prerequisiteList=new ArrayList<>();
                for (int i=0;i<size;i++){
                    prerequisiteList.add(getPrerequisiteDFS(stmtDFS,childList.get(i)));
                }
                return new AndPrerequisite(prerequisiteList);
            }else if (value.equals("or")){
                List<Prerequisite> prerequisiteList=new ArrayList<>();
                for (int i=0;i<size;i++){
                    prerequisiteList.add(getPrerequisiteDFS(stmtDFS,childList.get(i)));
                }
                return new OrPrerequisite(prerequisiteList);
            }else {
                return new CoursePrerequisite(value);
            }
        }
        rs.close();
        return null;
    }

    private List<Integer> cast(String value){
        String[] split = value.split(",");
        List<Integer> list=new ArrayList<>();
        for (int i=0;i<split.length;i++){
            list.add(Integer.parseInt(split[i]));
        }
        return list;
    }

    private boolean pass(String grade){
        if (grade.equals("PASS")){
            return true;
        }else if (grade.equals("FAIL") || grade.equals("NULL")){
            return false;
        }else {//百分制
            int grd=Integer.parseInt(grade);
            return grd >= 60;
        }
    }

    private boolean PassedCourse(int studentId, String courseId) {
        String sql = "select grade from project2.course join project2.CourseSection on project2.course.id = project2.coursesection.course_id" +
                "join project2.Student_CourseSection on coursesection.id = student_coursesection.coursesection_id" +
                "where project2.course.id = ? and student_id = ?";
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getInt("grade") >= 60) return true;
                else return false;
            } else return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Major getStudentMajor(int studentId) {
        String sql = "select major_id from project2.student where id = ?";
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int majorId = rs.getInt("major_id");
                ReferenceMajorService ms = new ReferenceMajorService();
                return ms.getMajor(majorId);
            } else {
                rs.close();
                throw entityNotFoundException;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}