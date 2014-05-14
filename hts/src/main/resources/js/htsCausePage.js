AJS.$(document).ready(function(){
	AJS.$('.rowGroup .formContainer').hide();

	AJS.$('.cell span').click(function() {
		console.log("HEHE");
		var spanClass = AJS.$(this).attr("class");
		var formCont = AJS.$(this).parent().parent().parent().find('.formContainer');
		console.log(formCont.is(":visible"));
		if(!(formCont.is(":visible"))) {
			AJS.$(this).removeClass(spanClass).addClass("aui-icon aui-icon-small aui-iconfont-devtools-task-disabled");
		}
		else {
			AJS.$(this).removeClass(spanClass).addClass("aui-icon aui-icon-small aui-iconfont-add");
		}
	    formCont.slideToggle();
	});
});