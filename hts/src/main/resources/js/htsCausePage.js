function checkElementExpansion(element) {
	return element.is(":visible");
}

function dateLayout() {
	var lastUpdated = AJS.$(".lastUpdated");
	if(lastUpdated.length > 0) {
		lastUpdated.each(function () {
			AJS.$(this)[0].innerText = Date.parse(AJS.$(this)[0].innerText.substring(0,19)).toString("MMMM dd, yyyy, HH:mm");
		});
	} 

}

function getCookieValue(id) {
	return AJS.$.cookie("show-" + getTheHazardNumber() + id);
}

function createCookie(id, type) {
	AJS.$.cookie("show-" + getTheHazardNumber() + id, type, { expires: 1 });
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

function changeButtonText() {
	if(checkIfAllDivsAreOpen()) {
		AJS.$("#expandAll").html("Close all");
	}
	else {
		AJS.$("#expandAll").html("Expand all");	
	}
}

function checkIfAllFormsAreValid() {
	var valid = !AJS.$(".causeForms > div > span.validationError").is(":visible");
	if(valid) {
		location.reload();
	}
}

function checkIfAllDivsAreOpen() {
	return (AJS.$(".formContainer").length === AJS.$(".formContainer:visible").length);
}

function addExpandedClass(element) {
	AJS.$(element).removeClass().addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled")
}

function addCollapsedClass(element) {
	AJS.$(element).removeClass().addClass("aui-icon aui-icon-small aui-iconfont-add");
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

function getTheHazardNumber() {
	var hazardNumberAndTitle = AJS.$(".causeBody>h2").text();
	var index = hazardNumberAndTitle.indexOf("-");
	return hazardNumberAndTitle.substring(0, (index-1))  + "-";
}

function confirmation(causeID){
	var dialog = new AJS.Dialog({
		width: 500,
		height: 240,
		id: "deleteDialog",
	});
	
	dialog.show();
	dialog.addHeader("Confirm");
	dialog.addPanel("Panel 1", "<p class='panelBody'>Deleting will remove this Hazard cause from JIRA. Please enter why this Hazard cause is begin deleted.</p> <form class='aui' id='deleteReasonForm'><input class='text' type='text' id='deleteReason' name='deleteReason'></form>", "panel-body");
	dialog.get("panel:0").setPadding(0);
	
	dialog.addButton("Continue", function(dialog) {
		dialog.hide();
		var reason = AJS.$("#deleteReason").val();
		AJS.$.ajax({
			type: "DELETE",
			url: "causeform?key=" + causeID + "&reason=" + reason,
			success: function(data) {
				console.log("DELETED");
			},
			error: function(data) {
				console.log("error", arguments);
			}
		});
	});

	dialog.addLink("Cancel", function(dialog) {
		dialog.hide();
	}, "#");
}

function submitCauses() {
	AJS.$("#causeSaveAllChanges").live('click', function() {
		AJS.$("form.causeForms").each(function(){
			if(AJS.$(this).parent().parent().parent().find(".deleteCause").is(':checked')) {
				var self = AJS.$(this);
				confirmation(self.data("key"));
			}
			else {
				AJS.$(this).trigger("submit");
			}
		});
	});
}

AJS.$(document).ready(function(){
	dateLayout();
	openDivOnReload();
	submitCauses();
	AJS.$("#expandAll").live('click', function() {
		if(AJS.$(this).html() === "Close all") {
			AJS.$(".rowGroup .formContainer").hide();
			var spanElement = AJS.$(".trigger").children();
			addCollapsedClass(spanElement);
			closeAllDivs();
			changeButtonText();
		}
		else {
			AJS.$(".rowGroup .formContainer").show();
			var spanElement = AJS.$(".trigger").children();
			addExpandedClass(spanElement);	
			openAllDivs();
			changeButtonText();
		}
	});

	AJS.$('.trigger').click(function() {
		var spanElement = AJS.$(this).children();
		var spanClass = spanElement.attr("class");
		var formCont = AJS.$(this).parent().parent().find('.formContainer');
		if(!(checkElementExpansion(formCont))) {
			addExpandedClass(spanElement);
			formCont.show();
			createCookie(formCont.attr("id"), "expanded");
		}
		else {
			addCollapsedClass(spanElement);
			formCont.hide();
			createCookie(formCont.attr("id"), "collapsed");
		}
		
	});
});