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
	AJS.$(element).removeClass().addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled")
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
	var hazardNumberAndTitle = AJS.$(".causeBody>h2").text();
	var index = hazardNumberAndTitle.indexOf("-");
	return hazardNumberAndTitle.substring(0, (index-1));
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
		AJS.$("form.causeForms").each(function(){
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
		AJS.bind("hide.dialog", function(e, data) {
			if(!checkIfElementIsVisible(AJS.$(".aui-dialog"))) {
				checkIfRefresh();
			}
		});
	});
}

function checkIfRefresh() {
	if(!checkIfElementIsVisible(AJS.$(".validationError")) && !checkIfElementIsVisible(AJS.$(".aui-dialog"))) {
		location.reload();
	}
	else {
		AJS.$(".validationError").each(function (counter){
			console.log("loop called " + counter);
			console.log(AJS.$(this).parent().parent().find("#causeNumber").val())
		});

		JIRA.Messages.showWarningMsg("Not all changes have been saved. See invalid forms below.", {closeable: true});
	}
}

	/**********************************************************
	*                                                         *
	*               Cause transfer related.                   *
	*                                                         *
	***********************************************************/

function openTransferPopup() {
	AJS.$(".transfers").live('click', function() {
		var form = AJS.$(this).parent().parent().parent();
		var causeTitle = form[0].causeTitle;
		var causeOwner = form[0].causeOwner;
		var causeEffect = form[0].causeEffects;
		var description = form[0].causeDescription;

		var dialog = new AJS.Dialog({
			width: 600,
			height: 240,
			id: "deleteDialog",
		});
		var hazardList;
		AJS.$.ajax({
			type:"GET",
			async: false,
			url: AJS.params.baseURL + "/rest/htsrest/1.0/report/allhazards/",
			success: function(data) {
				hazardList = data;
			}
		});

		var html = "<form class='aui panelBody'><label class='popupLabels' for='hazardList'>Hazard Reports</label><select size='1' class='select' name='hazardList' id='hazardList'><option value=''>-Select Hazard Report-</option>"
		AJS.$(hazardList).each(function() {
			html += "<option value=" + this.hazardID +">" + this.hazardNumber + " - " + this.title + "</option>";
		});
		html += "</select><button type='button' class='button popupLink' id='linkHazard'>Link Hazard</button><div class='container'></div>";
		AJS.$("#hazardList").live("change", function() {
			var elements = AJS.$("div.container").children().remove();
			AJS.$(".popUpSubmits").css("visibility", "hidden");
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
				var temp = "<label class='popupLabels' for='hazardList'>Hazard Causes</label><select class='select' name='causeList' id='causeList'>"
				if(causeList.length > 0) {
					AJS.$(causeList).each(function() {
						temp += "<option value=" + this.causeID + ">" + this.causeNumber + " - " + this.title + "</option>"
					});
					temp += "</select><button type='button' class='button popupLink' id='linkCause'>Link Cause</button>";
					AJS.$("div.container").append(temp);
					AJS.$(".popUpSubmits").css("visibility", "visible");
				}
				else {
					AJS.$("div.container").append("<p>This Hazard report has no causes</p>");
				}
			}

		}).trigger('change');

		/*dialog.addButton("Continue", function(dialog) {
			dialog.hide();
			var currentCauseID = AJS.$("#causeList option:selected").val();
			AJS.$.ajax({
				type:"GET",
				async: false,
				url: AJS.params.baseURL + "/rest/htsrest/1.0/report/transfercause/" + currentCauseID,
				success: function(data) {
					AJS.$(causeTitle).val(data.title).prop("readonly", true);
					AJS.$(causeOwner).val(data.owner).prop("readonly", true);
					AJS.$(description).val(data.description).prop("readonly", true);
					AJS.$(causeEffect).val(data.effects).prop("readonly", true);
					JIRA.Messages.showSuccessMsg(AJS.$("#causeList option:selected").text() +" was successfully transferred", {closeable: true});
				},
				error: function(e) {
					console.log(e);
				}
			});
		}, "popUpSubmits");

		AJS.$(".popUpSubmits").css("visibility", "hidden");*/
		dialog.addLink("Cancel", function(dialog) {
			dialog.hide();
		}, "#");

		dialog.show();
		dialog.addHeader("Transfer Hazard Causes");
		dialog.addPanel("Panel 1", html, "panel-body");
		dialog.get("panel:0").setPadding(0);
	});
}

AJS.$(document).ready(function(){
	dateLayout();
	openDivOnReload();
	submitCauses();
	openTransferPopup();
	AJS.$(".newFormContainer").hide();

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

	AJS.$(".newCauseFormTrigger").live("click", function() {
		var spanElement = AJS.$(this).children();
		var formCont = AJS.$(this).parent().find(".newFormContainer");
		if(!(checkIfElementIsVisible(formCont))) {
			addExpandedClass(spanElement);
			formCont.show()
		}
		else {
			addCollapsedClass(spanElement);
			formCont.hide();
		}
	});
});