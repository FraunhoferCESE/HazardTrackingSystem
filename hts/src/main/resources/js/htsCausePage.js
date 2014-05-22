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

function openDivOnReload() {
	AJS.$(".formContainer").each(function() {
		var spanElement = AJS.$(this).parent().find(".trigger").children();
		if(AJS.$.cookie("show-" + this.id) != "collapsed") {
			addExpandedClass(spanElement);
			AJS.$(this).show();
		}
		else {
			addCollapsedClass(spanElement);
			AJS.$(this).hide();
		}
	});
}

function addExpandedClass(element) {
	AJS.$(element).removeClass().addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled")
}

function addCollapsedClass(element) {
	AJS.$(element).removeClass().addClass("aui-icon aui-icon-small aui-iconfont-add");
}

function openAllDivs() {
	AJS.$(".rowGroup .formContainer").each(function() {
		AJS.$.cookie("show-" + this.id, "expanded"); 
	});
}

function closeAllDivs() {
	AJS.$(".rowGroup .formContainer").each(function() {
		AJS.$.cookie("show-" + this.id, "collapsed"); 
	});
}


AJS.$(document).ready(function(){
	dateLayout();
	openDivOnReload();

	AJS.$("#expandAll").live('click', function() {
		if(AJS.$(this).html() === "Close all") {
			AJS.$(this).html("Expand all");
			AJS.$(".rowGroup .formContainer").hide();
			var spanElement = AJS.$(".trigger").children();
			addCollapsedClass(spanElement);
			closeAllDivs();
		}
		else {
			AJS.$(this).html("Close all");
			AJS.$(".rowGroup .formContainer").show();
			var spanElement = AJS.$(".trigger").children();
			addExpandedClass(spanElement);	
			openAllDivs();
		}
	});

	AJS.$('.trigger').click(function() {
		var spanElement = AJS.$(this).children();
		var spanClass = spanElement.attr("class");
		var formCont = AJS.$(this).parent().parent().find('.formContainer');
		if(!(checkElementExpansion(formCont))) {
			addExpandedClass(spanElement);
			formCont.show();
			AJS.$.cookie("show-" + formCont.attr("id"), "expanded");
		}
		else {
			addCollapsedClass(spanElement);
			formCont.hide();
			AJS.$.cookie("show-" + formCont.attr("id"), "collapsed");
		}
	});
});