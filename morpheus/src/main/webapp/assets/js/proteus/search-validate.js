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
	}
});
