function markItem(id) {
    var listname = $.cookie('list');
    var sessionkey = $.cookie('session');
    if (listname == null || sessionkey == null) return;
    
    $("span[id='"+id+ "'] i").removeClass("icon-tag");
    $("span[id='"+id+ "'] i").addClass("icon-ok");
    $("span[id='"+id+ "'] i").removeAttr("onclick");
    // Add the entry to the "tagged items bag"
    var shorthand = $("span[id='" + id + "'] u").text().trim();
    if (shorthand.length > 25) {
	shorthand = shorthand.substr(0, 20) + "...";
    }
    $("#listDisplay").append("<tr id='"+id+"'><td><i class=\"icon-remove\" onclick=\"unMarkItem('"+id+"');\"></i></td><td>" 
			     +"<a href=\"#\" onclick=\"launchModal('"+id+"');\">"+shorthand+"</a>" 
			     +"</td></tr>");
    $.ajax({url: document.location.origin 
	    +'/items/'+id+'/'+listname+'/'+sessionkey,
	    type: 'put'});
}

function unMarkItem(id) {
    var listname = $.cookie('list');
    var sessionkey = $.cookie('session');
    if (listname == null || sessionkey == null) return;    

    $("#listDisplay tr[id='" + id + "']").remove();
    $("span[id='"+id+ "'] i").attr("onclick", "markItem('"+id+"')");
    $("span[id='"+id+ "'] i").removeClass("icon-ok");
    $("span[id='"+id+ "'] i").addClass("icon-tag");
    $.ajax({url: document.location.origin 
	    +'/items/'+id+'/'+listname+'/'+sessionkey,
	    type: 'delete'});
}

// Creates a modal page for reviewing stored data
function launchModal(id) {
    // Really need to call another page with better formatting.
    var response = $.ajax({url: 'http://ayr.cs.umass.edu:8080/details?id='+id,
			   async : false
			  });
    $('#detailsModal .modal-body')
	.empty()
	.append(response.responseText);
    $('#detailsModal').modal('show');
}

function selectList(listname) {
    $.cookie('list', listname);
    refreshWorkspaceUI();
}

function createList() {
    var sessionkey = $.cookie('session');
    if (sessionkey == null) return;
    var name = $('#listCreateModal #listCreateName').val();
    $('#listCreateModal').modal('hide');
    $.cookie('list', name);
    $.ajax({url: document.location.origin + '/lists/' + name + '/' + sessionkey,
	    type: 'put',
	    error: function(data) {
		noty({text: 'Could not create list:' + data, 
		      type: 'error',
		      layout: 'topRight',
		      timeout: 1000});
	    }
	   });
    refreshWorkspaceUI();
}

function deleteCurrentList() {
    var listname = $.cookie('list');
    var sessionkey = $.cookie('session');
    if (listname == null || sessionkey == null) return null;
    $.ajax({url: document.location.origin + '/lists/' + listname + '/' + sessionkey,
	    type: 'delete',
	    success: function() {
		$.removeCookie('list');	
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
    var listname = $.cookie('list');
    var sessionkey = $.cookie('session');
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
    var sessionkey = $.cookie('session');
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
    // Load the lists for this user
    var lists = getLists();
    $('#listBtnDropdown').empty();
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
    var listname = $.cookie('list');
    $('#listDisplay').empty();
    if (listname != null) {
	$('#listStatusText').text(listname);
	var items = getListItems();
	for (var i = 0; i < items.length; ++i) {
	    var newHTML = '<tr id="'+items[i]+'"><td>'
		+ '<i class="icon-remove" onclick="unMarkItem(\''+items[i]+'\');"></i></td>'
		+ '<td><a href="#" onclick="launchModal();">' + items[i] + '</a>'
		+ '</td></tr>';
	    $('#listDisplay').append(newHTML);
	}
	$('#listBtnDropdown').append('<li><a href="#" onclick="deleteCurrentList();">Delete current list</a></li>');
    } else {
	$('#listStatusText').text('No List Selected.');
    }
}