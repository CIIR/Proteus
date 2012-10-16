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

function markItem(id) {
    $("span[id='"+id+ "'] i").removeClass("icon-tag");
    $("span[id='"+id+ "'] i").addClass("icon-ok");
    $("span[id='"+id+ "'] i").removeAttr("onclick");
    // Add the entry to the "tagged items bag"
    var shorthand = $("span[id='" + id + "'] u").text().trim();
    if (shorthand.length > 25) {
	shorthand = shorthand.substr(0, 20) + "...";
    }
    $("#taggedBag table").append("<tr id='"+id+"'><td><i class=\"icon-remove\" onclick=\"unMarkItem('"+id+"');\"></i></td><td>" 
				 +"<a href=\"#\" onclick=\"launchModal('"+id+"');\">"+shorthand+"</a>" 
				 +"<input type='hidden' name='chosenResult[]' value='"+id+"'></input></td></tr>");
    $.ajax('http://ayr.cs.umass.edu:9009/addItemToSession/'+id);
}

function unMarkItem(id) {
    $("#taggedBag table tr[id='" + id + "']").remove();
    $("span[id='"+id+ "'] i").attr("onclick", "markItem('"+id+"')");
    $("span[id='"+id+ "'] i").removeClass("icon-ok");
    $("span[id='"+id+ "'] i").addClass("icon-tag");
    $.ajax('http://ayr.cs.umass.edu:9009/removeItemFromSession/'+id);
}

// Creates a modal page for reviewing stored dat
function launchModal(id) {
    // Really need to call another page with better formatting.
    var response = $.ajax({
			      url: 'http://ayr.cs.umass.edu:9009/details?id='+id,
			      async : false
			  });
    $('#detailsModal').empty();
    $('#detailsModal').append(response.responseText);
    $('#detailsModal').modal('show');
}

// Preps the page for transformation.
function prepRelatedSearch() {
    // Should do some validation here:
    // Make sure at least one returnable type is checked,
    // Make sure at least one result is chosen
    return true;
    
    // THIS IS ALL OLD AND BAD - NEEDS TO BE REPLACED
    $("#resultContent .tab-pane.active input")
	.each(function() {
		  var newvalue = $(this).attr('name') + ',' + $(this).attr('value');
		  return jQuery('<input/>', {
				    type : 'hidden',
				    name : 'score',
				    value : newvalue
				}).appendTo("#relatedForm");
	      });

    // Append the target type
    jQuery('<input/>', { 
	      type: 'hidden', name: 'targetType', value: targetType
	   }).appendTo("#relatedForm");
    $('#relatedForm').submit();
}
