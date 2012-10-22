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

