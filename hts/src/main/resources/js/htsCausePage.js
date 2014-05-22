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

AJS.$(document).ready(function(){
	dateLayout();

	AJS.$(".formContainer").each(function() {
		console.log(this.id);
		console.log(AJS.$.cookie("show-" + this.id));
		if(AJS.$.cookie("show-" + this.id) != "collapsed") {
			console.log(AJS.$(this));
			AJS.$(this).show();
		}
		else {
			AJS.$(this).hide();
		}
	});

	AJS.$("#expandAll").live('click', function() {
		if(AJS.$(this).html() === "Close all") {
			AJS.$(this).html("Expand all");
			AJS.$(".rowGroup .formContainer").hide();
			AJS.$(".trigger").children().removeClass().addClass("aui-icon aui-icon-small aui-iconfont-add");	
		}
		else {
			AJS.$(this).html("Close all");
			AJS.$(".rowGroup .formContainer").show();
			AJS.$(".trigger").children().removeClass().addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled")
		}
	});

	AJS.$('.trigger').click(function() {
		var spanElement = AJS.$(this).children();
		var spanClass = spanElement.attr("class");
		var formCont = AJS.$(this).parent().parent().find('.formContainer');
		if(!(checkElementExpansion(formCont))) {
			spanElement.removeClass(spanClass).addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
			formCont.show();
			AJS.$.cookie("show-" + formCont.attr("id"), "expanded");
		}
		else {
			spanElement.removeClass(spanClass).addClass("aui-icon aui-icon-small aui-iconfont-add");
			formCont.hide();
			console.log(AJS.$.cookie("show-" + formCont.attr("id"), "collapsed"));
		}
	});
});