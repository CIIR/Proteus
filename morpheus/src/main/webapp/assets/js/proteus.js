// Use this to activate the correct entry from the navbar above.
$('.nav-collapse ul.nav li a[href="'+location.pathname+'"]').parent().addClass('active');

// Validation code

// Make sure the search bar isn't empty, otherwise complain.
// If it's not empty, collect the types clicked and just add
// repeated hidden inputs to the form
$('#searchform').submit(
    function() {
	if ($('#q').val().trim() == '') {
	    $('#searchform + #search_empty').remove();
	    $('#searchform').after('<div id="search_empty" class="alert alert-error span5"><button class="close" data-dismiss="alert" type="button" href="#">x</button>You\'re gonna want to enter some search text, chief.</div>');
	    return false;
	} else if ($('#typeselection > .active').length == 0) {
	    $('#searchform + #types_empty').remove();
	    $('#searchform').after('<div id=\"types_empty" class="alert alert-error span5"><button class="close" data-dismiss="alert" type="button" href="#">x</button>You must select at least one search type.</div>');
	    return false;
	} else {
	    // Find all selected buttons, and add inputs to the form
	    // to make processing on the backend easier.
	    var active = $('#typeselection').find('.active');
	    active.each( function() {
		$(this).after(
		    function(index) {			
			// st: selected type 
			return '<input type="hidden" name="st" value="'+
			    $(this).attr('name') +'" />';
		    });
			     return true;
			 });
	    // Finally throw in the sessionid id we have one
	    if ($.cookie("session") != null) {
		$('#searchform').append($('<input/>',
					  {type: 'hidden',
					   name: 'sessionid',
					   value: $.cookie("session").key}
					 ));
	    }
	}
});

// Activate the first result pane and check the types
// we searched for.
// Sets up the selected type buttons based on the result tabs
$(function() {
      // If a hash location is defined that matches a tab, select that.
      // Otherwise select the first tab
      if (location.hash != "") {
	  $("ul#resultTabs li a[href='" + location.hash + "']").tab('show');
      } else {
	  $('ul#resultTabs a:first').tab('show');
      }
      
      // Now select the type buttons
      var resultTypes = $('#resultTabs a');
      if (resultTypes.size() == 7) {  // <--- hard-coded numbers? Yuck. Bad Marc.
	  // Previous search did 'all'
	  $('#typeselection button[name="all"]').addClass('active');
      } else if (resultTypes.size() > 0) {
	  // Do 'em one by one.
	  var pattern = /#([a-z]+)tab/;
	  resultTypes.each(function() {
			       var buttonName = pattern.exec($(this).attr('href'))[1];
			       $('#typeselection button[name="'+buttonName+'"]').addClass('active');
			   });
      } else {
	  // None of the types have been previously selected
	  // (either no hits or first time) - default to first option.
	  $('#typeselection button').first().addClass('active');
      }
  });

// Causes the url to be updated when changing result tabs
$('ul#resultTabs li a').click(function () {location.hash = $(this).attr('href');});

// Validation for the wordhistory pane.
// Should be using notys for this.
$('#wordForm').submit(
    function() {
	if ($('#w').val().trim() == '') {
	    $('#wordForm + #word_empty'),remove();
	    $('#wordForm').after('div id="word_empty" class="alert alert-error span4"><button class="close" data-dismiss="alert" type="button" href="#">x</button>This would be way easier if you enter a search phrase.</div>');
	    return false;
	} else {
	    return true;
	}
    });

$.cookie.json = true;

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
			 layout: 'topRight',
			 timeout: 1000});		   
	       },
	       error: function(data) {
		   noty({text: 'Could not create user ' 
			 + name + ':' + data, 
			 type: 'error',
			 layout: 'topRight',
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
		// Store session as a JS object
		var sessionData = Object();
		sessionData.key = data;
		$.cookie('session', sessionData, {expires: 1, path: '/'});
		markResults();
		refreshAccountUI();
		refreshWorkspaceUI();
	    },
	    error: function(data) {
		noty({text: 'Could not log in user ' + name, 
		      type: 'error',
		      layout: 'topRight',
		      timeout: 1000});
	    }
	   }); 
}

