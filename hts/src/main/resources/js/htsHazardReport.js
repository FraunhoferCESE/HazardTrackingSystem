AJS.$(document).ready(function() {
	var $ = AJS.$;

	$(".deleteHazardReport").click(function() {
		console.log("deleting");
		var self = $(this);
		console.log(self.data("key"));
		$.ajax({
			type: "delete",
			url: "hazardlist?key=" + self.data("key"),
			success: function(data) {
				console.log("success");
				console.log('dom', self, data);
				self.parent().parent().parent().parent().remove();
				if($("#hazardTable tr").length === 1) {
					location.reload();
				}
			},
			error: function() {
				console.log('error', arguments);
			}
		});
		return false;
	});
});