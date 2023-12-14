import java.sql.*;
import java.util.*;
import java.io.*;
import java.nio.*;

public class MakeClassDB { 
    static Statement statement;
    static String[] UWQuestions, EAPQuestions, TEDQuestions;
    public static void main (String[] args) { 
        try {
            //Conect to H2 DB server
            Class.forName("org.h2.Driver");
            //As in unit examples, I'm building this in the myservers folder that
            //we were instructed to put on the Desktop back in u9mod0.
            Connection conn = DriverManager.getConnection("jdbc:h2:~/Desktop/myservers/databases/jpaiz", "sa", "");
            statement = conn.createStatement();

            //Create tables for that database
            makeUsersTable();
            System.out.println("USERS TABLE:");
            printTable("USERS");
            
            makeClassesTable();
            System.out.println("CLASSES TABLE:");
            printTable("CLASSES");

            makeStudentClassesTable();
            System.out.println("STUDENT_CLASSES TABLE:");
            printTable("STUDENT_CLASSES");

            makeQuestionsTable();
            System.out.println("QUESTIONS TABLE:");
            printTable("QUESTIONS");

            makeQuestionClassesTable();
            System.out.println("QUESTION_CLASSES TABLE:");
            printTable("QUESTION_CLASSES");

            makeExamsTable();
            System.out.println("EXAMS TABLE:");
            printTable("EXAMS");

            makeExamQuestionsTable();
            System.out.println("EXAM_QUESTIONS TABLE:");
            printTable("EXAM_QUESTIONS");

            //Close the connnection to the DB server
            conn.close();
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error connecting to H2 Database Server");
        }
    }

    static void printTable(String tableName) throws SQLException { 
        // Build and send query
        String sql = "SELECT * FROM " + tableName;
        ResultSet rs = statement.executeQuery(sql);
        ResultSetMetaData md = rs.getMetaData();
        int numColumns = md.getColumnCount();
    
        // Calculate column widths and print column headers
        int[] columnWidths = new int[numColumns];
        for (int i = 1; i <= numColumns; i++) { 
            columnWidths[i - 1] = md.getColumnName(i).length();
            System.out.print(String.format("%-20s", md.getColumnName(i)) + " | ");
        }
        System.out.println();
        System.out.println(new String(new char[20 * numColumns + 3 * (numColumns - 1)]).replace('\0', '-')); // Table separator
    
        // Print table contents
        while (rs.next()) {
            for (int i = 1; i <= numColumns; i++) {
                Object obj = rs.getObject(i);
                String value = obj != null ? obj.toString() : "NULL";
                System.out.print(String.format("%-20s", value) + " | ");
            }
            System.out.println();
        }
        System.out.println("\n\n");
    }    

    static void makeUsersTable() throws SQLException { 
        //Clear any existing versions
        String sql = "DROP TABLE IF EXISTS USERS";
        statement.executeUpdate(sql);

        /*Build empty version of the table.
         * Set UID column to auto-increment as new users are created.
         * Set USERNAME column to unique to prevent duplicate user names being created.
         */
        sql = "CREATE TABLE USERS (UID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, USERNAME VARCHAR(45) UNIQUE, FIRST_NAME VARCHAR(45), LAST_NAME VARCHAR(45), PASSWORD VARCHAR(45), USER_TYPE INT)";
        statement.executeUpdate(sql);

        /*Slot in dummy values for demonstration purposes.
         * RE: user types:
         * 0 = admin
         * 1 = teacher
         * 2 = student
         */
        sql = "INSERT INTO USERS VALUES (NULL, 'admin', 'System', 'Administrator', '1234567890', 0)";
        statement.executeUpdate(sql);
        sql = "INSERT INTO USERS VALUES (NULL, 'TestTeacher1', 'Priyanka', 'Singh', 'password', 1)";
        statement.executeUpdate(sql);
        sql = "INSERT INTO USERS VALUES (NULL, 'TestTeacher2', 'Xiaoli', 'Zhang', 'password', 1)";
        statement.executeUpdate(sql);
        sql = "INSERT INTO USERS VALUES (NULL, 'student1', 'Xiaoming', 'Wang', 'password', 2)";
        statement.executeUpdate(sql);
        sql = "INSERT INTO USERS VALUES (NULL, 'student2', 'Jean', 'DuPont', 'password', 2)";
        statement.executeUpdate(sql);
        sql = "INSERT INTO USERS VALUES (NULL, 'student3', 'Marie', 'Martin', 'password', 2)";
        statement.executeUpdate(sql);
        sql = "INSERT INTO USERS VALUES (NULL, 'student4', 'Giannis', 'Papadopoulos', 'password', 2)";
        statement.executeUpdate(sql);
        sql = "INSERT INTO USERS VALUES (NULL, 'student5', 'Maria', 'Georgiou', 'password', 2)";
        statement.executeUpdate(sql);
        sql = "INSERT INTO USERS VALUES (NULL, 'student6', 'Taro', 'Yamada', 'password', 2)";
        statement.executeUpdate(sql);
        sql = "INSERT INTO USERS VALUES (NULL, 'student7', 'Hanako', 'Sawai', 'password', 2)";
        statement.executeUpdate(sql);
        sql = "INSERT INTO USERS VALUES (NULL, 'student8', 'Ivan', 'Ivanov', 'password', 2)";
        statement.executeUpdate(sql);
        sql = "INSERT INTO USERS VALUES (NULL, 'student9', 'Ghada', 'Gherwash', 'password', 2)";
        statement.executeUpdate(sql);
        sql = "INSERT INTO USERS VALUES (NULL, 'student10', 'Mercedes', 'Ramirez', 'password', 2)";
        statement.executeUpdate(sql);
        sql = "INSERT INTO USERS VALUES (NULL, 'TestTeacher3', 'Bin', 'Hu', 'password', 1)";
        statement.executeUpdate(sql);
    }

