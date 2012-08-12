$('#wordForm').submit(
    function() {
	if ($('#w').val().trim() == '') {
	    $('#wordForm + #word_empty'),remove();
	    $('#wordForm').after('div id="word_empty" class="alert alert-error span4"><button class="close" data-dismiss="alert" type="button" href="#">x</button>Enter a word to search, sucka!</div>');
	    return false;
	} else {
	    return true;
	}
    });