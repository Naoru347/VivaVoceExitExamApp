import java.sql.*;
import java.util.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;
import com.google.gson.*;

//For security/encapsualtion purposes, could modify to private vars
//w/ public getters/setters.
class UserData {
    String userid;
    String username; 
    String password;
    String usertype;
    String firstname;
    String lastname;
    String classid;
    String teacher;
    String teacherid;
    String studentid;
    String modifyStudentID;
    String modifyUserID;
    String modifyStudentClass;
    String questionid;
    String examid;
    String pointspossible;
    String pointsearned;
    String classQuestionID;
    String addQuestionText;
    String addfirstname;
    String addlastname;
    String addusername;
    String addpassword;
    String addusertype;
    String removequestionid;
    String removeclassid;
    String removeexamid;
    String modifyexamid;
    String modifyscore;
    List<String> teacherFeedback;
    List<ExamQuestion> examQuestions;
    List<Integer> examQuestionIDs;
    List<String> examQuestionText;
    List<String> teacherClasses;
    String action;
}

class ExamQuestion {
    String question;
    String feedback;
}

class QuestionData {
    int questionid;
    String question;
}

class ClassLinksData {
    int questionid;
    String classid;
}

public class ExamServlet extends HttpServlet {
     
    static Connection conn;
    static Statement statement;

    String message;
    String outputJson;