    static void makeClassesTable() throws SQLException { 
        //Clear any existing verisons
        String sql = "DROP TABLE IF EXISTS CLASSES";
        statement.executeUpdate(sql);
        //Build an empty version of the table
        sql = "CREATE TABLE CLASSES (CLASS_ID VARCHAR(8) NOT NULL PRIMARY KEY, TEACHER VARCHAR(45), TEACHER_ID INT, FOREIGN KEY (TEACHER_ID) REFERENCES USERS (UID) ON DELETE CASCADE)";
        statement.executeUpdate(sql);
        //Populate table with dummy values
        sql = "INSERT INTO CLASSES VALUES ('EAP1015', 'Singh', 2)";
        statement.executeUpdate(sql);
        sql = "INSERT INTO CLASSES VALUES ('UW1020', 'Zhang', 3)";
        statement.executeUpdate(sql);
        sql = "INSERT INTO CLASSES VALUES ('TED001', 'Singh', 2)";
        statement.executeUpdate(sql);
    }

    static void makeQuestionClassesTable() throws SQLException { 
        //Clear any exisiting version
        String sql = "DROP TABLE IF EXISTS QUESTION_CLASSES";
        statement.executeUpdate(sql);
        //Build an empty version of the table
        sql = "CREATE TABLE QUESTION_CLASSES (QUESTION_ID INT, CLASS_ID VARCHAR(8), FOREIGN KEY (QUESTION_ID) REFERENCES QUESTIONS (QUESTION_ID), FOREIGN KEY (CLASS_ID) REFERENCES CLASSES (CLASS_ID))";
        statement.executeUpdate(sql);
        //Populate table with dummy values
        int j = 1;
        for(int i = 0; i < UWQuestions.length; i++) {
            sql = "INSERT INTO QUESTION_CLASSES VALUES (" + j + ", 'UW1020')";
            statement.executeUpdate(sql);
            j++;;
        }
        for(int i = 0; i < EAPQuestions.length; i++) {
            sql = "INSERT INTO QUESTION_CLASSES VALUES (" + j + ", 'EAP1015')";
            statement.executeUpdate(sql);
            j++;
        }
        for(int i = 0; i < TEDQuestions.length; i++) {
            sql = "INSERT INTO QUESTION_CLASSES VALUES (" + j + ", 'TED001')";
            statement.executeUpdate(sql);
            j++;
        }
    }

    static void makeStudentClassesTable() throws SQLException {
        //Clear any existing versions
        String sql = "DROP TABLE IF EXISTS STUDENT_CLASSES";
        statement.executeUpdate(sql);
        //Build an empty version of the table
        sql = "CREATE TABLE STUDENT_CLASSES (STUDENT_ID INT, CLASS_ID VARCHAR(8), FOREIGN KEY (STUDENT_ID) REFERENCES USERS (UID) ON DELETE CASCADE, FOREIGN KEY (CLASS_ID) REFERENCES CLASSES (CLASS_ID))";
        statement.executeUpdate(sql);
        //Populate table with dummy values
        String className = "EAP1015";
        for(int i = 4; i <= 8; i++) {
            sql = "INSERT INTO STUDENT_CLASSES VALUES (" + i + ", \'" + className + "\')";
            statement.executeUpdate(sql);
        }
        className = "UW1020";
        for(int i = 9; i <= 13; i++) {
            sql = "INSERT INTO STUDENT_CLASSES VALUES (" + i + ", \'" + className + "\')";
            statement.executeUpdate(sql);
        }
    }

