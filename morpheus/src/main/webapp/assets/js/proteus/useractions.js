function createUserWithName() {
    var name = $('#userCreateModal #userCreateName').val();
    $('#userCreateModal').modal('hide');
    $.ajax({
	       url: "http://ayr.cs.umass.edu:8080/createUser/" + name,
	       async: true,
	       success: function(data) {
		   noty({text: 'Created user ' + data, 
			 type: 'success', 
			 layout: 'topCenter',
			 timeout: 1000});		   
	       },
	       error: function(data) {
		   noty({text: 'Could not create user ' + data, 
			 type: 'error',
			 layout: 'topCenter',
			 timeout: 1000});
	       }	   
});
}
 
function loginWithUserName() {
    var name = $('#userLoginModal #userLoginName').val();
    $('#userLoginModal').modal('hide');
    $.ajax({
	       url: "http://ayr.cs.umass.edu:8080/login/" + name,
	       async: true,
	       success: function(data) {
		   // User is logged in. Must replace interface items
		   // Change log in display
		   $('#userBtn')
		       .empty()
		       .append(data+'  ')
		       .append($('<span/>').addClass('caret'));
		   // Change dropdown options
		   $('#userBtnDropdown')
		       .empty()
		   .append('<li><a href="#" onclick="logout(\'logout\');">Logout</a></li>')
		   .append('<li><a href="#" onclick=\"logout(\'delete\');">Delete User</a></li>');
	       },
	       error: function(data) {
		   noty({text: 'Could not log in user ' + data, 
			 type: 'error',
			 layout: 'topCenter',
			 timeout: 1000});
	       }	   
}); 
}

function logout(action) {
    $.ajax({
	       url: "http://ayr.cs.umass.edu:8080/"+action+"/"+ name,
	       async: true,
	       success: function(data) {
		   // Change back to 'not logged in' mode.
		   $('#userBtn')
		       .empty()
		       .append('Not Logged In  ')
		       .append($('<span/>').addClass('caret'));
		   $('#userBtnDropdown')
		       .empty()
		   .append('<li><a href="#userCreateModal" data-toggle="modal">Create New User</a></li>')
		   .append('<li><a href="#userLoginModal" data-toggle="modal">Log In</a></li>');
	       },
	       error: function(data) {
		   noty({text: 'Could not log out user: ' + data, 
			 type: 'error',
			 layout: 'topCenter',
			 timeout: 1000});
	       }
}); 
}