    public ExamServlet() { 
        try{
            Class.forName("org.h2.Driver");
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            System.out.println("ExamServlet Database Connection Successful!");

        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in Servlet");
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) {
        try{
            System.out.println("In ExamServlet.doPost()");
            handleRequest(req, res);
        }catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) {
        try{
            System.out.println("In ExamServlet.doGet()");
            handleRequest(req, res);
        }catch(Exception e) {
            e.printStackTrace();
        }
        
    }

    public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException {
        //Create a buffer to accept the request from the broswer
        StringBuffer sbuff = null;
        BufferedReader buffReader = null;
        String inputStr = null;
        buffReader = req.getReader();
        sbuff = new StringBuffer();
        while((inputStr = buffReader.readLine()) != null) {
            sbuff.append(inputStr);
        }

        //JSON browser request received - Parse here
        String jstr = sbuff.toString();
        System.out.println("Recieved: " + jstr);

        UserData userData = new Gson().fromJson(jstr, UserData.class);

        if(userData.action.equals("login")){
            doLogin(userData);
            System.out.println("Response TO browser: " + message);
        }
        if(userData.action.equals("getClassInfo")){
            getClassInfo(userData);
            System.out.println("Response TO browser: " + message);
        }
        if(userData.action.equals("getStudentExamInfo")) {
                getStudentExamInfo(userData);
                getExamQuestions(userData);
                System.out.println("Response TO browser: " + message);
        }
        if(userData.action.equals("getTeacherClass")) {
            getTeacherClass(userData);
        }
        if(userData.action.equals("addStudentToClass")){
            addStudentToClass(userData);
            System.out.println("Response TO browser: " + message);
        }
        if(userData.action.equals("removeStudentFromClass")){
            removeStudentFromClass(userData);
            System.out.println("Response TO browser: " + message);
        }
        if(userData.action.equals("addQuestions")){
            addQuestions(userData);
            System.out.println("Response TO browser: " + message);
        }
        if(userData.action.equals("grabDB")){
            grabUserDB(userData);
            System.out.println("Response TO browser: " + message);
        }
        if(userData.action.equals("adminRemoveUser")){
            adminRemoveUser(userData);
            System.out.println("Response TO browser: " + message);
        }
        if(userData.action.equals("adminAddUser")){
            adminAddUser(userData);
            System.out.println("Response TO browser: " + message);
        }
        if(userData.action.equals("grabQuestionDB")){
            grabQuestionDB(userData);
            System.out.println("Reponse TO broswer: " + message);
        }
        if(userData.action.equals("grabLinkagesDB")){
            grabLinkagesDB(userData);
            System.out.println("Response TO browser: " + message);
        }if(userData.action.equals("adminRemoveLinkages")){
            removeLinkages(userData);
            System.out.println("Response TO browser: " + message);
        }if(userData.action.equals("removeExamRecord")) { 
            removeExamRecord(userData);
            System.out.println("Response TO browser: " + message);
        }if(userData.action.equals("modifyExamRecord")) { 
            modifyExamRecord(userData);
            System.out.println("Response TO browser: " + message);
        }if(userData.action.equals("getStartExamQuestions")) { 
            getStartExamQuestions(userData);
            System.out.println("Response TO browser: " + message);
        }if(userData.action.equals("submitExamInfo")) { 
            submitExamInfo(userData);
            System.out.println("Response TO browser: " + message);
        }

        //Prep servlet response
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        Writer writer = res.getWriter();
        outputJson = message;
        writer.write(outputJson);
        writer.flush();
        System.out.println("Output JSON: " + outputJson + "\n\n");
    }

    public void submitExamInfo(UserData userData) throws SQLException {
        System.out.println("In ExamServlet.submitExamInfo. Attempting to submit exam information.");
    
        try {
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();
    
            // Check if student exists in STUDENT_CLASSES
            String sql = "SELECT COUNT(*) FROM STUDENT_CLASSES WHERE STUDENT_ID = " + Integer.parseInt(userData.studentid) + " AND CLASS_ID = '" + userData.classid + "'";
            ResultSet rs = statement.executeQuery(sql);
            int count = 0;
            if(rs.next()) {
                count = rs.getInt(1);
            }
    
            if(count == 0) {
                // Student not found in STUDENT_CLASSES
                System.out.println("Student ID not found in STUDENT_CLASSES.");
                message = "{\"message\": \"submitExamInfoError\"}";
            } else {
                // Insert new exam record
                sql = "INSERT INTO EXAMS (CLASS_ID, STUDENT_ID, POINTS_POSSIBLE, POINTS_EARNED) VALUES ('" 
                    + userData.classid + "', " + Integer.parseInt(userData.studentid) + ", " 
                    + Integer.parseInt(userData.pointspossible) + ", " 
                    + Integer.parseInt(userData.pointsearned) + ")";
                //Use Statement.RETURN_GENERATED_KEYS to grab auto-incremented EXAM_ID PK
                int examInsertStatus = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
    
                if(examInsertStatus > 0) {
                    // Get the generated exam ID
                    ResultSet generatedKeys = statement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int examId = generatedKeys.getInt(1);
    
                        // Insert into EXAM_QUESTIONS table
                        for(int i = 0; i < userData.examQuestionIDs.size(); i++) {
                            int questionId = userData.examQuestionIDs.get(i);
                            String feedback = userData.teacherFeedback.get(i).replace("'", "''"); // Escape single quotes
    
                            sql = "INSERT INTO EXAM_QUESTIONS (EXAM_ID, QUESTION_ID, TEACHER_FEEDBACK) VALUES (" 
                                + examId + ", " + questionId + ", '" + feedback + "')";
                            statement.executeUpdate(sql);
                        }
    
                        message = "{\"message\": \"submitExamInfoSuccess\"}";
                    }
                } else {
                    message = "{\"message\": \"submitExamInfoError\"}";
                }
            }
    
            rs.close();
            conn.close();
            statement.close();
        } catch(Exception e) {
            e.printStackTrace();
            message = "{\"message\": \"submitExamInfoError\"}";
        }
    }
    

