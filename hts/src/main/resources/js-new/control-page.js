console.log("=== control-page.js ===");

var EXISTING_CONTROLS_SERIALIZED = null;

function initializeControlPage() {
	if (INIT.CONTROLS === true) {
		INIT.CONTROLS = false;
		initControlPageClickEvents();
	}
	initControlPageMultiSelectes();
	initControlPageDateModification();
}

function initControlPageDateModification() {
	AJS.$(".HTSDate").each(function() {
		var dateStrUnformatted = AJS.$(this).text();
		var dateStrFormatted = formatDate(dateStrUnformatted);
		AJS.$(this).text(dateStrFormatted);
	});
}

function initControlPageClickEvents() {
	// Add new control click event
	AJS.$("#ControlPageAddNewControl").live("click", function() {
		// Calling function in shared-cookies.js file
		toggleOpenCloseIcon(AJS.$(this), AJS.$("#ControlPageNewContainer"));
	});

	// Add new transfer click event
	AJS.$("#ControlPageAddTransfer").live("click", function() {
		// Calling function in shared-cookies.js file
		toggleOpenCloseIcon(AJS.$(this), AJS.$("#ControlPageTransferContainer"));
	});



	// Save new control
	AJS.$(".ControlPageSaveAllChanges").live("click", function() {
		postFormToControlServlet(AJS.$("#ControlPageFormAddNew"));

		// var formElement = AJS.$(".ControlPageFormExisting")[0];
		// console.log(formElement);
		// postFormToControlServlet(formElement);
	});

	// Transfer click event (gets causes belonging to selected hazard)
	AJS.$("#controlHazardList").live("change reset", function() {
		var causeContainer = AJS.$("#ControlPageCauseTransferContainer");
		AJS.$(causeContainer).children().remove();
		var hazardID = AJS.$(this).val();
		if (hazardID !== "") {
			var causes = getAllCausesWithinHazard(hazardID);
			var html = "<label class='popupLabels' for='controlCauseList'>Hazard Causes</label><select class='select long-field' name='controlCauseList' id='controlCauseList'>";
			if (causes.length !== 0) {
				html += "<option value=''>-Link to all Causes in selected Hazard Report-</option>";
				for (var i = 0; i < causes.length; i++) {
					var optionText;
					if (causes[i].transfer === true) {
						optionText = causes[i].causeNumber + "-T - " + causes[i].title;
					} else {
						optionText = causes[i].causeNumber + " - " + causes[i].title;
					}
					html += "<option value=" + causes[i].causeID + ">" + manipulateTextLength(optionText, 85) + "</option>";
				}
				html += "</select>";
				AJS.$(causeContainer).append(html);
			} else {
				AJS.$(causeContainer).append("<label class='popupLabels' for='controlCauseList'>Hazard Causes</label><div class='TransferNoProperties'>-Link to all Causes in selected Hazard Report- (Selected HR currently has no Causes)</div>");

			}
			AJS.$(causeContainer).show();
		} else {
			AJS.$(causeContainer).hide();
		}
	});

	// Transfer click event (gets controls belonging to selected cause)
	AJS.$("#controlCauseList").live("change reset", function() {
		var causeID = AJS.$(this).val();
		if (causeID !== "") {
			var controls = getAllControlsWithinCause(causeID);
			var html = "<label class='popupLabels' for='controlControlList'>Hazard Controls</label><select class='select long-field' name='controlControlList' id='controlControlList'>";
			if (controls.length !== 0) {
				html += "<option value=''>-Link to all Controls in selected Hazard Report-</option>";
				for (var i = 0; i < controls.length; i++) {

				}
			}
		}
	});

	// Open/close on existing cause
	AJS.$(".ControlTableToggle").live("click", function() {
		var elementID = AJS.$(this).parent().parent().attr("id");
		var controlID = elementID.split("ControlTableEntryID")[1];
		var displayElement = AJS.$("#ControlTableEntryContentID" + controlID);
		var operation = toggleOpenCloseIcon(AJS.$(this), displayElement);
		//var existingControlCount = AJS.$(".ControlTableToggle").length;
		// Calling function in shared-cookies.js file
		//modifyHTSCookieOpenCauses(operation, controlID, existingControlCount);
	});
}

function postFormToControlServlet(formElement) {
	AJS.$(formElement).ajaxSubmit({
		async: false,
		success: function(data) {
			console.log("SUCCESS");
			console.log(data);
		},
		error: function(error) {
			// TODO:
			// Getting an object here which contains the error message
			// Display similar message as in success, but of the error kind
			console.log("ERROR");
		}
	});
}

function initControlPageMultiSelectes() {
	AJS.$(".controlCauses").multiselect2side({
		selectedPosition: 'right',
		moveOptions: false,
		labelsx: '',
		labeldx: '',
		'search': 'Search: ',
		autoSort: true,
		autoSortAvailable: true
	});

	// Adjust the CSS
	var multiSelectDivs = AJS.$(".ms2side__div");
	AJS.$(multiSelectDivs).each(function() {
		AJS.$(this).children(":nth-child(1)").children(":nth-child(1)").css("padding-bottom", "3px");
		AJS.$(this).children(":nth-child(2)").css("padding-top", "12px");
		AJS.$(this).children(":nth-child(3)").css("padding-top", "28px");
	});
}