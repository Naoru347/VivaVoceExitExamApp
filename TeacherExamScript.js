//Servlet information for server-side processing
let url = "http://localhost:40106/examservlet";

//A slug object for all possible data that we might need to send to the server
let data = {
    userid: null,
    username: null,
    password: null,
    usertype: null,
    firstname: null,
    lastname: null,
    classid: null, 
    teacher: null,
    teacherid: null,
    studentid: null,
    modifyStudentID: null,
    modifyStudentClass: null,
    questionid: null,
    examid: null,
    pointspossible: null,
    pointsearned: null,
    examQuestions: [],
    examQuestionIDs: [],
    teacherFeedback: [],
    teacherClasses: [],
    classQuestionID: null,
    addQuestionText: null,
    action: null,
    message: null
};

//A holder for JSON data from the server
let jsonDataFromServer = {
    message: "temp"
};
var app = angular.module("teacherExamApp", ['ngMaterial', 'ngMessages']);

app.controller("control", function($scope, $mdToast, $mdDialog) {


    //Set the inital state of our $scope.user object
    //based on sessionStorage data.
    $scope.user = {
        userid: sessionStorage.getItem("userid"),
        teacherid: sessionStorage.getItem("userid"),
        firstname: sessionStorage.getItem("firstname"),
        lastname: sessionStorage.getItem("lastname"),
        teacherClasses: sessionStorage.getItem("teacherClasses"),
        classid: sessionStorage.getItem("classid"),
        examQuestionTexts: JSON.parse(sessionStorage.getItem("examQuestionTexts")),
        examQuestionIDs: sessionStorage.getItem("examQuestionIDs"),
        studentid: '',
        pointspossible: '',
        teacherFeedback: new Array(5).fill(''),
        scores: new Array(5).fill(null)

    };

    //Logout function as before
    $scope.logout = function() { 
        //Clear session storage
        sessionStorage.clear();
        window.location.href = 'login.html';
    };

    //Handle interim score calculation
    $scope.calculateFinalScore = function() {
        // Filter out unanswered questions
        let answeredQuestions = $scope.user.scores.filter(score => score !== null);
    
        // Calculate the average score of answered questions
        let averageScore = answeredQuestions.reduce((acc, score) => acc + score, 0) / (answeredQuestions.length * 5);
        console.log("average score=" + averageScore);
    
        // Calculate the final score by multiplying the average score by the points possible
        // Ensure that the points possible is a number and not null or undefined
        let pointsPossible = parseInt($scope.user.pointspossible);
        if (!isNaN(pointsPossible) && pointsPossible > 0 && answeredQuestions.length > 0) {
            let finalScore = averageScore * pointsPossible;
            $scope.user.pointsearned = parseFloat(finalScore.toFixed(2));
            sessionStorage.setItem("pointsearned", $scope.user.pointsearned);
        } else {
            // Handle cases where points possible is not set, zero, or no questions are answered
            $scope.user.pointsearned = 0;
            sessionStorage.setItem("pointsearned", $scope.user.pointsearned);
        }
    };

    //Handle exam submission
    $scope.submitExam = function() {
        //Only submit if the form is valid
        if($scope.examForm.$valid){
            let examQuestionIDsString = sessionStorage.getItem("examQuestionIDs");
            data.examQuestionIDs = JSON.parse(examQuestionIDsString);
            let feedbackArray = Object.values($scope.user.feedback);
            data.pointspossible = $scope.user.pointspossible;
            data.pointsearned = $scope.user.pointsearned;
            data.teacherFeedback = feedbackArray;
            data.studentid = $scope.user.studentid;
            data.classid = sessionStorage.getItem("classid");
            data.action = "submitExamInfo";
            sendDataToServer($scope);
        }else{
            alert("Error in student id or points possible. Please review and resubmit.");
        }
    }
    

    //Handle start exam pop up to get the class that the exam should be associated with
    $scope.startExam = function(ev) {
        $mdDialog.show ({
            controller: DialogController,
            templateUrl: 'classinfo.html',
            parent: angular.element(document.querySelector('#dialogAnchor')),
            targetEvent: ev,
            clickOutsideToClose: true,
            fullscreen: false,
            openFrom: angular.element(document.body),
            closeTo: angular.element(document.body)

        })
        .then(function(answer) {
            handleStartExam(answer);
        }, function () {
            $scope.stateus = 'You cancelled the dialog.';
        });
    };

    function DialogController($scope, $mdDialog) {
        $scope.hide = function() { 
            $mdDialog.hide();
        };
        $scope.cancel = function() { 
            $mdDialog.cancel();
        };
        $scope.answer = function(answer) {
            $mdDialog.hide(answer);
        }
    };

    function handleStartExam(answer) { 
        console.log("In TeacherExamScriptJS.handleStartExam. Attempting to start exam for classid=" + answer);
        //Temporarily
        sessionStorage.setItem("classid", answer);
        data.classid = sessionStorage.getItem("classid");
        $scope.user.classid = data.classid;
        data.action = "getStartExamQuestions";
        sendDataToServer($scope);
    }

    //Get teacher class information
    $scope.manageClass = function() { 
        sessionStorage.setItem("teacherid", sessionStorage.getItem("userid"));
        data.userid = sessionStorage.getItem("userid");
        data.teacherid = sessionStorage.getItem("userid");
        data.action = "getTeacherClass";
        data.firstname = sessionStorage.getItem("firstname");
        data.lastname = sessionStorage.getItem("lastname");
        sendDataToServer($scope);
    }
    
    //Add a student to the class
    $scope.add = function () { 
        console.log ("In addStudent().");
        console.log ("addStudent.studentID=" + $scope.user.addStudentID);
        console.log("addStudent.classID=" + $scope.user.addToClass);
        data.action = "addStudentToClass";
        data.modifyStudentID = $scope.user.addStudentID;
        data.modifyStudentClass = $scope.user.addToClass;
        $scope.user.action = data.action;
        sendDataToServer($scope)
    }

    //Remove a student from the class
    $scope.remove = function () { 
        console.log ("In removeStudent().");
        console.log ("removeStudent.studentID=" + $scope.user.removeStudentID);
        console.log("removeStudent.classID=" + $scope.user.removeFromClass);
        data.action = "removeStudentFromClass";
        data.modifyStudentID = $scope.user.removeStudentID;
        data.modifyStudentClass = $scope.user.removeFromClass;
        $scope.user.action = data.action;
        sendDataToServer($scope)
    }

    $scope.manageExamQuestions = function() { 
        console.log("In manageExamQuestions(). Redirecting to managequestions.html");
        data.teacherid = sessionStorage.getItem("userid");
        data.userid = sessionStorage.getItem("userid");
        data.firstname = sessionStorage.getItem("firstname");
        data.lastname = sessionStorage.getItem("lastname");
        console.log("data=" + data);
        window.location.href = "managequestions.html"
        console.log("Redirect complete.");
    }

    $scope.addQuestion = function() {
        console.log("In addQuestion(). Preparing data packet for transmission to server."); 
        data.classQuestionID = $scope.user.classQuestionID;
        data.addQuestionText = $scope.user.addQuestionText;
        sessionStorage.setItem("classQuestionID", $scope.user.classQuestionID);
        sessionStorage.setItem("addQuestionText", $scope.user.addQuestionText);
        data.action = "addQuestions"
        console.log("Sending classQuestionID=" + data.classQuestionID + " and addQuestionText=" + data.addQuestionText + " to server");
        sendDataToServer(data);

    }

    //Form validation
    $scope.isClassTaughtByTeacher = function(className) {
        return $scope.user.teacherClasses.includes(className);
    };

    // Validate add to class
    $scope.$watch('user.addToClass', function(newValue) {
        if(newValue && !$scope.isClassTaughtByTeacher(newValue)) {
            showToast("You can only add students to classes you teach.");
        }
    });

    // Validate remove from class
    $scope.$watch('user.removeFromClass', function(newValue) {
        if(newValue && !$scope.isClassTaughtByTeacher(newValue)) {
            showToast("You can only remove students from classes you teach.");
        }
    });
    $scope.$watch('user.addStudentID', function(newValue, oldValue) {
        if(newValue && !isIntegerString(newValue)) {
            showToast("Please enter a valid integer for Student ID");
        }
    });
    $scope.$watch('user.removeStudentID', function(newValue, oldValue) {
        if(newValue && !isIntegerString(newValue)) {
            showToast("Please enter a valid integer for Student ID");
        }
    });
    $scope.$watch('user.classQuestionID', function(newValue, oldValue) {
        if(newValue && !isValidFormat(newValue)) {
            showToast("Please enter a valid format: 2-4 letters followed by 3-4 digits, e.g., XYZ 1234 or AB123");
        }
    });

    function isIntegerString(value) {
        var num = parseInt(value);
        return !isNaN(num) && num.toString() === value;
    }
    
    function showToast(message) {
        $mdToast.show(
            $mdToast.simple()
                .textContent(message)
                .hideDelay(3000)
                .position('top right')
        );
    }
    function isValidFormat(value) {
        //Regex for Starting with 2-4 chars followed by 3-4 digits
        const pattern = /^[A-Za-z]{2,4}\d{3,4}$/;
        return pattern.test(value);
    }    
});



