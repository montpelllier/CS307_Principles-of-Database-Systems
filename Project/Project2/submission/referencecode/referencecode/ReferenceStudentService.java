package referencecode;

import cn.edu.sustech.cs307.database.SQLDataSource;
import cn.edu.sustech.cs307.dto.*;
import cn.edu.sustech.cs307.dto.grade.Grade;
import cn.edu.sustech.cs307.dto.grade.HundredMarkGrade;
import cn.edu.sustech.cs307.dto.grade.PassOrFailGrade;
import cn.edu.sustech.cs307.dto.prerequisite.AndPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.CoursePrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.OrPrerequisite;
import cn.edu.sustech.cs307.dto.prerequisite.Prerequisite;
import cn.edu.sustech.cs307.exception.EntityNotFoundException;
import cn.edu.sustech.cs307.exception.IntegrityViolationException;
import cn.edu.sustech.cs307.service.StudentService;

import javax.annotation.Nullable;
import java.sql.*;
import java.sql.Date;
import java.time.DayOfWeek;
import java.util.*;

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
        String CourseSection = "insert into project2.student_coursesection (student_id, coursesection_id, grade) values (?,?,?)";
        String getCapacity = "select leftcapacity from project2.coursesection where id = ?";
        String getGrade = "select grade from project2.Student_CourseSection where student_id = ? and coursesection_id=?";
        String Update = "update project2.coursesection set leftCapacity = leftCapacity-1 where id = ?";

        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmtCourseSection = connection.prepareStatement(CourseSection);
             PreparedStatement pstmtGetCapacity = connection.prepareStatement(getCapacity);
             PreparedStatement pstmtgetGrade = connection.prepareStatement(getGrade);
             PreparedStatement pstmtUpdate = connection.prepareStatement(Update);

        ) {
            boolean isLeft = false;
            int grade;
            pstmtGetCapacity.setInt(1,sectionId);
            ResultSet rs = pstmtGetCapacity.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) > 0) isLeft = true;
                pstmtgetGrade.setInt(1,studentId);
                pstmtgetGrade.setInt(2,sectionId);
                rs = pstmtgetGrade.executeQuery();
                if (rs.next()){
                    grade = rs.getInt(1);
                    if (grade >= 60) return EnrollResult.ALREADY_PASSED;
                }

            }else return EnrollResult.COURSE_NOT_FOUND;
            if (isLeft){
                pstmtCourseSection.setInt(1,studentId);
                pstmtCourseSection.setInt(2,sectionId);
                pstmtCourseSection.setInt(3,-1);//
                pstmtCourseSection.execute();

                pstmtUpdate.setInt(1,sectionId);
                pstmtUpdate.execute();
                return EnrollResult.SUCCESS;
            }else return EnrollResult.COURSE_IS_FULL;


        } catch (SQLException e) {
            e.printStackTrace();
            return EnrollResult.UNKNOWN_ERROR;
        }
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
            if (rs.next()) {
                if (rs.getInt(1)!=-1) throw new IllegalStateException();
            }
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
        String Student_CourseSection = "insert into project2.Student_CourseSection(student_id, coursesection_id, grade) values (?,?,?)";
        String getGrading = "select grading from project2.course join project2.CourseSection on project2.Course.id = project2.CourseSection.course_id " +
                "where project2.CourseSection.id = ?";
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmtGetGrading = connection.prepareStatement(getGrading);
             PreparedStatement pstmtStudent_CourseSection = connection.prepareStatement(Student_CourseSection)
        ) {
            pstmtGetGrading.setInt(1,sectionId);
            ResultSet rs = pstmtGetGrading.executeQuery();
            if (rs.next()) {
                if (grade != null) {
                    String grading = rs.getString(1);
                    String thisGrading = grade.when(new Grade.Cases<String>() {
                        @Override
                        public String match(PassOrFailGrade self) {
                            return self.name();
                        }

                        @Override
                        public String match(HundredMarkGrade self) {
                            return Short.toString(self.mark);
                        }
                    });

                    if (grading.equals("pf")) {
                        if (thisGrading.equals("PASS")) pstmtStudent_CourseSection.setInt(3, 60);
                        else if (thisGrading.equals("FAIL")) pstmtStudent_CourseSection.setInt(3, 0);
                        else throw integrityViolationException;
                    } else if (grading.equals("hm")) {
                        if (thisGrading.equals("PASS") || thisGrading.equals("FAIL")) {
                            rs.close();
                            throw integrityViolationException;
                        } else pstmtStudent_CourseSection.setInt(3, Integer.parseInt(thisGrading));
                    }
                }else pstmtStudent_CourseSection.setInt(3,-1);
            } else throw integrityViolationException;
            pstmtStudent_CourseSection.setInt(1,studentId);
            pstmtStudent_CourseSection.setInt(2,sectionId);

            pstmtStudent_CourseSection.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw integrityViolationException;
        }
    }

    @Override
    public void setEnrolledCourseGrade(int studentId, int sectionId, Grade grade) {
        String Student_CourseSection = "update project2.Student_CourseSection set grade = ? where student_id = ? and coursesection_id = ?";
        String getGrading = "select grading from project2.course join project2.CourseSection on project2.Course.id = project2.CourseSection.course_id " +
                "where project2.CourseSection.id = ?";
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmtGetGrading = connection.prepareStatement(getGrading);
             PreparedStatement pstmtStudent_CourseSection = connection.prepareStatement(Student_CourseSection)
        ) {
            pstmtGetGrading.setInt(1,sectionId);
            ResultSet rs = pstmtGetGrading.executeQuery();
            if (rs.next()) {
                if (grade != null) {
                    String grading = rs.getString(1);
                    String thisGrading = grade.when(new Grade.Cases<String>() {
                        @Override
                        public String match(PassOrFailGrade self) {
                            return self.name();
                        }

                        @Override
                        public String match(HundredMarkGrade self) {
                            return Short.toString(self.mark);
                        }
                    });

                    if (grading.equals("pf")) {
                        if (thisGrading.equals("PASS")) pstmtStudent_CourseSection.setInt(1, 60);
                        else if (thisGrading.equals("FAIL")) pstmtStudent_CourseSection.setInt(1, 0);
                        else throw integrityViolationException;
                    } else if (grading.equals("hm")) {
                        if (thisGrading.equals("PASS") || thisGrading.equals("FAIL")) {
                            rs.close();
                            throw integrityViolationException;
                        } else pstmtStudent_CourseSection.setInt(1, Integer.parseInt(thisGrading));
                    }
                }else pstmtStudent_CourseSection.setInt(1,-1);
            } else throw integrityViolationException;
            pstmtStudent_CourseSection.setInt(2,studentId);
            pstmtStudent_CourseSection.setInt(3,sectionId);

            pstmtStudent_CourseSection.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw integrityViolationException;
        }
    }

    @Override
    public Map<Course, Grade> getEnrolledCoursesAndGrades(int studentId, @Nullable Integer semesterId) {
        Map<Course, Grade> courseGradeMap = new HashMap<>();
        String sql = "select grade, grading, course_id from project2.course join project2.CourseSection on course.id = coursesection.course_id " +
                "join project2.student_coursesection on coursesection.id = student_coursesection.coursesection_id " +
                "where student_id = ? and semester_id = ?";
        String sqlNULL = "select grade, grading, course_id from project2.course join project2.CourseSection on course.id = coursesection.course_id " +
                "join project2.student_coursesection on coursesection.id = student_coursesection.coursesection_id " +
                "where student_id = ?";
        try (Connection connection = SQLDataSource.getInstance().getSQLConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             PreparedStatement pstmtNULL = connection.prepareStatement(sqlNULL);
        ) {
            ResultSet rs;
            Grade temp_g;
            Course temp_c;
            int c_id, grade;
            String grading;
            ReferenceCourseService cs = new ReferenceCourseService();
            if (semesterId != null) {
                pstmt.setInt(1,studentId);
                pstmt.setInt(2,semesterId);
                rs = pstmt.executeQuery();
            }else {
                pstmtNULL.setInt(1,studentId);
                rs = pstmtNULL.executeQuery();
            }

            while (rs.next()){
                grade = rs.getInt("grade");
                grading = rs.getString("grading");
                c_id = rs.getInt("course_id");
                temp_c = cs.getCourse(c_id);
                if (grade != -1){
                    if (grading.equals("pf")){
                        if (grade==60) temp_g = PassOrFailGrade.PASS;
                        else if (grade==0) temp_g = PassOrFailGrade.FAIL;
                        else throw integrityViolationException;
                    }else temp_g = new HundredMarkGrade((short) grade);
                    courseGradeMap.put(temp_c, temp_g);
                }
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courseGradeMap;
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