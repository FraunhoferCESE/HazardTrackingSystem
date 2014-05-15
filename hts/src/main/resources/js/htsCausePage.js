function checkElementExpansion(element) {
	return element.is(":visible");
}

AJS.$(document).ready(function(){
	AJS.$("#expandAll").live('click', function() {
		console.log(AJS.$(this));
		AJS.$(".rowGroup .formContainer").slideToggle();
		console.log(AJS.$(".formContainer").is(":visible"));
		console.log(AJS.$(this).html());
		if(AJS.$(this).html() === "Close all") {
			AJS.$(this).html("Expand all");	
		}
		else {
			AJS.$(this).html("Close all");
		}
	});

	AJS.$('.rowGroup .formContainer').hide();

	AJS.$('.toggle').click(function() {
		var spanElement = AJS.$(this).children();
		var spanClass = spanElement.attr("class");
		var formCont = AJS.$(this).parent().parent().find('.formContainer');
		if(!(checkElementExpansion(formCont))) {
			spanElement.removeClass(spanClass).addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
		}
		else {
			spanElement.removeClass(spanClass).addClass("aui-icon aui-icon-small aui-iconfont-add");
		}
	    formCont.slideToggle();
	});
});