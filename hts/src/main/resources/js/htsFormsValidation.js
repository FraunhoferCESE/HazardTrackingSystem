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

	//Custom method to check if completion date is set to precede initation date, which should not be allowed
	$.validator.addMethod("preventIncorrectCompl", function(complDate, element) {
		var initDate = $("#hazardInitation").val();
		return ValidateDate(initDate, complDate);
	}, "Completion date cannot be set before initation date.");

	$.validator.addMethod("mindate", function(val, element, minDate) {
		if(this.optional(element)) {
			return true;
		}
		var curDate = new Date($(element).val());
		return minDate <= curDate;
	}, "Dates cannot precede the year 1940");

	$("#hazardForm").validate({
		rules: {
			hazardNumber: { 
				required: true,
				maxlength: 255
			},
	    	hazardTitle: { 
	    		required: true,
	    		maxlength: 512
	    	},
	    	hazardPayload: {
	    		maxlength: 255
	    	},
	    	hazardSubsystem: {
	    		maxlength: 255
	    	},
	    	hazardInitation: {
	    		date: true,
	    		mindate: new Date(40, 0, 1)
	    	},
	    	hazardCompletion: {
	    		date: true,
	    		preventIncorrectCompl: true,
	    		mindate: new Date(40, 0, 1)
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
	    },

	    errorClass: "validationError",
	    errorElement: "span",
	    errorPlacement: function(error, element) {
	    	error.insertAfter(element);
	    }
	});

	//Helper functions
	function ValidateDate(initationVal, completionVal){
		//Both valid dates
		if(Date.parse(initationVal) && Date.parse(completionVal)) {
			var x = new Date(initationVal);
			var y = new Date(completionVal);
			return x < y; 
		}
		//initation is valid
		else if(Date.parse(initationVal) && !(Date.parse(completionVal))) {
			return true;
		}
		//initation is not valid but completion is then the form is invalid.
		else if(!(Date.parse(initationVal)) && Date.parse(completionVal)) {
			return false;
		} 
		else {
			return true;
		}
	}

});