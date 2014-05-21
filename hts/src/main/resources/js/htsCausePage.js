function checkElementExpansion(element) {
	return element.is(":visible");
}

function dateLayout() {
	var lastUpdated = AJS.$(".lastUpdated");
	if(lastUpdated.length > 0) {
		lastUpdated.each(function () {
			console.log(AJS.$(this)[0].innerText);
			AJS.$(this)[0].innerText = Date.parse(AJS.$(this)[0].innerText.substring(0,19)).toString("MMMM dd, yyyy, HH:mm");
		});
	} 

}

AJS.$(document).ready(function(){
	dateLayout();

	AJS.$("#expandAll").live('click', function() {
		console.log(AJS.$(this));
		console.log(AJS.$(this).html());
		if(AJS.$(this).html() === "Close all") {
			AJS.$(this).html("Expand all");
			AJS.$(".rowGroup .formContainer").hide();
			console.log(AJS.$(".toggle").children().removeClass().addClass("aui-icon aui-icon-small aui-iconfont-add"));	
		}
		else {
			AJS.$(this).html("Close all");
			AJS.$(".rowGroup .formContainer").show();
			AJS.$(".toggle").children().removeClass().addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled")
		}
	});

	AJS.$('.rowGroup .formContainer').hide();

	AJS.$('.toggle').click(function() {
		var spanElement = AJS.$(this).children();
		var spanClass = spanElement.attr("class");
		var formCont = AJS.$(this).parent().parent().find('.formContainer');
		if(!(checkElementExpansion(formCont))) {
			spanElement.removeClass(spanClass).addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
			formCont.show();
		}
		else {
			spanElement.removeClass(spanClass).addClass("aui-icon aui-icon-small aui-iconfont-add");
			formCont.hide();
		}
	});
});