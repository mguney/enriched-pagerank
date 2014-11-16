var mapFunction = function() {
	var pageId = this.pageId;
	var incomingLinks = this.incomingLinks;
	incomingLinks.forEach(function(edge) {
		emit(pageId, tojson(edge));
	});
};

function() {
	var pageId = this.pageId;
	var incomingLinks = this.incomingLinks;
	incomingLinks.forEach(function(edge) {
		var incomingNode = edge.nodeFrom;
		emit(pageId, incomingNode);
	});
};