    public void getStartExamQuestions (UserData userData) throws SQLException { 
        System.out.println("In ExamServlet.getStartExamQuestions. Attempting to retrieve random questions for class ID: " + userData.classid);

        //Check if class ID exists
        try{
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();

            String sql = "SELECT COUNT(*) FROM CLASSES WHERE CLASS_ID = '" + userData.classid + "'";
            ResultSet rs = statement.executeQuery(sql);
            int count = 0;
            if(rs.next()){
                count = rs.getInt(1);
            }
            if(count > 0) {
                System.out.println("Class ID found. Fetching questions");
                //"Order by RAND() Limit 5" makes the random selection happen in the
                //database server instead of in the application, saving us server-side steps.
                sql = "SELECT QUESTION_ID FROM QUESTION_CLASSES WHERE CLASS_ID = '" + userData.classid + "' ORDER BY RAND() LIMIT 5";
                rs = statement.executeQuery(sql);
                userData.examQuestionIDs = new ArrayList<>();
                while(rs.next()) { 
                    userData.examQuestionIDs.add(rs.getInt("QUESTION_ID"));
                }
                userData.examQuestionText = new ArrayList<>();
                for(Integer questionID : userData.examQuestionIDs) {
                    sql = "SELECT QUESTION_TEXT FROM QUESTIONS WHERE QUESTION_ID = " + questionID;
                    rs = statement.executeQuery(sql);
                    if(rs.next()) {
                        String questionText = rs.getString("QUESTION_TEXT");
                        userData.examQuestionText.add(questionText);
                    }
                }
                //Make sure we actually found what we think we found
                if(userData.examQuestionIDs.size() > 0 && userData.examQuestionIDs.size() == userData.examQuestionText.size()){
                    System.out.println("Questions successfully retrieved");
                    Gson gson = new Gson();
                    String questionIDsJson = gson.toJson(userData.examQuestionIDs);
                    String questionTextsJson = gson.toJson(userData.examQuestionText);
                    message = "{\"message\": \"getStartExamQuestionsSuccess\", " +
                      "\"questionIds\": " + questionIDsJson + ", " +
                      "\"questionTexts\": " + questionTextsJson + "}";
                }

            }else{
                System.out.println("Class ID not found.");
                message = "{\"message\": \"getStartExamQuestionsError\"}";
            }
            rs.close();
            conn.close();
            statement.close();
        }catch(Exception e) { 
            e.printStackTrace();
            message = "{\"message\": \"getStartExamQuestionsError\"}";
        }
    }

    public void modifyExamRecord(UserData userData) throws SQLException {
        System.out.println("In ExamServlet.updateExamScore. Attempting to update examID=" + userData.modifyexamid + " with new score=" + userData.modifyscore);
        int examIdToUpdate = Integer.parseInt(userData.modifyexamid);
        int newScore = Integer.parseInt(userData.modifyscore);
    
        try {
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();
    
            String sql = "UPDATE EXAMS SET POINTS_EARNED = " + newScore + " WHERE EXAM_ID = " + examIdToUpdate;
            int success = statement.executeUpdate(sql);
            if (success > 0) {
                message = "{\"message\": \"updateExamScoreSuccess\"}";
            } else {
                message = "{\"message\": \"updateExamScoreError\"}";
            }
        } catch (Exception e) {
            e.printStackTrace();
            message = "{\"message\": \"updateExamScoreError\"}";
        } 
    }
    
    public void removeExamRecord(UserData userData) throws SQLException {
        System.out.println("In ExamServlet.removeExamRecord. Attempting to remove examID=" + userData.removeexamid);
        int intRemoveExamID = Integer.parseInt(userData.removeexamid);
        try{
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();

            String sql = "DELETE FROM EXAMS WHERE EXAM_ID = " + intRemoveExamID;
            int success = statement.executeUpdate(sql);
            if(success > 0) { 
                message = "{\"message\": \"removeExamRecordSuccess\"}";
            }else{
            message = "{\"message\": \"removeExamRecordError\"}";
            }
        }catch (Exception e){
            e.printStackTrace();
            message = "{\"message\": \"removeExamRecordError\"}";
        }
    }

    public void removeLinkages(UserData userData) throws SQLException {
        System.out.println("In ExamServlet.removeLinkages. Attempting to remove questionID=" + userData.removequestionid + " for classID=" + userData.removeclassid);
        int intRemoveQuestionID = Integer.parseInt(userData.removequestionid);
        try{
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();

            String sql = "DELETE FROM QUESTION_CLASSES WHERE QUESTION_ID = " + intRemoveQuestionID + " AND CLASS_ID = '" + userData.removeclassid + "'";
            int success = statement.executeUpdate(sql);
            if(success > 0) { 
                message = "{\"message\": \"removeLinkageSuccess\"}";
            }else{
            message = "{\"message\": \"removeLinkageError\"}";
            }
        }catch (Exception e){
            e.printStackTrace();
            message = "{\"message\": \"removeLinkageError\"}";
        }
    }
    public void grabQuestionDB(UserData userData) throws SQLException {
        System.out.println("In ExamServlet.grabQuestionDB. Attempting to retrieve QUESTIONS db data...");
    
        List<QuestionData> questionsList = new ArrayList<>();
    
        try {
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();
    
            String sql = "SELECT QUESTION_ID, QUESTION_TEXT FROM QUESTIONS";
            ResultSet rs = statement.executeQuery(sql);
    
            while (rs.next()) {
                QuestionData question = new QuestionData();
                question.questionid = rs.getInt("QUESTION_ID");
                question.question = rs.getString("QUESTION_TEXT");
                questionsList.add(question);
            }
    
            Gson gson = new Gson();
            String questionsJson = gson.toJson(questionsList);
            message = "{\"message\": \"grabQuestionDBSuccess\", \"questions\": " + questionsJson + "}";
    
            rs.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            message = "{\"message\": \"grabQuestionDBError\"}";
        }
    }

