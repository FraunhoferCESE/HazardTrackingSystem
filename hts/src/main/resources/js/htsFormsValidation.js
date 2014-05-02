AJS.$(document).ready(function(){
	var $ = AJS.$
	var baseUrl = AJS.params.baseURL;

    editForm();

    $("#hazardSubsystem").multiselect2side({
    	selectedPosition: 'right',
		moveOptions: false,
		labelsx: '',
		labeldx: 'Selected',
		'search': 'Search: ',
		autoSort: true,
		autoSortAvailable: true
    });

    $("#hazardGroup").multiselect2side({
    	selectedPosition: 'right',
		moveOptions: false,
		labelsx: '',
		labeldx: 'Selected',
		'search': 'Search: ',
		autoSort: true,
		autoSortAvailable: true
    });
	
	$("#hazardPhase").multiselect2side({
    	selectedPosition: 'right',
		moveOptions: false,
		labelsx: '',
		labeldx: 'Selected',
		'search': 'Search: ',
		autoSort: true,
		autoSortAvailable: true
    });

	/**********************************************************
	*                                                         *
	*               Form validation below.                    *
	*                                                         *
	***********************************************************/

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

	$.validator.addMethod("uniquePayload", function(value, element) {
		var response = false;
		var actionUrl = baseUrl + "/rest/htsrest/1.0/report/hazardlist/" + value;
		$.ajax({
			type:"GET",
			async: false,
			url: actionUrl,
			success: function(msg) {
				response = true;
			}
		});
		if(!response) {
			$(element).css("color", "#D04437");
		}
		else {
			$(element).css("color", "");
		}

		return response;
	}, "Mission/Payload already in use.");

	//Custom method to check if completion date is set to precede initation date, which should not be allowed
	$.validator.addMethod("preventIncorrectCompl", function(complDate, element) {
		var initDate = $("#hazardInitation").val();
		return validateDate(initDate, complDate);
	}, "Completion date cannot be set before initation date.");

	//Make sure the user can't input years lower than defined.
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
	    		//First number is the year, then month and day. If the date interval is to be changed, this is the place to do it.
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
	    	$(error).css({"margin-top":'5px'});
	    },

	    submitHandler: function(form) {
	    	$(form).ajaxSubmit({
	    		success: function(data) {
	    			//To remove jiras dirty warning so navigating from the form after successful post is possible
	    			$("#hazardForm").removeDirtyWarning();
	    			successfulSave(form);
	    			//Retrieving the values from the json response. If it is not successful clean form is rendered(happens when user hits save and create another)
	    			var data = $.parseJSON(data);
	    			if(data.redirect) {
	    				window.location.replace(data.redirect);
	    			}
	    			else {
	    				var hazardNumber = data.hazardNumber;
		    			var hazardID = data.hazardID;
	    				addOrUpdateHazardNum(form, hazardNumber, hazardID);
	    			}	
	    		},
	    		error: function(error) {
	    			console.log(error);
	    		}
	    	});
	    }
	});

	$("#payloadForm").validate({
		rules: {
			hazardPayloadAdd: { 
				maxlength: 255,
				uniquePayload: true
			}
	    },

	    //Custom class so error messages are not styled with JIRA's css error style.
	    errorClass: "validationError",
	    errorElement: "span",
	    errorPlacement: function(error, element) {
	    	error.insertAfter(element);
	    	$(error).css({"height":0});
	    },

	    submitHandler: function(form) {
	    	$(form).ajaxSubmit({
	    		success: function(data) {
	    			//To remove jiras dirty warning so navigating from the form after successful post is possible
	    			$("#payloadForm").removeDirtyWarning();
	    			JIRA.Messages.showSuccessMsg($("#hazardPayloadAdd").val() +" was created successfully", {closeable: true});
	    			$("#payloadList").load(document.URL + " #payloadList");
	    			form.reset();
	    		},
	    		error: function(error) {
	    			console.log(error);
	    		}
	    	});
	    }
	});

	/**********************************************************
	*                                                         *
	*               Helper functions below.                   *
	*                                                         *
	***********************************************************/
	function validateDate(initationVal, completionVal) {
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

	//After a successful save message needs to be displayed to the user.
	function successfulSave(form) {
		//Input the successful save frame
		var success = $('<div class="aui-message success successMsg"><p><span class="aui-icon icon-success"></span>Changes were saved successfully</p></div>');
	    if($(".successMsg").length > 0) {
	    	$(".successMsg").hide();
	    	setTimeout(function() {
	    		$(".successMsg").show();
	    	}, 100);
	    }
	    else {
	    	$(form).after(success);
	    }
	}

	//Need to store the hazard number specified in the field to see if has been changed. If not saving is okay.
	function addOrUpdateHazardNum(form, hazardNum, id) {
		if($("#oldNumber").length > 0) {
			$("#oldNumber").val(hazardNum);
		}
		else {
			$(form).append('<input type="hidden" id="oldNumber" name="hazardNumberBeforeEdit" value/>');
			//Takes care of adding the two fields after the first post, so saving again is possible through the edit part.
			$("#oldNumber").val(hazardNum);
		}
		if(!($("#edit").length > 0 && $("#key").length > 0)) {
			addNecessaryInfo(id);
		}
	}

	//Hidden fields to store info about if the form is begin editied and if so, then it also stores the hazard ID.
	function addNecessaryInfo(id) {
        if(AJS.$("#oldNumber").length > 0) {
            var form = document.forms["hazardForm"];
            addHiddenField(form, "edit", "edit", "y");
            addHiddenField(form, "key", "key", id);
        }
    }

    //Creating a new hidden field in a form.
    function addHiddenField(form, key, id, value) {
        var input = document.createElement("input");
        input.type = "hidden";
        input.name = key;
        input.id = id;
        input.value = value;
        form.appendChild(input);
    }

	//Add fields to the edit form is opened for the first time
	function editForm() {
		var id = $.url().param("key");
		if(typeof id !== 'undefined') {
			addNecessaryInfo(id);
		}
	}
});