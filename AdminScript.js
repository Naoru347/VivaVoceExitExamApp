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
    teacherClasses: [],
    classQuestionID: null,
    addQuestionText: null,
    selectedUserType: null,
    action: null,
    message: null
};

//A holder for JSON data from the server
let jsonDataFromServer = {
    message: "temp"
};
var app = angular.module("adminApp", ['ngMaterial', 'ngMessages']);

app.controller("control", function($scope, $mdToast) {


    //Set the inital state of our $scope.user object
    //based on sessionStorage data.
    $scope.user = {
        userid: sessionStorage.getItem("userid"),
        teacherid: sessionStorage.getItem("userid"),
        firstname: sessionStorage.getItem("firstname"),
        lastname: sessionStorage.getItem("lastname"),
    };

    

    //Redirect functions
    $scope.manageExamRecords = function() { 
        data.userid = sessionStorage.getItem("userid");
        data.firstname = sessionStorage.getItem("firstname");
        data.lastname = sessionStorage.getItem("lastname")
        window.location.href = 'examops.html';
    }
    $scope.manageUsers = function() {
        data.userid = sessionStorage.getItem("userid");
        data.firstname = sessionStorage.getItem("firstname");
        data.lastname = sessionStorage.getItem("lastname")
        window.location.href = 'userops.html';
    }
    $scope.manageExamQuestions = function() {
        data.userid = sessionStorage.getItem("userid");
        data.firstname = sessionStorage.getItem("firstname");
        data.lastname = sessionStorage.getItem("lastname")
        window.location.href = 'questionops.html';
    }
    $scope.go = function() {
        // alert("In grabDB");
        // console.log("In grabDB()");
        // $scope.user.action = "grabDB";
        // data.action = $scope.user.action;
        // sendDataToServer(data);
        window.location.href = 'login.html';

    }
    

    //Logout function as before
    $scope.logout = function() { 
        //Clear session storage
        sessionStorage.clear();
        window.location.href = 'login.html';
    };

    //Validation Code
    $scope.$watch('user.removeStudentID', function(newValue, oldValue) {
        if(newValue && !isIntegerString(newValue)) {
            showToast("Please enter a valid integer for Student ID");
        }
    });
    $scope.$watch('user.classQuestionID', function(newValue, oldValue) {
        if(newValue && !isValidFormat(newValue)) {
            showToast("Please enter a valid format: 2-4 letters followed by 3-4 digits, e.g., XYZ 1234 or AB123");
        }
    })
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