    public void grabLinkagesDB(UserData userData) throws SQLException {
        System.out.println("In ExamServlet.grabLinkagesDB. Attempting to retrieve QUESTION_CLASS db data...");
    
        List<ClassLinksData> linkList = new ArrayList<>();
    
        try {
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();
    
            String sql = "SELECT QUESTION_ID, CLASS_ID FROM QUESTION_CLASSES";
            ResultSet rs = statement.executeQuery(sql);
    
            while (rs.next()) {
                ClassLinksData cld = new ClassLinksData();
                cld.questionid = rs.getInt("QUESTION_ID");
                cld.classid = rs.getString("CLASS_ID");
                linkList.add(cld);
            }
    
            Gson gson = new Gson();
            String linkagesJson = gson.toJson(linkList);
            message = "{\"message\": \"grabLinkagesDBSuccess\", \"linkages\": " + linkagesJson + "}";
    
            rs.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            message = "{\"message\": \"grabLinkagesDBError\"}";
        }
    }
    

    public void adminAddUser(UserData userData) throws SQLException {
        System.out.println("In ExamServlet.adminAddUser. Attempting to add username=" + userData.addusername);

        int intAddUserType = Integer.parseInt(userData.addusertype);

        try{
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();
            
            String sql = "INSERT INTO USERS (UID, USERNAME, FIRST_NAME, LAST_NAME, PASSWORD, USER_TYPE) VALUES (NULL, '" + userData.addusername + "', '" + userData.addfirstname + "', '" + userData.addlastname + "', '" + userData.addpassword + "', " + intAddUserType + ")";
            int rows = statement.executeUpdate(sql);

            if(rows > 0) {
                message = "{\"message\": \"adminAddUserSuccess\"}";
            }else{
                message = "{\"message\": \"adminAddUserError\"}";
            }
            conn.close();
            statement.close();
        }catch (Exception e) {
            e.printStackTrace();
            message = "{\"message\": \"adminAddUserError\"}";
        }
    }

    public void adminRemoveUser(UserData userData) throws SQLException {
        System.out.println("In ExamServlet.adminRemoveUser. Attempting to remove userid=" + userData.modifyUserID);

        int intModifyUserID = Integer.parseInt(userData.modifyUserID);

        try{
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();

            String sql = "DELETE FROM USERS WHERE uid = " + intModifyUserID;
            int success = statement.executeUpdate(sql);
            if(success > 0) { 
                message = "{\"message\": \"adminRemoveUserSuccess\"}";
            } else {
                message = "{\"message\": \"adminRemoveUserError\"}";
            }
            conn.close();
            statement.close();
        }catch (Exception e) { 
            e.printStackTrace();
            message = "{\"message\": \"adminRemoveUserError\"}";

        }
    }