function sendDataToServer($scope, callback) { 
    console.log("In TeacherExamScript.sendDataToServer: data=" + data);
    let req = new XMLHttpRequest();
    req.addEventListener("load", function(){
        requestListener(this.responseText, $scope);
        if(typeof callback === "function") {
            callback(this.responseText);
        }
    });
    req.open("POST", url);
    req.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    req.send(JSON.stringify(data));
    console.log("Sent to server json=" + JSON.stringify(data));
    
}

function requestListener(responseText, $scope) {
    //Parse response from server
    console.log("Received FROM server: " + responseText);
    let jsonObject = JSON.parse(responseText);
    console.log("Parsed server response:", jsonObject);
    //Control flow, based on message contentds
    if(jsonObject.message === "error"){
        alert("Error in request processing. Please try again later or contact your site administrator.");
    }else if(jsonObject.message === "getTeacherClassSuccess"){
        console.log("In TeacherExamScript.requestListener().elif teacherid=" + $scope.user.userid);
        jsonDataFromServer.message = jsonObject.message;
        jsonDataFromServer.teacherClasses = jsonObject.teacherClasses;
        $scope.$apply(() => {
            $scope.user.teacherClasses = jsonDataFromServer.teacherClasses;
            data.teacherClasses = jsonDataFromServer.teacherClasses;
            console.log("UPDATED data.teacherClasses=" + data.teacherClasses + " $scope.user.teacherClasses=" + $scope.user.teacherClasses);
        });
        sessionStorage.setItem("teacherClasses", data.teacherClasses);
        console.log(typeof data.teacherClasses);        
        if (jsonDataFromServer.message === "getTeacherClassSuccess") {
            window.location.href = "manageclass.html";
        }
    }else if(jsonObject.message === "addStudentSuccess"){
        console.log("In responseListener() elif addStudent success");
        jsonDataFromServer.message = jsonObject.message;
        renderAddStudentSuccess();
    }else if (jsonObject.message === "addStudentExists"){
        console.log("In responseListener() elif addStudent exists");
        jsonDataFromServer.message = jsonObject.message;
        renderAddStudentExists();
    }else if (jsonObject.message === "addStudentError"){
        console.log("In responseListener() elif addStudent error");
        jsonDataFromServer.message = jsonObject.message;
        renderAddStudentError();
    }else if(jsonObject.message === "removeStudentSuccess"){
        console.log("In responseListener() elif removeStudent success");
        jsonDataFromServer.message = jsonObject.message;
        renderRemoveStudentSuccess();
    }else if(jsonObject.message === "removeStudentError"){
        console.log("In responseListener() elif removestudent error");
        renderRemoveStudentError();
    }else if(jsonObject.message === "addQuestionSuccess"){
        console.log("In responseListener() elif addQuestionSuccess");
        renderAddQuestionSuccess();
    }else if(jsonObject.message === "addQuestionError"){
        console.log("In responseListener() elif addQuestionError");
        renderAddQuestionError();
    }else if(jsonObject.message === "getStartExamQuestionsSuccess") {
        console.log("In responseListner() elif getStartExamQuestionsSuccess");
         $scope.$apply(() => {
            $scope.user.examQuestionIDs = jsonObject.questionIds;
            $scope.user.examQuestionTexts = jsonObject.questionTexts;
        });
        sessionStorage.setItem("examQuestionIDs", JSON.stringify(jsonObject.questionIds));
        sessionStorage.setItem("examQuestionTexts", JSON.stringify(jsonObject.questionTexts));
        $scope.user.classid = sessionStorage.getItem("classid");
        console.log("In requestListener elif. $scope.user.classid= " + $scope.user.classid);
        window.location.href = "exam.html";
    }else if(jsonObject.message === "getStartExamQuestionsError") { 
        console.log("In responseListner() elif getStartExamQuestionsError");
        alert("Class or class/question linkages not found. Please try again.");
        window.location.href = "teachers.html";
    }else if(jsonObject.message === "submitExamInfoError") { 
        console.log("In responseListener() elif submitExamError");
        alert("Error submitting exam! Student or Student/Class pairing does not exist");
    }else if (jsonObject.message === "submitExamInfoSuccess") {
        alert.show("Exam data successfully submitted to database");
        window.location.href = "teachers.html";
    }
}

