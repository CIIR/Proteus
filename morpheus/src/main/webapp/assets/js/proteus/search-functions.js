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

// Preps the page for transformation.
function relatedResults(targetType) {
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