$(function() {
      // This activates the first result pane
      $('#resultTabs a:first').tab('show');
      
      // Now select the type buttons
      var resultTypes = $('#resultTabs a');
      if (resultTypes.size() == 5) {
	  // Previous search did 'all'
	  $('#typeselection button[name="all"]').addClass('active');
      } else {
	  // Do 'em one by one.
	  var pattern = /#([a-z]+)tab/;
	  resultTypes.each(function() {
			       var buttonName = pattern.exec($(this).attr('href'))[1];
			       $('#typeselection button[name="'+buttonName+'"]').addClass('active');
			   });
      }
  });

function markItem(id) {
    $("span#"+id+ " i").removeClass("icon-tag");
    $("span#"+id+ " i").addClass("icon-ok");
    $("span#"+id+ " i").removeAttr("onclick");
    // Add the entry to the "tagged items bag"
    var shorthand = $("span#" + id + " strong").text().trim().substr(0, 20) + "...";
    $("#taggedBag table").append("<tr id='"+id+"'><td><i class=\"icon-remove\" onclick=\"unMarkItem('"+id+"');\"></i></td><td>" + shorthand + "</td></tr>");
}

function unMarkItem(id) {
    $("#taggedBag table tr#" + id).remove();
    $("span#"+id+ " i").attr("onclick", "markItem('"+id+"')");
    $("span#"+id+ " i").removeClass("icon-ok");
    $("span#"+id+ " i").addClass("icon-tag");
}

// Preps the page for transformation.
function relatedResults(targetType) {
    alert("Submitting!");
    // copy the active result elements into the form
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
