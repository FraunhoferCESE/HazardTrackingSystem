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


AJS.$(document).ready(function(){
	dateLayout();
	openDivOnReload();
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

	AJS.$("#causeSaveAllChanges").live('click', function() {
		AJS.$("form.causeForms").each(function(){
			if(AJS.$(this).parent().parent().parent().find(".deleteCause").is(':checked')) {
				console.log("DELETE ME");
				var formContainer = AJS.$(this).parent().parent().parent().find('.formContainer');
				var causeID = formContainer.attr("id");
				console.log(causeID);
			}
			else {
				AJS.$(this).trigger("submit");
			}
		});
	});
});