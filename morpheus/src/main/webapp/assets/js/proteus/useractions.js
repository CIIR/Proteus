function createUserWithName() {
    var name = $('#userCreateModal #userCreateName').val();
    $('#userCreateModal').modal('hide');
    $.ajax({
	       url: document.location.origin + '/users/' + name,
	       type : 'put',
	       async: true,
	       success: function() {
		   noty({text: 'Created user ' + name, 
			 type: 'success', 
			 layout: 'topCenter',
			 timeout: 1000});		   
	       },
	       error: function(data) {
		   noty({text: 'Could not create user ' 
			 + name + ':' + data, 
			 type: 'error',
			 layout: 'topCenter',
			 timeout: 1000});
	       }	   
	   });
}

function loginWithUserName() {
    var name = $('#userLoginModal #userLoginName').val();
    $('#userLoginModal').modal('hide');
    $.ajax({url: document.location.origin + '/sessions/' + name,
	    type: 'put',
	    async: true,
	    success: function(data) {
		// User is logged in. Must replace interface items
		$.cookie('session', data, {expires: 7, path: '/'});
		refreshAccountUI();
		refreshWorkspaceUI();
	    },
	    error: function(data) {
		noty({text: 'Could not log in user ' + name, 
		      type: 'error',
		      layout: 'topCenter',
		      timeout: 1000});
	    }
	   }); 
}

function logout(action) {
    $.ajax({url: document.location.origin + '/sessions/' + $.cookie('session'),
	    type: 'delete',
	    async: true,
	    success: function() {
		$.removeCookie('session');
		$.removeCookie('list');
		refreshAccountUI();
		refreshWorkspaceUI();
	    },
	    error: function(data) {
		noty({text: 'Could not log out user: ' + data, 
		      type: 'error',
		      layout: 'topCenter',
		      timeout: 1000});
	    }
	   }); 
}

function deleteLoggedInUser() {
    var sessionkey = $.cookie('session');
    if (sessionkey != null) {
	$.ajax({url : document.location.origin + '/users/' + sessionkey,
		type: 'delete',
		async: true,
		success: function () {
		    $.removeCookie('session');
		    $.removeCookie('list');
		    refreshAccountUI();
		    refreshWorkspaceUI();
		},
		error: function(data) {
		    noty({text: 'Unable to delete user: ' + data,
			  type: 'error',
			  layout: 'topRight',
			  timeout: 1000});
		}
	       });
    } else {
	noty({text: 'No user logged in.',
	      type: 'error',
	      layout: 'topRight',
	      timeout: 1000});
    } 
}

function getUserData() {
    var sessionkey = $.cookie('session');
    var receiver = null;
    if (sessionkey != null) {
	$.ajax({url : document.location.origin + '/users/' + sessionkey,
		type: 'get',
		async: false,
		dataType: 'json',
		success: function(data) { receiver = data; },
		error: function(data) {
		    noty({text: 'Could not retrieve user data: ' + data,
			  type: 'error',
			  layout: 'topRight`',
			  timeout: 1000});
		    receiver = null;
		}
	       });
	return receiver;
    } else {
	return null;
    }
}

// Renders the account-status components
// based on a session
function refreshAccountUI() {
    // If we have a session, get the user's name to display
    // for now.
    var userdata = getUserData();
    if (userdata != null) {
	$('#accountStatusText').text(userdata.user);
	$('#userBtnDropdown')
	    .empty()
	    .append('<li><a href="#" onclick="logout();">Logout</a></li>')
	    .append('<li><a href="#" onclick=\"deleteLoggedInUser();">Delete User</a></li>');	
    } else {
	$('#accountStatusText').text('Not logged in');
	$('#userBtnDropdown')
	    .empty()
	    .append('<li><a href="#userCreateModal" data-toggle="modal">Create New User</a></li>')
	    .append('<li><a href="#userLoginModal" data-toggle="modal">Log In</a></li>');   
    }
}