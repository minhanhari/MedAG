const graphUrl = "/graph" + window.location.search

const data = await d3.json(graphUrl);
if (data.error) {
    var main_area = document.querySelector("#main");
    result_area.innerHTML = '';
    var div = document.createElement('div');
    var head = document.createElement('h3');
    head.innerHTML = 'Error';
    div.appendChild(head);
    var e = document.createElement('div');
    e.appendChild(document.createTextNode(data.error));
    div.appendChild(e);
    result_area.appendChild(div);
}
else {
    console.log(data);
}

// Specify the color scale.
const color = d3.scaleOrdinal(d3.schemeCategory10);

// The force simulation mutates links and nodes, so create a copy
// so that re-evaluating this cell produces the same result.
const links = data.links.map(d => ({ ...d }));
const nodes = data.nodes.map(d => ({ ...d }));

var grapharea = d3.select("#graph-area");
var width = grapharea.node().getBoundingClientRect().width,
    height = 600;

// Create a simulation with several forces.
const simulation = d3.forceSimulation(nodes).alphaDecay(1 - 0.001 ** (1 / (links.length * 5)))
    .force("link", d3.forceLink(links).id(d => d.id))
    .force("charge", d3.forceManyBody().strength(-links.length))
    .force("center", d3.forceCenter(width / 2, height / 2))
    .on("tick", ticked);

// Create the SVG container.
var svg = grapharea.append("svg")
    .attr("height", height)
    .attr("viewBox", [0, 0, width, height])
    .attr("style", "max-width: 100%; height: auto;");

// Add a line for each link, and a circle for each node.
const link = svg.append("g")
    .attr("stroke", "#999")
    .attr("stroke-opacity", 0.6)
    .selectAll()
    .data(links)
    .join("line")
    .attr("stroke-width", d => Math.sqrt(d.value));

const node = svg.append("g")
    .attr("stroke", "#fff")
    .attr("stroke-width", 1.5)
    .selectAll()
    .data(nodes)
    .join("circle")
    .attr("r", 15)
    .attr("fill", "#ef6603")
    .style("cursor", "pointer")
    .on("click", clicked);

node.append("title")
    .text(d => d.id);

// Add a drag behavior.
node.call(d3.drag()
    .on("start", dragstarted)
    .on("drag", dragged)
    .on("end", dragended));

// Set the position attributes of links and nodes each time the simulation ticks.
function ticked() {
    link
        .attr("x1", d => d.source.x)
        .attr("y1", d => d.source.y)
        .attr("x2", d => d.target.x)
        .attr("y2", d => d.target.y);

    node
        .attr("cx", d => d.x)
        .attr("cy", d => d.y);
}

function clicked(event, d) {
    if (event.defaultPrevented) return; // dragged
    d3.select(this).transition()
        .attr("r", 24)
        .transition()
        .attr("r", 15);
    sendName(d.id);
}

// Reheat the simulation when drag starts, and fix the subject position.
function dragstarted(event) {
    if (!event.active) simulation.alphaTarget(0.3).restart();
    event.subject.fx = event.subject.x;
    event.subject.fy = event.subject.y;
}

// Update the subject (dragged node) position during drag.
function dragged(event) {
    event.subject.fx = event.x;
    event.subject.fy = event.y;
}

// Restore the target alpha so the simulation cools after dragging ends.
// Unfix the subject position now that it’s no longer being dragged.
function dragended(event) {
    if (!event.active) simulation.alphaTarget(0);
    d3.select(this).attr("stroke", "#fff");
    event.subject.fx = null;
    event.subject.fy = null;
}

const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/transfactor'
});

connect();

//stompClient.activate();

stompClient.onConnect = (frame) => {
    //setConnected(true);
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/nodeinfo', (regResult) => {
        showNodeInfo(JSON.parse(regResult.body));
    });
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function connect() {
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}

function sendName(name) {
    stompClient.publish({
        destination: "/app/transfactor",
        body: JSON.stringify({ 'id': name })
    });
    var result_area = document.querySelector("#info-area");
    result_area.innerHTML = '<div><p>Please wait...</p></div>';
}

function showNodeInfo(nodeinfo) {
    console.log(nodeinfo);
    var result_area = document.querySelector("#info-area");
    result_area.innerHTML = '';
    var table = document.createElement('table'),
        tbody = document.createElement('tbody');

    var rbp = nodeinfo.transFactorEntity;
    for (const [key, value] of Object.entries(rbp)) {
        var tr = document.createElement('tr'),
            th = document.createElement('th'),
            td = document.createElement('td');
        th.appendChild(document.createTextNode(properDisplay(key)));
        td.appendChild(document.createTextNode(value));
        tr.appendChild(th); tr.appendChild(td);
        tbody.appendChild(tr);
    }
    table.appendChild(tbody);
    result_area.appendChild(table);
}

function properDisplay(field) {
    switch (field) {
        case 'annotationId':
            return 'Annotation ID';
        case 'geneSymbol':
            return 'Gene symbol';
        case 'geneId':
            return 'Gene ID';
        case 'description':
            return 'Description';
        case 'synonyms':
            return 'Synonyms';
        default:
            return field;
    }
}

const neighbours = {};

nodes.forEach((node) => {
    neighbours[node.id] = neighbours[node.id] || [];
});

links.forEach((link) => {
    neighbours[link.source.id].push(link.target.id);
    neighbours[link.target.id].push(link.source.id);
});

const degrees = Object.fromEntries(Object.keys(neighbours).map(k => [k, neighbours[k].length]));

const minDegree = Math.min(...Object.values(degrees));
const maxDegree = Math.max(...Object.values(degrees));

function normalize(val, minval, maxval) {
    return (val - minval) / (maxval - minval);
}

const keysSorted = Object.keys(degrees).sort((a, b) => degrees[b] - degrees[a]);

const degreeCentralities = Object.fromEntries(keysSorted.map(k => [k, normalize(degrees[k], minDegree, maxDegree)]));

document.querySelectorAll('input[type="radio"][name="method"]').forEach((button) => {
    button.addEventListener('change', function () {
        if (this.checked) {
            if (this.value == "degree") {
                node.transition()
                    .attr("fill", d => d3.hsv(25, 0.99, degreeCentralities[d.id]))
            }
        }
    });
});