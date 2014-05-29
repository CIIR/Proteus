function handleEnter()
{
	var button;
	var result= $("#search-buttons input");



	for(var i =0; i < result.length; i++){
		if (result[i].value == "Metadata"){

			result[i].click();
			break;
		}
	}


}