function logout(action) {
    if ($.cookie('session') == null) return;
    $.ajax({url: document.location.origin + '/sessions/' + $.cookie('session').key,
	    type: 'delete',
	    async: true,
	    success: function() {
		$.removeCookie('session');
		refreshAccountUI();
		refreshWorkspaceUI();
	    },
	    error: function(data) {
		noty({text: 'Could not log out user: ' + data, 
		      type: 'error',
		      layout: 'topRight',
		      timeout: 1000});
	    }
	   }); 
}

function deleteLoggedInUser() {
    if ($.cookie('session') == null) return;    
    var sessionkey = $.cookie('session').key;
    if (sessionkey != null) {
	$.ajax({url : document.location.origin + '/users/' + sessionkey,
		type: 'delete',
		async: true,
		success: function () {
		    $.removeCookie('session');
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
    if ($.cookie('session') == null) return null;
    var sessionkey = $.cookie('session').key;
    var receiver = null;
    if (sessionkey != null) {
	$.ajax({url : document.location.origin + '/users/' + sessionkey,
		type: 'get',
		async: false,
		dataType: 'json',
		success: function(data) { receiver = data; },
		error: function(data) { receiver = null; }
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
	// Do not maintain a session if we can't get user data
	$.removeCookie('session');
	$('#accountStatusText').text('Not logged in');
	$('#userBtnDropdown')
	    .empty()
	    .append('<li><a href="#userCreateModal" data-toggle="modal">Create New User</a></li>')
	    .append('<li><a href="#userLoginModal" data-toggle="modal">Log In</a></li>');
    }
}

// True if an item has the label of a particular list.
// False otherwise.
//
// Ideally, this is in total sync with the state of the list
// containing the item on the server (note that these two conditions)
// only *should* be equivalent. Extra care must be taken to ensure this.)
function hasLabel(id, listname) {
    var selection = $('span[id="'+id+'-labels"] span:contains("'+listname+'")');
    return (selection.size() > 0);
}

// Adds a label of a list to an item. 
function addLabel(id, listname) {
    $('span[id="'+id+'-labels\"]')
	.append('<span class="badge">'+listname
		+'<i class="icon-remove icon-white" onclick="unMarkItem(\''+id+'\',\''+listname+'\');"></i></span>');    
}

// Removes a label of a list from an item.
// Does no consistency checking.
function removeLabel(id, listname) {
    $('span[id="'+id+'-labels\"] span:contains("'+listname+'")').remove();    
}

function markItem(id, datatype) {
    if ($.cookie('session') == null) return;
    var listname = $.cookie('session').list;
    var sessionkey = $.cookie('session').key;
    if (listname == null || sessionkey == null) return;

    if (hasLabel(id, listname)) return;

    // Stick the label next to the entry
    addLabel(id, listname);

    // Add the entry to the current list
    var fulltitle = $("span[id='" + id + "'] u").text().trim();
    var shorthand;
    if (fulltitle.length > 25) {
	shorthand = fulltitle.substr(0, 20) + "...";
    } else {
	shorthand = fulltitle;
    }
    var tooltip = '';
    if (shorthand != fulltitle) {
	tooltip = 'rel="tooltip" data-original-title="'+fulltitle+'"';
    }
    $("#listDisplay").append("<tr id='"+id+"'><td><i class=\"icon-remove\" onclick=\"unMarkItem('"+id+"');\"></i></td><td>" 
			     +"<a "+fulltitle+"\" href=\"/details?"+id+"\" class=\""+datatype+"\">"+shorthand+"</a>" 
			     +"</td></tr>");
    $.ajax({url: document.location.origin 
	    +'/items/'+id+'/'+listname+'/'+sessionkey,
	    data: { title: shorthand,
		    datatype: datatype,
		    fulltitle: fulltitle 
		  },
	    type: 'put'});
}

function unMarkItem(id, listname) {
    if ($.cookie('session') == null) return;
    listname = (listname != null) ? listname : $.cookie('session').list;
    var sessionkey = $.cookie('session').key;
    if (listname == null || sessionkey == null) return;    
    removeLabel(id, listname);
    $("#listDisplay tr[id='" + id + "']").remove();
    $.ajax({url: document.location.origin 
	    +'/items/'+id+'/'+listname+'/'+sessionkey,
	    type: 'delete'});
}

function selectList(listname) {
    if ($.cookie('session') == null) return;
    var sessionData = $.cookie('session');
    sessionData.list = listname;
    $.cookie('session', sessionData);
    refreshWorkspaceUI();
}

function createList() {
    if ($.cookie('session') == null) return;
    var sessionkey = $.cookie('session').key;
    if (sessionkey == null) return;
    var name = $('#listCreateModal #listCreateName').val();
    $('#listCreateModal').modal('hide');
    $.ajax({url: document.location.origin + '/lists/' + name + '/' + sessionkey,
	    type: 'put',
	    success: selectList(name),
	    error: function(data) {
		noty({text: 'Could not create list:' + data, 
		      type: 'error',
		      layout: 'topRight',
		      timeout: 1000});
	    }
	   });
}

function renameList() {
    if ($.cookie('session') == null) return;
    var sessionkey = $.cookie('session').key;
    if (sessionkey == null) return;
    $('#listRenameModal').modal('hide');
    var oldName = $('#listStatusText').text();
    var newName = $('#listRenameModal #listNewName').val();
    $.ajax({url: document.location.origin + '/lists/' + oldName + '/' + sessionkey,
	    type: 'post',
	    data: { newname: newName },
	    success : selectList(newName),
	    error: function(data) {
		noty({text: 'Could not rename list:' + oldName, 
		      type: 'error',
		      layout: 'topRight',
		      timeout: 1000});
	    }
	   });    
}

function deleteCurrentList() {
    if ($.cookie('session') == null) return;
    var listname = $.cookie('session').list;
    var sessionkey = $.cookie('session').key;
    if (listname == null || sessionkey == null) return null;
    $.ajax({url: document.location.origin + '/lists/' + listname + '/' + sessionkey,
	    type: 'delete',
	    success: function() {
		var sessionData = $.cookie('session');
		sessionData.list = null;
		$.cookie('session', sessionData);	
		refreshWorkspaceUI();
	    },
	    error: function(data) {
		noty({text: 'Could not delete list:' + data, 
		      type: 'error',
		      layout: 'topRight',
		      timeout: 1000});
	    }
	   });
}

function getListItems() {
    if ($.cookie('session') == null) return;
    var listname = $.cookie('session').list;
    var sessionkey = $.cookie('session').key;
    if (listname == null || sessionkey == null) {
	return null;
    } 

    var receiver;
    $.ajax({url: document.location.origin + '/lists/' + listname + '/' + sessionkey,
	    type: 'get',
	    async: false,
	    dataType: 'json',
	    success: function(data) { receiver = data; },
	    error: function(data) {
		noty({text: 'Could not retrieve list contents: ' + data,
		      type: 'error',
		      layout: 'topRight`',
		      timeout: 1000});
		receiver = null;
	    }
	   });
    return receiver;    
}

function getLists() {
    if ($.cookie('session') == null) return;
    var sessionkey = $.cookie('session').key;
    var receiver;
    if (sessionkey != null) {
      	$.ajax({url: document.location.origin + '/lists/' + sessionkey,
		type: 'get',
		async: false,
		dataType: 'json',
		success: function(data) { receiver = data; },
		error: function(data) {
		    noty({text: 'Could not retrieve user lists: ' + data,
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

function refreshWorkspaceUI() {
    $('#listBtnDropdown').empty();
    $('#listDisplay').empty();

    // Make sure we have a user
    if ($.cookie('session') == null) {
	// Do not allow list management if a user is not logged in
	$('#listStatusText').text('Log in to use lists.');
	// Make sure the badges are all gone
	$('span.badge').remove();
	return;
    }

    // Load the lists for this user
    var lists = getLists();
    if (lists != null) {
	for (var i = 0; i < lists.length; ++i) {
	    var item = lists[i];
	    $('#listBtnDropdown')
		.append('<li><a href="#" onclick="selectList(\''+item+'\');">'+item+'</a></li>');
	}	
    }
    $('#listBtnDropdown').append('<li class="divider"></li>');
    $('#listBtnDropdown').append('<li><a href="#listCreateModal" data-toggle="modal">Create New List...</a></li>');

    // Now try to populate a chosen list
    var listname = $.cookie('session').list;
    if (listname != null) {
	$('#listStatusText').text(listname);
	var items = getListItems();
	for (var i = 0; i < items.length; ++i) {
	    var data = items[i];
	    var tooltip = '';
	    if (data.fulltitle != null && data.fulltitle != data.title) {
		tooltip = 'rel="tooltip" data-original-title="'+data.fulltitle+'"';
	    }
	    var newHTML = '<tr id="'+data.itemid+'"><td>'
		+ '<i class="icon-remove" onclick="unMarkItem(\''+data.itemid+'\');"></i></td>'
		+ '<td><a '+tooltip+' href="/details?id='+data.itemid+'" class="'+data.datatype+'">' +data.title+ '</a>'
		+ '</td></tr>';	    
	    $('#listDisplay').append(newHTML);
	}
	$('#listBtnDropdown').append('<li><a href="#listRenameModal" data-toggle="modal">Rename current list</a></li>');
	$('#listBtnDropdown').append('<li><a href="#" onclick="deleteCurrentList();">Delete current list</a></li>');
	$('#listBtnDropdown').append('<li class="divider"></li>');
	// Stuff to do with it?
	$('#listBtnDropdown').append('<li id="expandRocchio"><span><a href="#" onclick="toggleExpansion(\'Rocchio\');">Expand with Rocchio</a></span></li>');
	$('#listBtnDropdown').append('<li id="expandLinks"><span><a href="#" onclick="toggleExpansion(\'Links\');">Expand via object links</a></span></li>');
    } else {
	$('#listStatusText').text('No List Selected.');
    }
}

function toggleExpansion(expander) {
    if ($.cookie('session') == null) return;
    var expName = "expand" + expander;
    var selector = 'li[id="'+expName+'"] span';
    var selection = $(selector);
    if ($.cookie('session').expanders == null) {
	$.cookie('session').expanders = Array();
    }
    var sessionData = $.cookie('session');
    if (selection.hasClass('icon-ok')) {
	selection.removeClass('icon-ok');
	var position = $.inArray(sessionData.expanders, expander);
	if (~position) {
	    sessionData.expanders.splice(position, 1);
	    $.cookie('session', sessionData);
	}
    } else {
	selection.addClass('icon-ok');
	sessionData.expanders.push(expander);
	$.cookie('session', sessionData);
    }
}

// Used to mark search results when a user logs in (otherwise
// labeling doesn't apply until a new search is submitted).
var whatever;
function markResults() {
    // Look for results
    if ($.cookie('session') == null) return;
    var resultSpans = $('#resultContent td.span10 span');
    if (resultSpans.size() == 0) return;

    // Simple solution - if we have results and have just logged in,
    // reload the damn page
    $('#searchform').submit();
}

// Update stuff based on current cookie contents
refreshAccountUI();
refreshWorkspaceUI();