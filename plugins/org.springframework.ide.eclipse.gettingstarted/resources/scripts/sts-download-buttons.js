console.log("STS support loading");

if (typeof sts_import_guide == "undefined") {
	sts_import_guide = function (url) {
		window.alert("You are trying to import "+url+" but this only works inside the STS dashboard");
	}
}

$(document).ready(function () {
	console.log("Document ready!");
	
//	$(".gs-guide-import").each(function (it) {
//		console.log("found "+it);
//	});

	$(".gs-guide-import").click(function (e) {
		var linkElement = e.target;
//		console.log(linkElement);
//		for (var prop in linkElement) {
//			console.log(prop);
//		}
		var url = linkElement.href;
		sts_import_guide(url);
		e.preventDefault();
	});
	
});
