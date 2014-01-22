
RED.view2 = function () {

    var space_width = 5000,
        space_height = 5000,
        lineCurveScale = 0.75,
        scaleFactor = 1,
        node_width = 100,
        node_height = 30;

    var drag = d3.behavior.drag().origin(Object).on("drag", function (d) {
        d.x = d3.event.x;
        d.y = d3.event.y;
        draw();
    });

    var nodes;

    var nodeMap = {};

    var links;

    var svg = d3.select("body").append("svg:svg")
        .attr("width", 900)
        .attr("height", 500);

    function calculateTextWidth(str) {
        var sp = document.createElement("span");
        sp.className = "node_label";
        sp.style.position = "absolute";
        sp.style.top = "-1000px";
        sp.innerHTML = (str || "").replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
        document.body.appendChild(sp);
        var w = sp.offsetWidth;
        document.body.removeChild(sp);
        return 35 + w;
    }

    function redraw() {
        console.log(RED.main.nodes.length);
        var node = svg.selectAll(".nodegroup").data(RED.nodes,function(d){return d.id});

        var nodes = node.enter().append("g").attr("class", "node nodegroup");
        nodes
            .attr("transform", function (d) {
                return "translate(" + d.x + "," + d.y + ")";
            });
    }

    return {
        redraw:redraw
    }
//        links.attr("d", function (d) {
//            d.source = nodeMap[into(d)];
//            d.target = nodeMap[out(d)];
//            d.w = 200;
//            d.h = 50;
//            var numOutputs = d.source.outputs || 1;
//            var sourcePort = d.sourcePort || 0;
//            var y = -((numOutputs - 1) / 2) * 13 + 13 * sourcePort;
//
//            var dy = d.target.y - (d.source.y + y);
//            var dx = (d.target.x - d.target.w / 2) - (d.source.x + d.source.w / 2);
//            var delta = Math.sqrt(dy * dy + dx * dx);
//            var scale = lineCurveScale;
//            var scaleY = 0;
//            if (delta < node_width) {
//                scale = 0.75 - 0.75 * ((node_width - delta) / node_width);
//            }
//
//            if (dx < 0) {
//                scale += 2 * (Math.min(5 * node_width, Math.abs(dx)) / (5 * node_width));
//                if (Math.abs(dy) < 3 * node_height) {
//                    scaleY = ((dy > 0) ? 0.5 : -0.5) * (((3 * node_height) - Math.abs(dy)) / (3 * node_height)) * (Math.min(node_width, Math.abs(dx)) / (node_width));
//                }
//            }
//
//            d.x1 = d.source.x + d.source.w / 2;
//            d.y1 = d.source.y + y;
//            d.x2 = d.target.x - d.target.w / 2;
//            d.y2 = d.target.y;
//            return "M " + (d.source.x + d.source.w / 2) + " " + (d.source.y + y) +
//                " C " + (d.source.x + d.source.w / 2 + scale * node_width) + " " + (d.source.y + y + scaleY * node_height) + " " +
//                (d.target.x - d.target.w / 2 - scale * node_width) + " " + (d.target.y - scaleY * node_height) + " " +
//                (d.target.x - d.target.w / 2) + " " + d.target.y;
//        })
//            .attr("stroke", "blue")
//            .attr("stroke-width", 2)
//            .attr("fill", "none");
}();