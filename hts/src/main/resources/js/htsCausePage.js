AJS.$(document).ready(function(){
	AJS.$('.rowGroup .formContainer').hide();

	AJS.$('.toggle').click(function() {
		var spanElement = AJS.$(this).children();
		var spanClass = spanElement.attr("class");
		var formCont = AJS.$(this).parent().parent().parent().find('.formContainer');
		if(!(formCont.is(":visible"))) {
			spanElement.removeClass(spanClass).addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
		}
		else {
			spanElement.removeClass(spanClass).addClass("aui-icon aui-icon-small aui-iconfont-add");
		}
	    formCont.slideToggle();
	});
});