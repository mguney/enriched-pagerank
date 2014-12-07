function(pageId, incomingNodes) {
	var totalScore = 0;
	incomingNodes.forEach(function(node) {
		var outgoingEdges = node.outgoingLinks;
		var outgoingNodeCount = 0;
		outgoingEdges.forEach(function(outgoingEdge) {
			if (outgoingEdge.edgeType == "ExplicitLink") {
				outgoingNodeCount++;
			}
		});
		if (outgoingNodeCount != 0) {
			var incomingNodePageRank = incomingNode.pageRank;
			totalScore += incomingNodePageRank / outgoingNodeCount;
		}
	});
	var dumpingFactor = 0.85;
	var pageRank = (1 - dumpingFactor) + dumpingFactor * totalScore;
	var reducedObject = {
		pr : pageRank,
		pid : pageId
	};
	return reducedObject;
};