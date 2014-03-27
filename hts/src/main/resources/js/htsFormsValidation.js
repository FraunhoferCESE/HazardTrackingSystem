AJS.$(document).ready(function(){
	var $ = AJS.$
	var baseUrl = AJS.params.baseURL;

	$.validator.addMethod("uniqueHazard", function(value, element) {
		var response = false;
		//Check if hazard is begin edited, if so the hazard # can stay the same.
		if($("#oldNumber").length > 0) {
			var oldValue = $("#oldNumber").val();
			var newValue = value;
			if(oldValue === newValue) {
				response = true;
			}
		}
		//If the api is updated this url should be updated accordingly
		var actionUrl = baseUrl + "/rest/htsrest/1.0/report/hazardnumber/" + value;
		$.ajax({
			type:"GET",
			async: false,
			url: actionUrl,
			success: function(msg) {
				response = true;
			}
		});
		
		//Check to see if we have an error. If so change the color of the input text to be red.
		if(!response) {
			$(element).css("color", "#D04437");
		}
		else {
			$(element).css("color", "");
		}

		return response;
	}, "Hazard # is in use.");

	//Custom method to check if completion date is set to precede initation date, which should not be allowed
	$.validator.addMethod("preventIncorrectCompl", function(complDate, element) {
		var initDate = $("#hazardInitation").val();
		return ValidateDate(initDate, complDate);
	}, "Completion date cannot be set before initation date.");

	//Make sure the user can't input years lower than defined
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
				maxlength: 255,
				uniqueHazard: true
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

	    //Custom class so error messages are not styled with JIRA's css error style.
	    errorClass: "validationError",
	    errorElement: "span",
	    errorPlacement: function(error, element) {
	    	error.insertAfter(element);
	    },

	    submitHandler: function(form) {
	    	$(form).ajaxSubmit(function(data) {
	    		$("#hazardForm").append("<div>Changes were saved successfully</div>").addClass("successMsg");
	    		$("#hazardForm input, textarea, select").prop("disabled", true);
	    	});
	    }
	});

	//Helper functions
	function ValidateDate(initationVal, completionVal){
		//Both valid dates
		if(Date.parse(initationVal) && Date.parse(completionVal)) {
			var x = new Date(initationVal);
			var y = new Date(completionVal);
			return x <= y; 
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