    static void makeQuestionsTable() throws SQLException, IOException {
        //Clear any exisiting version
        String sql = "DROP TABLE IF EXISTS QUESTIONS";
        statement.execute(sql);
        //Build an empty version of the table
        sql = "CREATE TABLE QUESTIONS (QUESTION_ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, QUESTION_TEXT VARCHAR(2000))";
        statement.executeUpdate(sql);
        //Read questions into program from questions.txt
        readQuestionsFromFile();
        //Make sure to properly escape any question-internal quotes
        for(int i = 0; i < UWQuestions.length; i++) {
            sql = "INSERT INTO QUESTIONS VALUES (NULL, '" + UWQuestions[i].replace("'", "''") + "')";
            statement.executeUpdate(sql);
        }
        for(int i = 0; i < EAPQuestions.length; i++) {
            sql = "INSERT INTO QUESTIONS VALUES (NULL, '" + EAPQuestions[i].replace("'", "''") + "')";
            statement.executeUpdate(sql);
        }
        for(int i = 0; i < TEDQuestions.length; i++) {
            sql = "INSERT INTO QUESTIONS VALUES (NULL, '" + TEDQuestions[i].replace("'", "''") + "')";
            statement.executeUpdate(sql);
        }
        
    }

    static void makeExamQuestionsTable() throws SQLException {
        //Clear any existing version
        String sql = "DROP TABLE IF EXISTS EXAM_QUESTIONS";
        statement.executeUpdate(sql);
        //Build blank verion of the table
        sql = "CREATE TABLE EXAM_QUESTIONS (EXAM_ID INT, QUESTION_ID INT, TEACHER_FEEDBACK CLOB, FOREIGN KEY (EXAM_ID) REFERENCES EXAMS (EXAM_ID) ON DELETE CASCADE, FOREIGN KEY (QUESTION_ID) REFERENCES QUESTIONS (QUESTION_ID))";
        statement.executeUpdate(sql);
        //Populate dummy entries
        sql = "INSERT INTO EXAM_QUESTIONS VALUES (1, 1, 'A thoughtful answer, overall.')";
        statement.executeUpdate(sql);
        sql = "INSERT INTO EXAM_QUESTIONS VALUES (1, 2, 'The answer meanders a bit. Aim for more precision in your responses.')";
        statement.executeUpdate(sql);
        sql = "INSERT INTO EXAM_QUESTIONS VALUES (1, 3, 'While your conclusion is strong, the supporting arguments need more development.')";
        statement.executeUpdate(sql);
        sql = "INSERT INTO EXAM_QUESTIONS VALUES (1, 4, 'Great structure and clarity in your answer. Keep up the good work!')";
        statement.executeUpdate(sql);
        sql = "INSERT INTO EXAM_QUESTIONS VALUES (1, 5, 'Your creativity in approaching this question is commendable, but don''t forget to back it up with facts.')";
        statement.executeUpdate(sql);
        sql = "INSERT INTO EXAM_QUESTIONS VALUES (2, 39, 'This is a solid response, though it could benefit from more specific examples.')";
        statement.executeUpdate(sql);
        sql = "INSERT INTO EXAM_QUESTIONS VALUES (2, 40, 'Excellent insight! Your depth of understanding is impressive.')";
        statement.executeUpdate(sql);
        sql = "INSERT INTO EXAM_QUESTIONS VALUES (2, 41, 'While your conclusion is strong, the supporting arguments need more development.')";
        statement.executeUpdate(sql);
        sql = "INSERT INTO EXAM_QUESTIONS VALUES (2, 42, 'Great structure and clarity in your answer. Keep up the good work!')";
        statement.executeUpdate(sql);
        sql = "INSERT INTO EXAM_QUESTIONS VALUES (2, 43, 'Your creativity in approaching this question is commendable, but don''t forget to back it up with facts. Something that would be particularly helpful here is the use of the sources that you found in your AnnBib to support your ideas more.')";
        statement.executeUpdate(sql);
        sql = "INSERT INTO EXAM_QUESTIONS VALUES (3, 69, 'Your response showcases a thorough understanding of the topic, but it would benefit from a more critical analysis of the sources. Consider contrasting different viewpoints and exploring the implications of these perspectives in greater depth.')";
        statement.executeUpdate(sql);

        sql = "INSERT INTO EXAM_QUESTIONS VALUES (3, 70, 'You have demonstrated a clear grasp of the subject matter; however, the argument could be strengthened by incorporating a wider range of scholarly sources. This would add depth to your analysis and provide a more rounded view of the topic.')";
        statement.executeUpdate(sql);

        sql = "INSERT INTO EXAM_QUESTIONS VALUES (3, 71, 'While your answer is well-structured, it occasionally deviates from the main question. Try to maintain a focused approach throughout, ensuring each point directly contributes to answering the question posed. Additionally, drawing upon more empirical evidence could substantiate your claims more effectively.')";
        statement.executeUpdate(sql);

        sql = "INSERT INTO EXAM_QUESTIONS VALUES (3, 72, 'Your answer is comprehensive, but it lacks the critical insight that could take it to the next level. Try to interrogate your sources more deeply, and question the underlying assumptions in the arguments presented. This kind of critical engagement would elevate the quality of your response significantly.')";
        statement.executeUpdate(sql);

        sql = "INSERT INTO EXAM_QUESTIONS VALUES (3, 73, 'You''ve articulated your points well, but the response would benefit from more varied sentence structures and a more engaging narrative style. This can help maintain the reader''s interest and enhance the overall impact of your argument. Also, don''t forget to tie back each point explicitly to the thesis statement.')";
        statement.executeUpdate(sql);

        sql = "INSERT INTO EXAM_QUESTIONS VALUES (3, 74, 'The analytical depth you''ve achieved in some sections is commendable, yet in others, the analysis remains surface-level. Strive for consistency in depth across your response. Also, integrating real-world examples where applicable could provide tangible context to your theoretical insights, making your arguments more relatable and impactful.')";
        statement.executeUpdate(sql);

    }

