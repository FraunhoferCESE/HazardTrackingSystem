	/**********************************************************
	*                                                         *
	*            Form - editing and saving related            *
	*                                                         *
	***********************************************************/

function getCookieValue(id) {
	return AJS.$.cookie("show-" + getTheHazardNumber() + "-" + id);
}

function createCookie(id, type) {
	AJS.$.cookie("show-" + getTheHazardNumber() + "-" + id, type, { expires: 1 });
}

function checkIfElementIsVisible(element) {
	return element.is(":visible");
}

function changeButtonText() {
	if(checkIfAllDivsAreOpen()) {
		AJS.$("#expandAll").html("Close all");
	}
	else {
		AJS.$("#expandAll").html("Expand all");
	}
}

function addExpandedClass(element) {
	AJS.$(element).removeClass().addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
}

function addCollapsedClass(element) {
	AJS.$(element).removeClass().addClass("aui-icon aui-icon-small aui-iconfont-add");
}

function openDivOnReload() {
	AJS.$(".formContainer").each(function() {
		var spanElement = AJS.$(this).parent().find(".trigger").children();
		if(getCookieValue(this.id) != "collapsed" && typeof(getCookieValue(this.id))!="undefined") {
			addExpandedClass(spanElement);
			AJS.$(this).show();
		}
		else {
			addCollapsedClass(spanElement);
			AJS.$(this).hide();
		}
	});
	changeButtonText();
}

function checkIfAllDivsAreOpen() {
	return (AJS.$(".formContainer").length === AJS.$(".formContainer:visible").length);
}


function openAllDivs() {
	AJS.$(".rowGroup .formContainer").each(function() {
		createCookie(this.id, "expanded");
	});
}

function closeAllDivs() {
	AJS.$(".rowGroup .formContainer").each(function() {
		createCookie(this.id, "collapsed");
	});
}

function dateLayout() {
	var lastUpdated = AJS.$(".lastUpdated");
	if(lastUpdated.length > 0) {
		lastUpdated.each(function () {
			AJS.$(this)[0].innerText = Date.parse(AJS.$(this)[0].innerText.substring(0,19)).toString("MMMM dd, yyyy, HH:mm");
		});
	}
}

function getTheHazardNumber() {
	return AJS.$("#HazardNumberForCause").text();
}

function deleteConfirmation(element, causeID){
	console.log("opened dialog");
	var dialog = new AJS.Dialog({
		width: 500,
		height: 260,
		id: "deleteDialog"+causeID,
	});
	var causeTitle = element.children().find(".causeTitle").text();
	var causeNumber = element.children().find(".trigger").text();

	dialog.show();
	dialog.addHeader("Confirm");
	dialog.addPanel("Panel 1", "<p class='panelBody'>The following cause will be removed from Hazard report " + getTheHazardNumber() + ": <ul><li>" + causeNumber + ": "+ causeTitle +"</li></ul></p><form class='aui' id='deleteReasonForm'><input class='text' type='text' id='deleteReason' name='deleteReason'></form><span class='deleteReasonText'>Field must not be empty</span>", "panel-body");
	dialog.get("panel:0").setPadding(0);

	dialog.addButton("Continue", function(dialog) {
		dialog.hide();
		var reason = AJS.$("#deleteDialog"+causeID).find("input").val();
		AJS.$.ajax({
			type: "DELETE",
			async: false,
			url: "causeform?key=" + causeID + "&reason=" + reason,
			success: function(data) {
				console.log("DELETED");
				element.remove();
			},
			error: function(data) {
				console.log("error", arguments);
			}
		});
	}, "popUpSubmits");

	AJS.$(".popUpSubmits").prop("disabled", true);

	AJS.$("#deleteDialog"+causeID).find("input").live("change keyup", function() {
		console.log(AJS.$("#deleteDialog"+causeID).find("input").val());
		if(AJS.$("#deleteDialog"+causeID).find("input").val().length > 0) {
			AJS.$(".popUpSubmits").prop("disabled", false);
		}
		else {
			AJS.$(".popUpSubmits").prop("disabled", true);
		}
	});

	dialog.addLink("Cancel", function(dialog) {
		dialog.hide();
		element.find(".deleteCause").attr('checked', false);
	}, "#");
}

