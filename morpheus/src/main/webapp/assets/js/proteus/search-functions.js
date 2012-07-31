$(function() {
      // This activates the first pane
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

// Sets up a callback for the result links to open a modal for showing
// index contentx
function showContent(pid) {
    var url = "/lookup-single?pid=" + pid;
    var modalpid = pid + "-modal";

    // AJAX MAGIC OR LOOKUP ON THE DOM

    $("#" + modalpid).modal('show');
}