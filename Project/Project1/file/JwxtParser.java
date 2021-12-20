import java.nio.file.*;
// TODO: import the json library of your choice

public class JwxtParser {
    // Run as: jshell JwxtParser.java <json file>
    public static void main(String[] args) {
        String content = Files.readString(Path.of(args[0]));
     
		// Gson is an example
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Type type = new TypeToken<List<Course>>(){}.getType();
        List<Course> courses=gson.fromJson(content,  type);
   
        System.out.println(gson.toJson(courses));
		
		// or ...
        // ObjectMapper mapper = new ObjectMapper();
    }
}

class Course {
     // TODO:
    private int totalCapacity;
    private String courseId;
    private String prerequisite;
    private String teacher;
    private ClassList[] classList;
}

class ClassList {
	// TODO: define data-class as the json structure

    private int[] weekList;
    private String location;
    private String classTime;
    
}
