
//Servlet information for server-side processing
let url = "http://localhost:40106/examservlet";

//A slug for all possible data that might need to be sent to the server
let form = {
    userid: 'null',
    username: 'null',
    password: 'null',
    usertype: 'null',
    firstname: 'null',
    lastname: 'null',
    classid: 'null', 
    teacher: 'null',
    teacherid: 'null',
    studentid: 'null',
    questionid: 'null',
    examid: 'null',
    pointspossible: 'null',
    pointsearned: 'null',
    examQuestions: [],
    action: 'null',
    message: 'null'

};

//Holder for JSON data from the server
let jsonDataFromServer = {
    message: "temp"
};

var app = angular.module("examApp", ['ngMaterial']);
    app.controller("control", function($scope) {

        $scope.login = function() {
            form.username = $scope.user.username;
            form.password = $scope.user.password;
            form.action = "login";
            sendDataToServer($scope);
        };
        
         // Retrieve and set user data from sessionStorage
         $scope.user = {
            userid: sessionStorage.getItem("userid"),
            firstname: sessionStorage.getItem("firstname"),
            lastname: sessionStorage.getItem("lastname"),
            classid: sessionStorage.getItem("classid"),
            pointspossible: parseInt(sessionStorage.getItem("pointspossible"), 10),
            pointsearned: parseInt(sessionStorage.getItem("pointsearned"), 10),
            examQuestions: JSON.parse(sessionStorage.getItem("examQuestions")) || []
        };

        $scope.getStudentResults = function() {
            getExamResults($scope);
        }
        
        $scope.logout = function() {
            // Clear sessionStorage
            sessionStorage.clear();
            // Redirect to login.html
            window.location.href = 'login.html';
        };
    });

    function sendDataToServer($scope) {
        for (let key in $scope.user) {
            if ($scope.user.hasOwnProperty(key) && form.hasOwnProperty(key)) {
                form[key] = $scope.user[key];
            }
        }
        console.log("In sendDataToServer: form.user=" + form.userid);
        let req = new XMLHttpRequest();
    
        req.addEventListener("load", function() {
            requestListener(this.responseText, $scope);
        });
    
        req.open("POST", url);
        req.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
        req.send(JSON.stringify(form));
        console.log("Sent to server: json=" + JSON.stringify(form));
    }
    
    function requestListener(responseText, $scope) {
        // Parse response and update jsonDataFromServer
        let jsonObject = JSON.parse(responseText);
        jsonDataFromServer.message = jsonObject.message;
        console.log("MESSAGE FROM SERVER: " + jsonObject.message);
        $scope.user.message = jsonDataFromServer.message;
        // Control flow, based on message contents
        console.log("In requestListener() BEFORE if getExamQuestionsFailed message=" + jsonObject.message);
        if(jsonObject.message === "getExamQuestionsError") {
            console.log("In requestListener()if getExamQuestionsFailed");
            var oe = document.getElementById("output");
            oe.textContent = "Exam record not found. Please contact your instructor for more information.";
        } 
        if (jsonObject.message === "loginfail") {
            alert("Login not found. Please try.");
        }else if (jsonObject.message === "loginsuccess") {
            console.log("userid: " + jsonObject.userid + ", usertype: " + jsonObject.usertype);
            sessionStorage.setItem("userid", jsonObject.userid);
            sessionStorage.setItem("usertype", jsonObject.usertype);
            sessionStorage.setItem("firstname", jsonObject.firstname);
            sessionStorage.setItem("lastname", jsonObject.lastname);
    
            // Update the $scope.user with the response data
            $scope.$apply(function () {
                $scope.user = jsonObject; 
            });

            //Update form fields with server responses
            for(let key in jsonObject) {
                if(jsonObject.hasOwnProperty(key)){
                    form[key] = jsonObject[key];
                    console.log("In loginsuccess for loop. Updating form:" + form[key]);
                }
            }
    
            // Redirect based on usertype
            redirectToUserPage(jsonObject.usertype, $scope);
        } else if (jsonObject.message === "getClassSuccess"){
            console.log("In getClassSuccess if statement.");
            console.log("classid: " + jsonObject.classid + " for user " + jsonObject.username);
            sessionStorage.setItem("classid", jsonObject.classid);
            
            $scope.$apply(() => {
                $scope.user.classid = jsonObject.classid;
                form.classid = $scope.user.classid;
            });
            
            for(let key in jsonObject) {
                if(jsonObject.hasOwnProperty(key)) {
                    form[key] = jsonObject[key];
                    console.log("In getClassSuccess for loop. Updating form:" + form[key]);
                }
            }
            console.log("Received from server: " + jsonObject.username);
            $scope.$apply(function() {
                $scope.user = jsonObject;
            });
        } else if (jsonObject.message === "getStudentExamSuccess") {
            console.log("In getStudentExamSuccess if statement:");
            console.log("examid: " + jsonObject.examid + " total score=" + jsonObject.pointsearned);
            sessionStorage.setItem("pointspossible", jsonObject.pointspossible);
            sessionStorage.setItem("pointsearned", jsonObject.pointsearned);
            sessionStorage.setItem("examQuestions", jsonObject.examQestions);

            $scope.$apply(() => {
                $scope.user.pointspossible = jsonObject.pointspossible;
                $scope.user.pointsearned = jsonObject.pointsearned;
                $scope.user.examQuestions = jsonObject.examQuestions;
            });
            for(let key in jsonObject) {
                if(jsonObject.hasOwnProperty(key)) {
                    form[key] = jsonObject[key];
                    console.log("In getStudentExamSuccess for loop. Updating form:" + form[key]);
                }
            }
            console.log("Received from server: " + jsonObject.pointsearned);
            createExamTable($scope);
            calculateAndDisplayScore($scope);
        }
        console.log("Howd you get here?");
    }

    function redirectToUserPage(usertype, $scope) {
        switch (usertype) {
            case '0': // Admin
                window.location.href = "admin.html";
                break;
            case '1': // Teacher
                window.location.href = "teachers.html";
                break;
            case '2': // Student
            getClassInfo($scope);    
            window.location.href = "students.html";
                break;
            default:
                alert("Error. Please refresh your session and try again.");
        }
    }    

    function getClassInfo($scope) {
        if ($scope.user) {
            $scope.user.action = "getClassInfo";
            console.log("In getClassInfo. action=" + $scope.user.action);
            form.action = $scope.user.action;
            sendDataToServer($scope);
        } else {
            console.error('User object is undefined.');
        }
    }

    function getExamResults($scope) {
        console.log("in getExamResults #1: $scope.user=" + $scope.user.userid);
        if($scope.user) { 
            $scope.user.action = "getStudentExamInfo";
            console.log("In getExamResults. action=" + $scope.user.action);
            form.action = $scope.user.action;
            console.log("in getExamResults #2: $scope.user=" + $scope.user.userid);
            sendDataToServer($scope);
            if($scope.user.message === "getStudentExamError") {
                alert("No exam record on file. Please contact your instructor.");
            }
        }
    }

    // Function to create exam table
    function createExamTable($scope) {
        console.log("In createExamTable: examQuestions=" + $scope.user.examQuestions);
        var outputElement = document.getElementById('output');
        if (outputElement && $scope.user.examQuestions.length > 0) {
            var table = document.createElement('table');

            // Create table header
            var thead = document.createElement('thead');
            var headerRow = document.createElement('tr');
            var questionHeader = document.createElement('th');
            questionHeader.textContent = 'Question';
            var feedbackHeader = document.createElement('th');
            feedbackHeader.textContent = 'Feedback';
            headerRow.appendChild(questionHeader);
            headerRow.appendChild(feedbackHeader);
            thead.appendChild(headerRow);
            table.appendChild(thead);

            // Create table body with exam questions
            var tbody = document.createElement('tbody');
            $scope.user.examQuestions.forEach(function(question) {
                var row = document.createElement('tr');
                var questionCell = document.createElement('td');
                questionCell.textContent = question.question;
                var feedbackCell = document.createElement('td');
                feedbackCell.textContent = question.feedback;
                row.appendChild(questionCell);
                row.appendChild(feedbackCell);
                tbody.appendChild(row);
            });
            table.appendChild(tbody);

            // Clear existing content and append the table
            outputElement.innerHTML = '';
            outputElement.appendChild(table);
        }
    }
    
    function calculateAndDisplayScore ($scope) {
        console.log("In calculateAndDisplayScore");
        console.log("Points Possible: ", $scope.user.pointspossible);
        console.log("Points Earned: ", $scope.user.pointsearned);
    
        if ($scope.user.pointspossible && $scope.user.pointsearned) {
            var percentage = ($scope.user.pointsearned / $scope.user.pointspossible) * 100;
            console.log("Calculated Percentage: ", percentage);
    
            var output2Element = document.getElementById('output2');
            if (output2Element) {
                output2Element.textContent = 'Score: ' + $scope.user.pointsearned + '/' + $scope.user.pointspossible + ' (' + percentage.toFixed(2) + '%)';                    
                console.log("Output Element Text: ", output2Element.textContent);
            } else {
                console.log("Output2 Element not found");
            }
        } else {
            console.log("Points Possible or Earned are not defined");
        }
    }

    //Setting up desired themeing
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