    static void makeExamsTable() throws SQLException { 
        //Clear any exisiting version
        String sql = "DROP TABLE IF EXISTS EXAMS";
        statement.executeUpdate(sql);
        //Build an empty version of the table using subqueries to prevent hardcoding class info
        sql = "CREATE TABLE EXAMS (EXAM_ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, CLASS_ID VARCHAR(8), STUDENT_ID INT, POINTS_POSSIBLE INT, POINTS_EARNED INT, FOREIGN KEY (CLASS_ID) REFERENCES CLASSES (CLASS_ID), FOREIGN KEY (STUDENT_ID) REFERENCES USERS (UID) ON DELETE CASCADE)";
        statement.executeUpdate(sql);
        //Populate table with dummy values
        sql = "INSERT INTO EXAMS (EXAM_ID, CLASS_ID, STUDENT_ID, POINTS_POSSIBLE, POINTS_EARNED) SELECT NULL, CLASS_ID, 4, 325, 289 FROM STUDENT_CLASSES WHERE STUDENT_ID = 4";
        statement.executeUpdate(sql);
        sql = "INSERT INTO EXAMS (EXAM_ID, CLASS_ID, STUDENT_ID, POINTS_POSSIBLE, POINTS_EARNED) SELECT NULL, CLASS_ID, 6, 325, 253 FROM STUDENT_CLASSES WHERE STUDENT_ID = 6";
        statement.executeUpdate(sql);
        sql = "INSERT INTO EXAMS (EXAM_ID, CLASS_ID, STUDENT_ID, POINTS_POSSIBLE, POINTS_EARNED) SELECT NULL, CLASS_ID, 9, 325, 189 FROM STUDENT_CLASSES WHERE STUDENT_ID = 9";
        statement.executeUpdate(sql);

    }

    static void readQuestionsFromFile() throws IOException { 
        // System.out.println("In readQuestionsFromFile. Attempting to read questions.txt");
        try {
            //Read in the file
            BufferedReader br = new BufferedReader(new FileReader("./questions.txt"));
            //Set up holder lists
            List<String> UWList = new ArrayList<>();
            List<String> EAPList = new ArrayList<>();
            List<String> TEDList = new ArrayList<>();
            //Set up holder for line and section tracking
            String line;
            String currentSection = "";
            //Sort the contents of the file
            //Using switch because nested while/if structure was too error prone
            while ((line = br.readLine()) != null) {
                if (line.equals("UWQuestions")) {
                    currentSection = "UWQuestions";
                } else if (line.equals("EAPQuestions")) {
                    currentSection = "EAPQuestions";
                } else if (line.equals("TEDQuestions")) {
                    currentSection = "TEDQuestions";
                } else {
                    switch (currentSection) {
                        case "UWQuestions":
                            UWList.add(line);
                            break;
                        case "EAPQuestions":
                            EAPList.add(line);
                            break;
                        case "TEDQuestions":
                            TEDList.add(line);
                            break;
                    }
                }
            }
    
            UWQuestions = UWList.toArray(new String[0]);
            EAPQuestions = EAPList.toArray(new String[0]);
            TEDQuestions = TEDList.toArray(new String[0]);
            // System.out.println("questions.txt successfully read");
            System.out.println("UW List: " + UWQuestions.length);
            System.out.println("EAP List: " + EAPQuestions.length);
            System.out.println("TED List: " + TEDQuestions.length);
    
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("File: \"questions.txt\" not found in this directory.");
        }
    }
    
}