function submitCauses() {
	AJS.$(".causeSaveAllChanges").live('click', function() {
		AJS.$("form.causeForms, form.transferredForms").each(function(){
			var rowGroup = AJS.$(this).parent().parent().parent();
			if(rowGroup.find(".deleteCause").is(':checked')) {
				var self = AJS.$(this);
				deleteConfirmation(rowGroup, self.data("key"));
			}
			else {
				if(AJS.$(this).isDirty()) {
					AJS.$(this).trigger("submit");
				}
			}
		});

		if(AJS.$("#addNewCauseForm").isDirty()) {
			AJS.$("#addNewCauseForm").trigger("submit");
		}

		var hazardID = AJS.$("#hazardList").val();
		if(hazardID.length) {
			AJS.$("#transferForm").trigger("submit");
		}

		AJS.bind("hide.dialog", function(e, data) {
			if(!checkIfElementIsVisible(AJS.$(".aui-dialog"))) {
				checkIfRefresh();
			}
		});
		checkIfRefresh();
	});
}

function checkIfRefresh() {
	if(!checkIfElementIsVisible(AJS.$(".validationError")) && !checkIfElementIsVisible(AJS.$(".aui-dialog"))) {
		location.reload();
	}
	else {
		AJS.$(".validationError").each(function (counter){
			console.log(AJS.$(this).parent().parent().find("#causeNumber").val())
		});

		JIRA.Messages.showWarningMsg("Not all changes have been saved. See invalid forms below.", {closeable: true});
	}
}

function foldable(element, containerClass) {
	var spanElement = AJS.$(element);
	var formCont = AJS.$("." + containerClass);
	if(!(checkIfElementIsVisible(formCont))) {
		addExpandedClass(spanElement);
		formCont.show()
	}
	else {
		addCollapsedClass(spanElement);
		formCont.hide();
	}
}

	/**********************************************************
	*                                                         *
	*               Cause transfer related.                   *
	*                                                         *
	***********************************************************/

function manipulateTextForOptionInCauses(theText) {
	if (theText.length >= 85){
		return theText.substring(0,82) + "...";
	}
	else {
		return theText;
	}
}

function transfer() {
	AJS.$("#transferForm").live("reset", function() {
		AJS.$(".container").hide();
		AJS.$("div.container").children().remove();
	});

	AJS.$("#hazardList").live("change reset", function() {
		var elements = AJS.$("div.container").children().remove();
		var value = AJS.$(this).val();
		var causeList;
		if(value.length) {
			AJS.$.ajax({
				type:"GET",
				async: false,
				url: AJS.params.baseURL + "/rest/htsrest/1.0/report/allcauses/" + value,
				success: function(data) {
					causeList = data;
				}
			});
			AJS.$(".container").show();
			var temp = "<label class='popupLabels' for='causeList'>Hazard Causes</label><select class='select long-field' name='causeList' id='causeList'>";
			if(causeList.length > 0) {
				temp += "<option value=''>-Link to all causes in selected Hazard report-</option>";
				AJS.$(causeList).each(function() {
					var causeNumberAndTitle = this.causeNumber + " - " + this.title;
					temp += "<option value=" + this.causeID + ">" + manipulateTextForOptionInCauses(causeNumberAndTitle) + "</option>";
				});
				AJS.$("div.container").append(temp);
			}
			else {
				AJS.$("div.container").append("<p>This Hazard report has no causes</p>");
			}
		}
		else {
			AJS.$(".container").hide();
		}

	}).trigger('change');
}

