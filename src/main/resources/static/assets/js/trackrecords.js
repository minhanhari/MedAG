const graphUrl = "/track-records" + window.location.search

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

var trackarea = d3.select("#track-area");
var width = trackarea.node().getBoundingClientRect().width,
    height = 80;

// Create the SVG container.
var svg = trackarea.append("svg")
    .attr("height", height)
    .attr("viewBox", [0, 0, width, height])
    .attr("style", "max-width: 100%; height: auto;");

// Set the dimensions and margins of the diagram
const marginLeft = 40;
const marginTop = 40;
const marginRight = 40;
const marginBottom = 40;

const w = width - marginLeft - marginRight;
const h = height - marginTop - marginBottom;
const wrapper = d3.create("div");

const x0 = d3.scaleQuantize()
    .domain([0, 1])
    .range(data); // PiYG

const x1 = d3.scalePoint(data.map(d => d.event_date + "\/" + (data.indexOf(d) + 1)), [0, w]);
const x2 = d3.scalePoint(data, [0, w]);

const g = svg.append("g")
    .attr("transform", `translate(${marginLeft},${marginTop})`);

g.append("g")
    .attr("class", "axis axis--x")
    .call(d3.axisTop(x1).tickPadding(18));

g.append("g")
    .attr("stroke", "#fff")
    .attr("stroke-width", 5)
    .selectAll("circle")
    .data(x2.domain())
    .enter().append("circle")
    .attr("cx", x2)
    .attr("r", 15)
    .attr("fill", "#ef6603")
    .style("cursor", "pointer")
    .on("click", clicked);

function clicked(event, d) {
    if (event.defaultPrevented) return; // dragged
    d3.select(this).transition()
        .attr("r", 24)
        .transition()
        .attr("r", 15);
    documentInfo(d);
    makeTree(d);
}

var recordarea = d3.select("#record-area");

var rectNode = { width: 180, height: 90, textMargin: 5 };

// Rows are separated by dx pixels, columns by dy pixels. These names can be counter-intuitive
// (dx is a height, and dy a width). This because the tree must be viewed with the root at the
// “bottom”, in the data domain. The width of a column is based on the tree’s height.
const dx = 100;
const dy = 230;

// Define the tree layout and the shape for links.
const tree = d3.tree().nodeSize([dx, dy]);
const diagonal = d3.linkHorizontal().x(d => d.y + rectNode.width).y(d => d.x + rectNode.height / 2);

// Create the SVG container, a layer for the links and a layer for the nodes.
const svg1 = recordarea.append("svg")
    .attr("width", width)
    .attr("height", dx)
    .attr("viewBox", [-marginLeft, -marginTop, width, dx])
    .attr("style", "max-width: 100%; height: auto; user-select: none;");

const gLink = svg1.append("g")
    .attr("fill", "none")
    .attr("stroke", "#555")
    .attr("stroke-opacity", 0.4)
    .attr("stroke-width", 1.5);

const gNode = svg1.append("g")
    .attr("cursor", "pointer")
    .attr("pointer-events", "all");

