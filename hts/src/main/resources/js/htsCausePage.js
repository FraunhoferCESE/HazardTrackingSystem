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

function checkElementExpansion(element) {
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

function confirmation(element, causeID){
	var dialog = new AJS.Dialog({
		width: 500,
		height: 240,
		id: "deleteDialog",
	});
	var causeTitle = element.children().find(".causeTitle").text();
	var causeNumber = element.children().find(".trigger").text();
	
	dialog.show();
	dialog.addHeader("Confirm");
	dialog.addPanel("Panel 1", "<p class='panelBody'>The following cause will be removed from Hazard report " + getTheHazardNumber() + ": <ul><li>" + causeNumber + ": "+ causeTitle +"</li></ul></p><form class='aui' id='deleteReasonForm'><input class='text' type='text' id='deleteReason' name='deleteReason'></form>", "panel-body");
	dialog.get("panel:0").setPadding(0);
	
	dialog.addButton("Continue", function(dialog) {
		dialog.hide();
		var reason = AJS.$("#deleteReason").val();
		AJS.$.ajax({
			type: "DELETE",
			url: "causeform?key=" + causeID + "&reason=" + reason,
			success: function(data) {
				console.log("DELETED");
				element.remove();
			},
			error: function(data) {
				console.log("error", arguments);
			}
		});
	});

	dialog.addLink("Cancel", function(dialog) {
		dialog.hide();
		element.find(".deleteCause").attr('checked', false);
	}, "#");
}

function submitCauses() {
	AJS.$("#causeSaveAllChanges").live('click', function() {
		AJS.$("form.causeForms").each(function(){
			var rowGroup = AJS.$(this).parent().parent().parent();
			if(rowGroup.find(".deleteCause").is(':checked')) {
				var self = AJS.$(this);
				confirmation(rowGroup, self.data("key"));
			}
			else {
				AJS.$(this).trigger("submit");
			}
		});
	});
}

	/**********************************************************
	*                                                         *
	*               Cause transfer related.                   *
	*                                                         *
	***********************************************************/

function openPopUp() {
	AJS.$(".transfers").live('click', function() {
		var dialog = new AJS.Dialog({
			width: 500,
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

		var html = "<form class='aui panel-body'><label class='popupLabels' for='hazardList'>Hazard Reports</label><select size='1' class='select' name='hazardList' id='hazardList'><option value=''>-Select Hazard Report-</option>"
		AJS.$(hazardList).each(function() {
			html += "<option value=" + this.hazardID +">" + this.hazardNumber + " - " + this.title + "</option>";
		});
		html += "</select><div class='container'></div>";
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
				AJS.$(causeList).each(function() {
					temp += "<option value=" + this.causeID + ">" + this.causeNumber + " - " + this.title + "</option>"
				});
				temp += "</select>";
				AJS.$("div.container").append(temp);
				AJS.$(".popUpSubmits").css("visibility", "visible");
			}

		}).trigger('change');

		dialog.addButton("Continue", function(dialog) {
			dialog.hide();
			var currentCauseID = AJS.$("#causeList option:selected").val();
			AJS.$.ajax({
				type:"GET",
				async: false,
				url: AJS.params.baseURL + "/rest/htsrest/1.0/report/transfercause/" + currentCauseID,
				success: function(data) {
					console.log(data);
				}
			});
		}, "popUpSubmits");

		AJS.$(".popUpSubmits").css("visibility", "hidden");
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
	openPopUp();

	AJS.$("#expandAll").live('click', function() {
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
		changeButtonText();
	});
});