    public void grabUserDB(UserData userData) throws SQLException {
        System.out.println("In ExamServlet.grabUserDB. Grabbing user database information.");
    
        List<UserData> usersList = new ArrayList<>(); // To store the user data
        try {
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();
    
            String sql = "SELECT * FROM USERS";
            ResultSet rs = statement.executeQuery(sql);
    
            while (rs.next()) {
                UserData user = new UserData();
                user.userid = rs.getString("UID");
                user.username = rs.getString("USERNAME");
                user.password = rs.getString("PASSWORD"); 
                user.usertype = rs.getString("USER_TYPE");
                user.firstname = rs.getString("FIRST_NAME");
                user.lastname = rs.getString("LAST_NAME");    
                usersList.add(user);
            }
    
            Gson gson = new Gson();
            String usersJson = gson.toJson(usersList);
            message = "{\"message\": \"grabUserDBSuccess\", \"users\": " + usersJson + "}";
    
            rs.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in grabUserDB");
            message = "{\"message\": \"grabUserDBError\"}";
        }
    }    

    public void addQuestions (UserData userData) throws SQLException {
        System.out.println("In ExamServlet.addQuestions. Adding question: " + userData.addQuestionText + " to class=" + userData.classQuestionID);
        try {
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();
        
            // Handle question format escaping
            String userDataText = userData.addQuestionText;
            String escapedText = userDataText.replace("'", "''");
        
            // Check if the question already exists
            String checkSql = "SELECT COUNT(*) FROM QUESTIONS WHERE QUESTION_TEXT = '" + escapedText + "'";
            ResultSet rs = statement.executeQuery(checkSql);
        
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
        
            if (count == 0) {
                // Question does not exist, add it to the database
                String sql = "INSERT INTO QUESTIONS (QUESTION_ID, QUESTION_TEXT) VALUES (NULL, '" + escapedText + "')";
                int modifyStatus = statement.executeUpdate(sql);
        
                if (modifyStatus > 0) {
                    sql = "SELECT QUESTION_ID FROM QUESTIONS WHERE QUESTION_TEXT = '" + escapedText + "'";
                    rs = statement.executeQuery(sql);
                    int qid = 0;
                    if(rs.next()) {
                        qid = rs.getInt(1);
                    }
                    checkSql = "SELECT COUNT(*) FROM QUESTION_CLASSES WHERE QUESTION_ID = " + qid + " AND CLASS_ID = '" + userData.classQuestionID + "'";
                    rs = statement.executeQuery(checkSql);
                    count = 0;
                    if(rs.next()) {
                        count = rs.getInt(1);
                    }
                    if(count > 0) {
                        message = "{\"message\": \"addQuestionError\"}";
                    }else{
                        sql = "INSERT INTO QUESTION_CLASSES (QUESTION_ID, CLASS_ID) VALUES (" + qid + ", \'" + userData.classQuestionID + "')";
                        statement.executeUpdate(sql);
                        message = "{\"message\": \"addQuestionSuccess\"}";
                    }
                } else {
                    message = "{\"message\": \"addQuestionError\"}";
                }
            } else {
                // Question already exists
                message = "{\"message\": \"addQuestionError\"}";
            }
            conn.close();
            rs.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            message = "{\"message\": \"addQuestionError\"}";
        }        
    }

    public void addStudentToClass(UserData userData) throws SQLException {
        System.out.println("In ExamServlet.addStudentToClass. Adding student id " + userData.modifyStudentID + " to class " + userData.modifyStudentClass);
        try{
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();

            int localModifyStudentID = Integer.parseInt(userData.modifyStudentID);

            String sql = "SELECT STUDENT_ID FROM STUDENT_CLASSES WHERE STUDENT_ID = " + localModifyStudentID + " AND CLASS_ID = '" + userData.modifyStudentClass + "'";
            ResultSet rs = statement.executeQuery(sql);
            if(rs.next()) {
                message = "{\"message\": \"addStudentExists\"}";
            }else{
                sql = "INSERT INTO STUDENT_CLASSES (STUDENT_ID, CLASS_ID) VALUES (" + localModifyStudentID + ", \'" + userData.modifyStudentClass + "\')";
                int modifyStatus = statement.executeUpdate(sql);

                if(modifyStatus >= 1) {
                    message = "{\"message\": \"addStudentSuccess\"}";
                }else if (modifyStatus < 1) {
                    message = "{\"message\": \"addStudentError\"}";
                }
            }
            rs.close();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
            message = "{\"message\": \"addStudentError\"}";
        }
    }
    public void removeStudentFromClass(UserData userData) throws SQLException {
        System.out.println("In ExamServlet.removeStudentFromClass. Removing student id " + userData.modifyStudentID + " from class " + userData.modifyStudentClass);
        try{
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();

            int localModifyStudentID = Integer.parseInt(userData.modifyStudentID);

            String sql = "DELETE FROM STUDENT_CLASSES WHERE STUDENT_ID = " + localModifyStudentID + " AND CLASS_ID = '" + userData.modifyStudentClass + "'";
            int modifyStatus = statement.executeUpdate(sql);
                if(modifyStatus >= 1) {
                    message = "{\"message\": \"removeStudentSuccess\"}";
                }else if (modifyStatus < 1) {
                    message = "{\"message\": \"removeStudentError\"}";
                }
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
            message = "{\"message\": \"removeStudentError\"}";
        }
    }

