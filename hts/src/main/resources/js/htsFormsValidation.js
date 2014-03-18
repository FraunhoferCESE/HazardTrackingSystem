AJS.$(document).ready(function(){
	var $ = AJS.$
	var url = AJS.params.baseURL + "/plugins/servlet/hazardform";
	console.log(url);
	//TODO FIX ajax request
	var response;
	$.validator.addMethod("uniqueHazard", function(value, element) {
		$.ajax({

		});
		return response;
	}, "Hazard # must be unique");

	//TODO minor fixes
	$.validator.addMethod("preventIncorrectCompl", function(value, element) {
		console.log(value);
		var initDate = $("#hazardInitation").val();
		console.log($("#hazardInitation").val());
		return ValidateDate(initDate, value);
	}, "Completion date cannot precede initation date.");

	$("#hazardForm").validate({
		rules: {
			hazardNumber: { 
				required: true,
			},
	    	hazardTitle: { 
	    		required: true,
	    		maxlength: 512
	    	},
	    	hazardCompletion: {
	    		preventIncorrectCompl: true
	    	}
	    },

	    messages: {
	    	hazardNumber: {
	    		required: "Hazard # is required."
	    	},
	    	hazardTitle: {
	    		required: "Title is required.",
	    		maxlength: "Title should not excede 512 characters."
	    	},
	    }
	});

	function ValidateDate(initationVal, completionVal){
		var x = new Date(initationVal);
		var y = new Date(completionVal);
		return x < y;
	}
});