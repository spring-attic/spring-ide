/**
 * Copyright 2013 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

var RED = function () {


    $('#btn-keyboard-shortcuts').click(function () {
        showHelp();
    });

    function save(force) {
        if (RED.view.dirty()) {

            if (!force) {
                var invalid = false;
                RED.nodes.eachNode(function (node) {
                    invalid = invalid || !node.valid;
                });
                if (invalid) {
                    $("#node-dialog-confirm-deploy").dialog("open");
                    return;
                }
            }
            var nns = RED.nodes.createCompleteNodeSet();

            d3.xhr("flows").post(JSON.stringify(nns), function (err, resp) {
                if (resp && resp.status == 204) {
                    RED.notify("Successfully deployed", "success");
                    RED.view.dirty(false);
                    // Once deployed, cannot undo back to a clean state
                    RED.history.markAllDirty();
                } else {
                    if (resp) {
                        RED.notify("<strong>Error</strong>: " + resp, "error");
                    } else {
                        RED.notify("<strong>Error</strong>: no response from server", "error");
                    }
                    console.log(err, resp);
                }
            });
        }
    }

    $('#btn-deploy').click(function () {
        save();
    });

    $("#node-dialog-confirm-deploy").dialog({
        title: "Confirm deploy",
        modal: true,
        autoOpen: false,
        width: 530,
        height: 230,
        buttons: [
            {
                text: "Confirm deploy",
                click: function () {
                    save(true);
                    $(this).dialog("close");
                }
            },
            {
                text: "Cancel",
                click: function () {
                    $(this).dialog("close");
                }
            }
        ]
    });

    function loadNodes() {
    	console.log("Laoding");
        d3.xml("integration.xml", "application/xml", function (xml) {
            var xml_nodes = xml.documentElement.getElementsByTagName("*");
            var nodes = new Array();
            var transformers = new Array();
            var nodeMap = {};
            var ti = 0;
            for (var i = 0; i < xml_nodes.length; i++) {
                var d = xml_nodes[i];
                if (!d.id && !d.getAttribute("input-channel")) {
                    continue;
                }
                var node = new Object();
                nodes[i] = node;
                node.x = d.getAttribute("x");
                node.y = d.getAttribute("y");
                node.type = d.nodeName.substr(d.nodeName.indexOf(':') + 1);
                node.wires = new Array();
                if (d.id) {
                    console.log(d.id);
                    node.id = d.getAttribute("id");
                    node.label = d.id;
                } else {
                    console.log("t " + d.id);
                    transformers[ti] = node;
                    node.id = "transform";
                    node.input = d.getAttribute("input-channel");
                    node.output = d.getAttribute("output-channel");
                    console.log("n " + node.input);
                    console.log("n " + node.output);
                    ti++;
                }
                nodeMap[node.id] = node;
            }
            for (var i = 0; i < transformers.length; i++) {
                var transformer = transformers[i];
                console.log(transformer.input);
                console.log(transformer.output);
                var inputChannel = nodeMap[transformer.input];
                var outputChannel = nodeMap[transformer.output];
                console.log(inputChannel);
                console.log(outputChannel);
                inputChannel.wires.push(new Array(transformer.id));
                transformer.wires.push(outputChannel.id);
            }
            RED.nodes.import(nodes);
            RED.view.dirty(false);
            RED.view.redraw();
        });
    }

    function showHelp() {

        var dialog = $('#node-help');

        //$("#node-help").draggable({
        //        handle: ".modal-header"
        //});

        dialog.on('show', function () {
            RED.keyboard.disable();
        });
        dialog.on('hidden', function () {
            RED.keyboard.enable();
        });

        dialog.modal();
    }


    $(function () {
        RED.keyboard.add(/* ? */ 191, {shift: true}, function () {
            showHelp();
            d3.event.preventDefault();
        });
        loadNodes();
    });

    return {
    };
}
    ();