    public void getTeacherClass(UserData userData) throws SQLException {
        System.out.println("In ExamServlet.getTeacherClass(). Retrieving teacher assignments for userid/teacherid: " + userData.userid + ".");
        try {
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();

            int teacheridInt = Integer.parseInt(userData.userid);

            //Query the Class_ID table to get teacher's assignments
            String sql = "SELECT CLASS_ID FROM CLASSES WHERE TEACHER_ID = '" + teacheridInt + "'";
            ResultSet rs = statement.executeQuery(sql);
            userData.teacherClasses = new ArrayList<>();
            while(rs.next()) {
                userData.teacherClasses.add(rs.getString(1));
            }

            Gson gson = new Gson();
            // Convert the list of exam questions to JSON
            String teacherClassesJson = gson.toJson(userData.teacherClasses);
            // Construct the message including exam questions
            message = "{\"message\": \"getTeacherClassSuccess\", " +
                    "\"teacherClasses\":" + teacherClassesJson + "}";

            rs.close();
            conn.close();

        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in getTeacherClass");
            message = "{\"message\": \"getTeacherClassFailed\"}";
        }
    }

    public void getExamQuestions(UserData userData) throws SQLException {
        System.out.println("In ExamServlet.getExamQuestions(). Retrieving exam questions for examid=" + userData.examid);
        try {
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();
            
            String sql = "SELECT Q.QUESTION_TEXT, EQ.TEACHER_FEEDBACK " +
                         "FROM EXAM_QUESTIONS EQ " +
                         "JOIN QUESTIONS Q ON EQ.QUESTION_ID = Q.QUESTION_ID " +
                         "WHERE EQ.EXAM_ID = '" + userData.examid + "'";
            
            ResultSet rs = statement.executeQuery(sql);
            //Get ready to handle the list of questions
            userData.examQuestions = new ArrayList<>();
    
            while (rs.next()) {
                ExamQuestion examQuestion = new ExamQuestion();
                //Get question text
                examQuestion.question = rs.getString(1);
                //Get feedback text & convert from CLOB to string
                Clob feedbackClob = rs.getClob(2);
                examQuestion.feedback = clobToString(feedbackClob);
                //Add the data to the userData object
                userData.examQuestions.add(examQuestion);
            }
            
            Gson gson = new Gson();
            // Convert the list of exam questions to JSON
            String examQuestionsJson = gson.toJson(userData.examQuestions);
            // Construct the message including exam questions
            message = "{\"message\": \"getStudentExamSuccess\", " +
                    "\"userid\": \"" + userData.userid + "\", " +
                    "\"username\": \"" + userData.username + "\", " +
                    "\"firstname\": \"" + userData.firstname + "\", " +
                    "\"lastname\": \"" + userData.lastname + "\", " +
                    "\"usertype\": \"" + userData.usertype + "\", " +
                    "\"classid\": \"" + userData.classid + "\", " +
                    "\"pointspossible\": \"" + userData.pointspossible + "\", " +
                    "\"pointsearned\": \"" + userData.pointsearned + "\", " +
                    "\"examQuestions\": " + examQuestionsJson + "}";

            rs.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in getExamQuestions");
            message = "{\"message\": \"getExamQuestionsError\"}";
        } 
    }
    
