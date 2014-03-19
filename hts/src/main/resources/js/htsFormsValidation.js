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
	}, "Dates cannot precede the year 1940.");

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
	    		maxlength: 512
	    	},
	    	hazardSubsystem: {
	    		maxlength: 512
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
		else if(!(Date.parse(initationVal)) && Date.parse(completionVal)) {
			return false;
		} 
		else {
			return true;
		}
	}

});