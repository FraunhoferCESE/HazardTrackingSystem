AJS.$(document).ready(function(){
	AJS.$('.rowGroup .formContainer').hide();

	AJS.$('.rowGroup').click(function() {
	    AJS.$(this).find('.formContainer').slideToggle();
	});
});