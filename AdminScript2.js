//Servlet information for server-side processing
let url = "http://localhost:40106/examservlet";

//A slug object for all possible data that we might need to send to the server
let data = {
    addfirstname: null,
    addlastname: null,
    addusername: null,
    addpassword: null,
    addusertype: null,
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
    modifyUserID: null,
    modifyStudentClass: null,
    removequestionid: null,
    removeclassid: null,
    questionid: null,
    examid: null,
    pointspossible: null,
    pointsearned: null,
    examQuestions: [],
    teacherClasses: [],
    classQuestionID: null,
    addQuestionText: null,
    removeexamid: null,
    modifyexamid: null,
    modifyscore: null,
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
        addusertype: '',
        modifyUserID: '',
        addpassword: '',
        removequestionid: '',
        removeclassid: '',
        removeexamid: '',
        modifyexamid: '',
        modifyscore: ''
    };

    $scope.getUserType = function(userType) {
        switch (userType) {
            case '0': return 'Administrator';
            case '1': return 'Teacher';
            case '2': return 'Student';
            default: return 'Unknown';
        }
    };

    $scope.removeQuestion = function() { 
            data.removequestionid = $scope.user.removequestionid;
            data.removeclassid = $scope.user.removeclassid;
            console.log("In removeQuestion() attempting to remove: " + $scope.user.removeclassid + " and " + $scope.user.removequestionid);
            $scope.user.action = "adminRemoveLinkages";
            data.action = $scope.user.action;
            sendDataToServer($scope);
    };

    $scope.addUser = function() {
        console.log("In addUser()");
        $scope.user.action = "adminAddUser";
        data.action = $scope.user.action;
        data.addfirstname = $scope.user.addfirstname;
        data.addlastname = $scope.user.addlastname;
        data.addusername = $scope.user.addusername;
        data.addpassword = $scope.user.addpassword;
        data.addusertype = $scope.user.addusertype;
        sessionStorage.setItem("adduser", $scope.user.addusername);
        console.log("Attempting to add user: " + $scope.user.addusername);
        sendDataToServer($scope);
    };

    $scope.removeUser = function() { 
        console.log("In removeUser()");
        $scope.user.action = "adminRemoveUser";
        data.action = $scope.user.action;
        data.modifyUserID = $scope.user.removeUserID;
        console.log("Attempting to remove user: " + $scope.user.removeUserID);
        sendDataToServer($scope);
    };

    $scope.displayQuestionDB = function() {
        console.log("In displayQuestionDB()");
        $scope.user.action = "grabQuestionDB";
        data.action = $scope.user.action;
        sendDataToServer($scope);

    };

    $scope.displayLinkagesDB = function() {
        console.log("In displayLinkagesDB()");
        $scope.user.action = "grabLinkagesDB";
        data.action = $scope.user.action;
        sendDataToServer($scope);
    };

    $scope.removeExamRecord = function() { 
        console.log("In removeExamRecord()");
        $scope.user.action = "removeExamRecord";
        data.action = $scope.user.action;
        data.removeexamid = $scope.user.removeexamid;
        sendDataToServer($scope);
    };

    $scope.modifyExamRecord = function() { 
        console.log("In modifyExamRecord()");
        $scope.user.action = "modifyExamRecord";
        data.action = $scope.user.action;
        data.modifyexamid = $scope.user.modifyexamid;
        data.modifyscore = $scope.user.modifyscore;
        sendDataToServer($scope);
    };

    //Redirect functions
    $scope.go = function() {
        console.log("In grabDB()");
        $scope.user.action = "grabDB";
        data.action = $scope.user.action;
        sendDataToServer($scope);

    };
    $scope.clear = function() { 
        console.log("Attempting to clear");
        var outputElement = document.getElementById('usersOutput');
        if(outputElement) {
            outputElement.innerHTML = '';
        }
    };
    

    //Logout function as before
    $scope.logout = function() { 
        //Clear session storage
        sessionStorage.clear();
        window.location.href = 'login.html';
    };

    //Validation Code
    $scope.$watch('user.removeUserID', function(newValue, oldValue) {
        if(newValue && !isIntegerString(newValue)) {
            showToast("Please enter a valid integer for User ID");
            $scope.user.removeUserID = '';
        }
    });
    $scope.$watch('user.removeexamid', function(newValue, oldValue) {
        if(newValue && !isIntegerString(newValue)) {
            showToast("Please enter a valid integer for Exam ID");
            $scope.user.removeexamid = '';
        }
    });
    $scope.$watch('user.modifyexamid', function(newValue, oldValue) {
        if(newValue && !isIntegerString(newValue)) {
            showToast("Please enter a valid integer for Exam ID");
            $scope.user.modifyexamid = '';
        }
    });
    $scope.$watch('user.modifyscore', function(newValue, oldValue) {
        if(newValue && !isIntegerString(newValue)) {
            showToast("Please enter a valid integer for New Score Value");
            $scope.user.modifyscore = '';
        }
    });
    $scope.$watch('user.removequestionid', function(newValue, oldValue) {
        if(newValue && !isIntegerString(newValue)) {
            showToast("Plese enter a valid integer for Question ID");
            $scope.user.removequestionid = '';
        }
    });
    $scope.$watch('user.removeclassid', function(newValue, oldValue) {
        if(newValue && !isValidFormat(newValue)) {
            showToast("Please enter a valid format: 2-4 letters followed by 3-4 digits, e.g., XYZ 1234 or AB123");
        }
    });
    $scope.$watch('user.addusertype', function(newValue, oldValue) {
        // Check if newValue is a non-empty string and not an integer
        if(newValue && isNaN(parseInt(newValue))) {
            showToast("Please enter a valid integer for the user type. 0-Admin, 1-Teacher, 2-Student");
            $scope.user.addusertype = ''; // Reset the input
        }
    });
    $scope.$watch('user.addpassword', function(newValue, oldValue) {
        // Check if the password is less than 8 characters
        if(newValue && newValue.length < 8) {
            showToast("Password must be at least 8 characters long.");
            $scope.user.addpassword = '';
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
    console.log("In AdminScript2.sendDataToServer: data=" + data);
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
    if(jsonObject.message === "grabUserDBSuccess") {
        console.log("Received user data from server");
        $scope.$apply(() => {
            $scope.users = jsonObject.users; 
        });
        createUserTable($scope);
    }else if(jsonObject.message === "adminRemoveUserSuccess") {
        var outputElement = document.getElementById('removeuserresult');
        if(outputElement) {
            outputElement.textContent = "User successfully removed from database. To verify, click on \"Display Users\", above.";
            $scope.user.modifyUserID = '';
        }
    }else if(jsonObject.message === "adminRemoveUserError") {
        console.log("In requestListener elif @" + jsonObject.message);
        var outputElement = document.getElementById('removeuserresult');
        if(outputElement) {
            outputElement.textContent = "Error removing user from database. If you feel this result is in error please try again or contact your institutions database administrator. If you are that person, ask for a pay raise.";
            $scope.user.modifyUserID = '';
        }
    }else if(jsonObject.message === "adminAddUserSuccess") {
        var outputElement = document.getElementById('adduserresult');
        if(outputElement) {
            outputElement.textContent = $scope.user.addusername + " successfully add to database. To verify, click on \"Display Users\", above.";
            $scope.user.modifyUserID = '';
        }
    }else if(jsonObject.message === "grabQuestionDBError") {
        console.log("In requestListener elif @" + jsonObject.message);
        var outputElement = document.getElementById('usersOutput');
        if(outputElement) {
            outputElement.textContent = "Database error. Please try again later. If this error persists, please contact your institution's database manager. If this is you, ask for a pay raise.";
        }
    }else if(jsonObject.message === "grabQuestionDBSuccess") {
        console.log("Received question data from server");
        $scope.$apply(() => {
            $scope.questions = jsonObject.questions;
        });
        createQuestionTable($scope);
    }else if(jsonObject.message === "grabLinkagesDBSuccess") {
        console.log("Received question data from server");
        $scope.$apply(() => {
            $scope.linkages = jsonObject.linkages;
        });
        createLinkagesTable($scope);
    }else if(jsonObject.message === "grabLinkagesDBError") {
        console.log("Database error.");
        var outputElement = document.getElementById('usersOutput');
        if(outputElement) {
            outputElement.textContent = "Database error. Please try again later. If this error persists, please contact your institution's database manager. If this is you, ask for a pay raise.";
        }
    }else if(jsonObject.message === "removeLinkageSuccess") {
        console.log("Database error.");
        var outputElement = document.getElementById('output');
        if(outputElement) {
            outputElement.textContent = "Question/Class linkage successfully removed";
        }
    }else if(jsonObject.message === "removeLinkageError") {
        console.log("Database error.");
        var outputElement = document.getElementById('output');
        if(outputElement) {
            outputElement.textContent = "Database error. Please try again later. If this error persists, please contact your institution's database manager. If this is you, ask for a pay raise.";
        }
    }else if(jsonObject.message === "updateExamScoreSuccess") {
        console.log("Database error.");
        var outputElement = document.getElementById('output2');
        if(outputElement) {
            outputElement.textContent = "Student exam record successfully change!";
        }
    }else if(jsonObject.message === "updateExamScoreError") {
        console.log("Database error.");
        var outputElement = document.getElementById('output2');
        if(outputElement) {
            outputElement.textContent = "Database error. Please try again later. If this error persists, please contact your institution's database manager. If this is you, ask for a pay raise.";
        }
    }else if(jsonObject.message === "removeExamRecordSuccess") {
        console.log("Database error.");
        var outputElement = document.getElementById('output1');
        if(outputElement) {
            outputElement.textContent = "Student exam record successfully removed!";
        }
    }else if(jsonObject.message === "removeExamRecordError") {
        console.log("Database error.");
        var outputElement = document.getElementById('output1');
        if(outputElement) {
            outputElement.textContent = "Database error. Please try again later. If this error persists, please contact your institution's database manager. If this is you, ask for a pay raise.";
        }
    }else{
        console.log("How'd you get here?");
    }
}

function createLinkagesTable($scope) {
    var outputElement = document.getElementById('usersOutput');
    if (outputElement && $scope.questions.length > 0) {
        var table = document.createElement('table');

        // Create table header
        var thead = document.createElement('thead');
        var headerRow = document.createElement('tr');
        ['Question ID', 'Class ID'].forEach(headerText => {
            var header = document.createElement('th');
            header.textContent = headerText;
            headerRow.appendChild(header);
        });
        thead.appendChild(headerRow);
        table.appendChild(thead);

        // Create table body with question data
        var tbody = document.createElement('tbody');
        $scope.linkages.forEach(function(linkage) {
            var row = document.createElement('tr');
            row.appendChild(createCell(linkage.questionid));
            row.appendChild(createCell(linkage.classid));
            tbody.appendChild(row);
        });
        table.appendChild(tbody);

        // Clear existing content and append the table
        outputElement.innerHTML = '';
        outputElement.appendChild(table);
    }
}

function createQuestionTable($scope) {
    var outputElement = document.getElementById('usersOutput');
    if (outputElement && $scope.questions.length > 0) {
        var table = document.createElement('table');

        // Create table header
        var thead = document.createElement('thead');
        var headerRow = document.createElement('tr');
        ['Question ID', 'Question Text'].forEach(headerText => {
            var header = document.createElement('th');
            header.textContent = headerText;
            headerRow.appendChild(header);
        });
        thead.appendChild(headerRow);
        table.appendChild(thead);

        // Create table body with question data
        var tbody = document.createElement('tbody');
        $scope.questions.forEach(function(question) {
            var row = document.createElement('tr');
            row.appendChild(createCell(question.questionid));
            row.appendChild(createCell(question.question));
            tbody.appendChild(row);
        });
        table.appendChild(tbody);

        // Clear existing content and append the table
        outputElement.innerHTML = '';
        outputElement.appendChild(table);
    }
}

function createUserTable($scope) {
    var outputElement = document.getElementById('usersOutput');
    if (outputElement && $scope.users.length > 0) {
        var table = document.createElement('table');

        // Create table header
        var thead = document.createElement('thead');
        var headerRow = document.createElement('tr');
        ['UserID', 'Username', 'User Type', 'First Name', 'Last Name'].forEach(headerText => {
            var header = document.createElement('th');
            header.textContent = headerText;
            headerRow.appendChild(header);
        });
        thead.appendChild(headerRow);
        table.appendChild(thead);

        // Create table body with user data
        var tbody = document.createElement('tbody');
        $scope.users.forEach(function(user) {
            var row = document.createElement('tr');
            row.appendChild(createCell(user.userid));
            row.appendChild(createCell(user.username));
            //Replace int value from the DB with text equivalent
            row.appendChild(createCell($scope.getUserType(user.usertype)));
            row.appendChild(createCell(user.firstname));
            row.appendChild(createCell(user.lastname));
            tbody.appendChild(row);
        });
        table.appendChild(tbody);

        // Clear existing content and append the table
        outputElement.innerHTML = '';
        outputElement.appendChild(table);
    }
}

//helper function to help build the table while allowing for nested function calls
function createCell(text) {
    var cell = document.createElement('td');
    cell.textContent = text;
    return cell;
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