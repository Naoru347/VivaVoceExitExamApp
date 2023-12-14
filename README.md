# ExitExamWebApplication
This 3-teir application provides an interface for teachers and students to manage oral exit exams/reflective defenses. Providing functionality for teachers to manage their classes, score exams, and provide feedback; and, for students to easily review their grades, the questions they were asked, and the feedback they received from their teachers. 

1: Database initialization
Ensure that the H2 database server is running and located out of ~/USER/desktop/myservers/h2
o Run your H2 server per the usual method describe in course materials.
o For me, this involved going to ~/USER/Desktop/myserver/h2 and running
the command: java -cp h2.jar org.h2.tools.Server -tcp -pg
Ensure that the folder “databases” exists in your ~/USER/desktop/myservers director such that ~/USER/desktop/myservers/databses is a legitimate directory. Ensure that the included questions.txt file is in the same directory as MakeClassDB.java
o If you have not modified the directory structure from what was submitted, this should already be the case
Compile and run MakeClassDB.java
o This will connect to the H2 DB Server through the following credentials o URL: jdbc:h2:~/Desktop/myservers/databases/jpaiz
o Username: sa
o Password: “”
Successful execution will be verified at the terminal as the database tables will be printed to the terminal upon successful code execution.
Successful execution can also be verified by confirming the presence of “jpaiz.mv.db” and “jpaiz.trace.db”.
2: Servlet Initialization
Compile and run ExamServlet.java
3: Server Initialization
Compile and run MyServer.java
Note: The server will listen on port 40106
4: Site File Storage
Note: All site files should be stored in a single, shared directory. Example: I coded this site with all files in ~/USER/desktop/cs6004/unit11/module5 directory.
5: Site testing
Using the browser of your choice, navigate to http://localhost:40106/ or http://localhost:40106/login.html
The admin account uses the following credentials
o Username: admin
o Password: 1234567890 There are two test teacher accounts
o Username:TestTeacher1
o Password: password
o AssignedClasses:EAP1015,TED001
  
o Username:TestTeacher2
o Password: password
o Assigned Classes: UW1020
• There are 10 test student accounts
o Usernames: student1 – student10
o Passwords: password
o student1 – student5 are enrolled in EAP1015
o student6 – student10 are enrolled in UW1020
o No students are enrolled in TED001 upon initialization.
• There are 3 active exam records upon initialization for the following students: o student1
o student3
o student6
o You can log in as these students and instantly see exam results.
• You can log in as TestTeacher1 and start and grade an exam for student2, student4, and student 5. You can login as TestTeacher2 and start and grade an exam for student7-student10. Upon successful exam submission, you can log in as these students to retrieve the exam records.