function manipulateCausesTitles(causesTitles) {
	if (causesTitles.length > 0) {
		causesTitles.each(function () {
			var shortend;
			if (AJS.$(this)[0].children.length === 0) {
				if (AJS.$(this)[0].innerText.length >= 128) {
					shortend = (AJS.$(this)[0].innerText).substring(0, 125) + "...";
					AJS.$(this)[0].innerText = shortend;
				}
			}
			else {
				var shortendArr = (AJS.$(this)[0].innerText).split(" - ");
				if (shortendArr.length === 2) {
					if (shortendArr[1].length >= 128) {
						shortend = shortendArr[1].substring(0, 125) + "...";
						AJS.$(this)[0].children[0].innerText = shortendArr[0] + " - " + shortend;
					}
				}
				else {
					if (AJS.$(this)[0].innerText.length >= 128) {
						shortend = (AJS.$(this)[0].innerText).substring(0, 125) + "...";
						AJS.$(this)[0].children[0].innerText = shortend;
					}
				}
			}
		});
	}
}

function manipulateTextForHazardSelectionInCauses(theHazardList) {
	if (theHazardList[0].children.length > 1) {
		(theHazardList.children()).each(function (index) {
			if ((AJS.$(this)[0].innerText).length >= 85) {
				AJS.$(this)[0].text = (AJS.$(this)[0].innerText).substring(0,82) + "...";
			}
		});
	}
}

AJS.$(document).ready(function(){
	var causesTitles = AJS.$(".CausesTableTitleText");
	manipulateCausesTitles(causesTitles);
	getTheHazardNumber();
	dateLayout();
	openDivOnReload();
	submitCauses();
	transfer();
	AJS.$(".newFormContainer").hide();
	AJS.$(".transferFormContainer").hide();

	AJS.$("#expandAll").live("click", function() {
		if(AJS.$(this).html() === "Close all") {
			AJS.$(".rowGroup .formContainer").hide();
			var spanElement = AJS.$(".trigger").children();
			addCollapsedClass(spanElement);
			closeAllDivs();
		}
		else {
			AJS.$(".rowGroup .formContainer").show();
			var spanElement = AJS.$(".trigger").children();
			addExpandedClass(spanElement);
			openAllDivs();
		}
		changeButtonText();
	});

	AJS.$(".trigger").live("click", function() {
		var spanElement = AJS.$(this).children();
		var spanClass = spanElement.attr("class");
		var formCont = AJS.$(this).parent().parent().find(".formContainer");
		if(!(checkIfElementIsVisible(formCont))) {
			addExpandedClass(spanElement);
			formCont.show();
			createCookie(formCont.attr("id"), "expanded");
		}
		else {
			addCollapsedClass(spanElement);
			formCont.hide();
			createCookie(formCont.attr("id"), "collapsed");
		}
		changeButtonText();
	});

	AJS.$("#newCauseFormTrigger").live("click", function() {
		foldable(this, "newFormContainer");
	});

	AJS.$("#transferFormTrigger").live("click", function() {
		var causesHazardList = AJS.$("#hazardList");
		manipulateTextForHazardSelectionInCauses(causesHazardList);
		foldable(this, "transferFormContainer");
	});

	AJS.$("#causeAddControl").live("click", function() {
		var causeIDAndHazardIDArr = AJS.$(this).data("key").split("-");
		var causeID = causeIDAndHazardIDArr[0];
		var hazardID = causeIDAndHazardIDArr[1];
		window.location.href = AJS.params.baseURL + "/plugins/servlet/controlform?edit=y&key=" + hazardID + "&selcau=" + causeID;
	});

	var whichForm;
	if (AJS.$.url().data.seg.path.length === 4) {
		whichForm = AJS.$.url().data.seg.path[3];
	}
	else {
		whichForm = AJS.$.url().data.seg.path[2];
	}

	if (whichForm === "causeform") {
		var idOfCauseOpen = AJS.$.url().param("id");
		if(idOfCauseOpen) {
			closeAllDivs();
			createCookie(idOfCauseOpen, "expanded");
			openDivOnReload();
		}
		if (AJS.$.url().param("trans") === "y") {
			AJS.$('html, body').animate({
				scrollTop: AJS.$("#CausesTableEntry" + idOfCauseOpen).offset().top
			}, 50);
		}
	}

});