function renderAddQuestionError() {
    var outputText = document.getElementById('output2b');
    if(outputText){
        outputText.textContent = "Error adding question to database. Question/class pairing may already exist or you may be attempting to add a question to a class that is not in the system."
    }
}

function renderAddQuestionSuccess() {
    var outputText = document.getElementById('output2b');
    if(outputText){
        outputText.textContent = "The following question has been added successfully to class: " + sessionStorage.getItem("classQuestionID");
    }
    var outputText2 = document.getElementById('output3');
    if(outputText2){
        outputText.textContent = "Question text added: " + sessionStorage.getItem("addQuestionText");

    }
}

function renderAddStudentSuccess() {
    var outputText = document.getElementById('output');
    if(outputText){
        outputText.textContent = "Student successfully added!";
    }
}

function renderAddStudentExists() {
    var outputText = document.getElementById('output');
    if(outputText){
        outputText.textContent = "ERROR: Student/Class assignment already exists. To reassign this student, contact your VVEE administrator";
    }
}

function renderAddStudentError() {
    var outputText = document.getElementById('output');
    if(outputText){
        outputText.textContent = "Error attempting to add student. Please try again.";
    }
}

function renderRemoveStudentSuccess() {
    var outputText = document.getElementById('output2a');
    if(outputText){
        outputText.textContent = "Student successfully removed!";
    }
}

function renderRemoveStudentError() {
    var outputText = document.getElementById('output2a');
    if(outputText){
        outputText.textContent = "Error removing student. Student not in class/database!";
    }
}

app.config(function($mdThemingProvider) {
    $mdThemingProvider.theme('studentTheme')
            .primaryPalette('blue-grey')
            .accentPalette('orange')
            .warnPalette('red')
            $mdThemingProvider.theme('teacherTheme')
            .primaryPalette('teal')
            .accentPalette('orange')
            .warnPalette('red')
            $mdThemingProvider.theme('adminTheme')
            .primaryPalette('red')
            .accentPalette('orange')
            .warnPalette('red')
        $mdThemingProvider.theme('default')
            .primaryPalette('blue', {'default':'900'})
            .accentPalette('orange')
            .warnPalette('red')
});