function update(event, source, root) {
    const duration = event?.altKey ? 2500 : 250; // hold the alt key to slow down the transition
    var nodes = root.descendants().reverse();
    var links = root.links();

    // Compute the new tree layout.
    tree(root);

    let left = root;
    let right = root;
    root.eachBefore(node => {
        if (node.x < left.x) left = node;
        if (node.x > right.x) right = node;
    });

    const height = right.x - left.x + rectNode.height + marginTop + marginBottom;

    const transition = svg1.transition()
        .duration(duration)
        .attr("height", height)
        .attr("viewBox", [-marginLeft, left.x - marginTop, width, height])
        .tween("resize", window.ResizeObserver ? null : () => () => svg1.dispatch("toggle"));

    // Update the nodes…
    const node = gNode.selectAll("g")
        .data(nodes, d => d.id);

    // Enter any new nodes at the parent's previous position.
    const nodeEnter = node.enter().append("g")
        .attr("transform", d => `translate(${source.y0},${source.x0})`)
        .attr("fill-opacity", 0)
        .attr("stroke-opacity", 0)
        .on("click", (event, d) => {
            d.children = d.children ? null : d._children;
            update(event, d, root);
        });
    nodeEnter.append('rect')
        .attr('rx', 6)
        .attr('ry', 6)
        .attr('width', rectNode.width)
        .attr('height', rectNode.height)
        .attr('class', 'node-rect')
        .attr('fill', 'rgba(42, 44, 57, 0.9)')
        .attr('filter', 'url(#drop-shadow)');

    nodeEnter.append('foreignObject')
        .attr('x', rectNode.textMargin)
        .attr('y', rectNode.textMargin)
        .attr('width', function () {
            return (rectNode.width - rectNode.textMargin * 2) < 0 ? 0
                : (rectNode.width - rectNode.textMargin * 2)
        })
        .attr('height', function () {
            return (rectNode.height - rectNode.textMargin * 2) < 0 ? 0
                : (rectNode.height - rectNode.textMargin * 2)
        })
        .append('xhtml').html(function (d) {
            var info = '';
            for (var key in d.data) {
                if (key == 'children' || key == 'name') continue
                else info += key + ': ' + d.data[key] + '<br>';
            }
            return '<div style="color: rgb(255, 255, 255);font-size: 10px;width: '
                + (rectNode.width - rectNode.textMargin * 2) + 'px; height: '
                + (rectNode.height - rectNode.textMargin * 2) + 'px;" class="node-text wordwrap">'
                + '<b>' + d.data['name'] + '</b><br>'
                + info
                + '</div>';
        });

    // Transition nodes to their new position.
    const nodeUpdate = node.merge(nodeEnter).transition(transition)
        .attr("transform", d => `translate(${d.y},${d.x})`)
        .attr("fill-opacity", 1)
        .attr("stroke-opacity", 1);

    // Transition exiting nodes to the parent's new position.
    const nodeExit = node.exit().transition(transition).remove()
        .attr("transform", d => `translate(${source.y},${source.x})`)
        .attr("fill-opacity", 0)
        .attr("stroke-opacity", 0);

    // Update the links…
    const link = gLink.selectAll("path")
        .data(links, d => d.target.id);

    // Enter any new links at the parent's previous position.
    const linkEnter = link.enter().append("path")
        .attr("d", d => {
            const o = { x: source.x0, y: source.y0 };
            return diagonal({ source: o, target: o });
        });

    // Transition links to their new position.
    link.merge(linkEnter).transition(transition)
        .attr("d", function (d) {
            return diagonal({
                source: d.source,  // this is the same
                target: { x: d.target.x, y: d.target.y - (rectNode.width) }
            });
        });

    // Transition exiting nodes to the parent's new position.
    link.exit().transition(transition).remove()
        .attr("d", d => {
            const o = { x: source.x, y: source.y };
            return diagonal({ source: o, target: o });
        });

    // Stash the old positions for transition.
    root.eachBefore(d => {
        d.x0 = d.x;
        d.y0 = d.y;
    });
}

async function makeTree(data) {
    var document_type = data.document_type;
    var document_class = data.document_class_name;
    var id = data.id;

    const treeUrl = "/document-tree?id=" + id + "&document_type=" + document_type + "&document_class_name=" + document_class;

    const documentdata = await d3.json(treeUrl);
    console.log(data)

    const root = d3.hierarchy(documentdata);

    // Do the first update to the initial configuration of the tree — where a number of nodes
    // are open (arbitrarily selected as the root, plus nodes with 7 letters).
    root.x0 = dy / 2;
    root.y0 = 0;
    root.descendants().forEach((d, i) => {
        d.id = i;
        d._children = d.children;
        if (d.depth && d.data.name.length !== 7) d.children = null;
    });

    update(null, root, root);
}

function documentInfo(data) {
    var result_area = document.querySelector("#document-info-area");

    result_area.innerHTML = '';
    var table = document.createElement('table'),
        tbody = document.createElement('tbody');

    for (const [key, value] of Object.entries(data)) {
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
        case "id":
            return "ID";
        case "patient_uuid":
            return 'UUID';
        case 'document_class_id':
            return 'Class id';
        case 'document_class_name':
            return 'Class name';
        case 'document_type':
            return 'Type';
        case 'event_date':
            return 'Date';
        default:
            return field;
    }
}