    //A helper method to handle turing clobs to stings
    public String clobToString(Clob clob) throws SQLException, IOException {
        //Use a string builder to grow the string
        StringBuilder sb = new StringBuilder();
        //Use a reader to read the clob almost like a file char by char
        Reader reader = clob.getCharacterStream();
        BufferedReader br = new BufferedReader(reader);
    
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
    
        return sb.toString();
    }
    
    
    public void getStudentExamInfo(UserData userData) throws SQLException {
        System.out.println("In ExamServlet.getStudentExamInfo().");
        System.out.println("Retrieving information for userid=" + userData.userid + " username=" + userData.username);
        try {
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();
            String sql = "SELECT EXAM_ID, POINTS_POSSIBLE, POINTS_EARNED FROM EXAMS WHERE STUDENT_ID = '" + userData.userid + "'";
            ResultSet rs = statement.executeQuery(sql);

            if(rs.next()) {
                int examID = rs.getInt(1);
                System.out.println("examID=" + examID);
                
                userData.examid = String.valueOf(examID);
                userData.pointspossible = rs.getString(2);
                userData.pointsearned = rs.getString(3);
                message = "{\"message\": \"getStudentExamSuccess\", \"userid\": \"" + userData.userid + 
                    "\", \"username\": \"" + userData.username + 
                    "\", \"firstname\": \"" + userData.firstname + 
                    "\", \"lastname\": \"" + userData.lastname + 
                    "\", \"usertype\": \"" + userData.usertype + 
                    "\", \"classid\": \"" + userData.classid + 
                    "\", \"pointspossible\": \"" + userData.pointspossible + 
                    "\", \"pointsearned\": \"" + userData.pointsearned + "\"}";

            }
            rs.close();
            conn.close();
        } catch(Exception e) {

        }
    }

    public void getClassInfo(UserData userData) throws SQLException {
        System.out.println("In ExamServlet.getClassInfo().");
        System.out.println("userid=" + userData.userid + " username=" + userData.username);
        try {
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();
            String sql = "SELECT CLASS_ID FROM STUDENT_CLASSES WHERE STUDENT_ID = '" + userData.userid + "'";
            ResultSet rs = statement.executeQuery(sql);
    
            if (rs.next()) {
                String classID = rs.getString(1);
                System.out.println("Class ID for " + userData.username + ": " + classID);
                
                userData.studentid = userData.userid;
                userData.classid = classID; // Store the class ID in userData
                // Construct the response message including the class ID
                message = "{\"message\": \"getClassSuccess\", \"userid\": \"" + userData.userid + 
                          "\", \"username\": \"" + userData.username + 
                          "\", \"firstname\": \"" + userData.firstname + 
                          "\", \"lastname\": \"" + userData.lastname + 
                          "\", \"usertype\": \"" + userData.usertype + 
                          "\", \"classid\": \"" + userData.classid + "\"}";
            } else {
                message = "{\"message\": \"getClassFailed\"}";
            }
            rs.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in getClassInfo");
        }
    }
    

    public void doLogin(UserData userData) throws SQLException {
        System.out.println("In ExamServlet.doLogin().");
        System.out.println("username=" + userData.username);
        try {
            conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();
            String sql = "SELECT PASSWORD, UID, FIRST_NAME, LAST_NAME, USER_TYPE FROM USERS WHERE USERNAME = '" + userData.username + "'";
            ResultSet rs = statement.executeQuery(sql);
    
            if (rs.next()) {
                String passwordFromDB = rs.getString(1);
                System.out.println("Entered password: " + userData.password);
                System.out.println("Password from DB: " + passwordFromDB);
    
                if (passwordFromDB != null && passwordFromDB.equals(userData.password)) {
                    userData.userid = rs.getString(2);
                    userData.firstname = rs.getString(3);
                    userData.lastname = rs.getString(4);
                    userData.usertype = rs.getString(5);
                    message = "{\"message\": \"loginsuccess\", \"userid\": \"" + userData.userid + "\", \"username\": \"" + userData.username + "\", \"firstname\": \"" + userData.firstname + "\", \"lastname\": \"" + userData.lastname + "\", \"usertype\": \"" + userData.usertype + "\"}";
                } else {
                    message = "{\"message\": \"loginfail\"}";
                }
            } else {
                message = "{\"message\": \"loginfail\"}";
            }
            rs.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in doLogin");
        }
    }    
}
