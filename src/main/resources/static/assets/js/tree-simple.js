const graphUrl = "/graph" + window.location.search

const data = await d3.json(treeUrl);

console.log(data)

var recordarea = d3.select("#record-area");
var width = recordarea.node().getBoundingClientRect().width;

const marginTop = 10;
const marginRight = 10;
const marginBottom = 10;
const marginLeft = 10;

var rectNode = { width: 120, height: 50, textMargin: 5 };

// Rows are separated by dx pixels, columns by dy pixels. These names can be counter-intuitive
// (dx is a height, and dy a width). This because the tree must be viewed with the root at the
// “bottom”, in the data domain. The width of a column is based on the tree’s height.
const root = d3.hierarchy(data);
const dx = 60;
const dy = (width - marginRight - marginLeft) / (1 + root.height);

// Define the tree layout and the shape for links.
const tree = d3.tree().nodeSize([dx, dy]);
const diagonal = d3.linkHorizontal().x(d => d.y + rectNode.width).y(d => d.x + rectNode.height / 2);

// Create the SVG container, a layer for the links and a layer for the nodes.
const svg = recordarea.append("svg")
    .attr("width", width)
    .attr("height", dx)
    .attr("viewBox", [-marginLeft, -marginTop, width, dx])
    .attr("style", "max-width: 100%; height: auto; user-select: none;");

const gLink = svg.append("g")
    .attr("fill", "none")
    .attr("stroke", "#555")
    .attr("stroke-opacity", 0.4)
    .attr("stroke-width", 1.5);

const gNode = svg.append("g")
    .attr("cursor", "pointer")
    .attr("pointer-events", "all");

var blue = '#337ab7',
    green = '#5cb85c',
    yellow = '#f0ad4e',
    blueText = '#4ab1eb',
    purple = '#9467bd';

function update(event, source) {
    const duration = event?.altKey ? 2500 : 250; // hold the alt key to slow down the transition
    var nodes = root.descendants();
    var links = root.links();

    var maxDepth = 0;
    var maxTreeWidth = breadthFirstTraversal(nodes, function (currentLevel) {
        maxDepth++;
        currentLevel.forEach(function (node) {
            if (maxDepth == 1)
                node.color = blue;
            if (maxDepth == 2)
                node.color = green;
            if (maxDepth == 3)
                node.color = yellow;
            if (maxDepth >= 4)
                node.color = purple;
        });
    });

    nodes = nodes.reverse();

    // Compute the new tree layout.
    tree(root);

    let left = root;
    let right = root;
    root.eachBefore(node => {
        if (node.x < left.x) left = node;
        if (node.x > right.x) right = node;
    });

    const height = right.x - left.x + rectNode.height + marginTop + marginBottom;

    const transition = svg.transition()
        .duration(duration)
        .attr("height", height)
        .attr("viewBox", [-marginLeft, left.x - marginTop, width, height])
        .tween("resize", window.ResizeObserver ? null : () => () => svg.dispatch("toggle"));

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
            update(event, d);
        });
    nodeEnter.append('rect')
        .attr('rx', 6)
        .attr('ry', 6)
        .attr('width', rectNode.width)
        .attr('height', rectNode.height)
        .attr('class', 'node-rect')
        .attr('fill', function (d) { return d.color; })
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
                else info += key + ': ' + d.data[key] + '<br>'
            }
            return '<div style="width: '
                + (rectNode.width - rectNode.textMargin * 2) + 'px; height: '
                + (rectNode.height - rectNode.textMargin * 2) + 'px;" class="node-text wordwrap">'
                + '<b>' + d.data['name'] + '</b><br><br>'
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

// Do the first update to the initial configuration of the tree — where a number of nodes
// are open (arbitrarily selected as the root, plus nodes with 7 letters).
root.x0 = dy / 2;
root.y0 = 0;
root.descendants().forEach((d, i) => {
    d.id = i;
    d._children = d.children;
    if (d.depth && d.data.name.length !== 7) d.children = null;
});

update(null, root);

// Breadth-first traversal of the tree
// func function is processed on every node of a same level
// return the max level
function breadthFirstTraversal(tree, func) {
    var max = 0;
    if (tree && tree.length > 0) {
        var currentDepth = tree[0].depth;
        var fifo = [];
        var currentLevel = [];

        fifo.push(tree[0]);
        while (fifo.length > 0) {
            var node = fifo.shift();
            if (node.depth > currentDepth) {
                func(currentLevel);
                currentDepth++;
                max = Math.max(max, currentLevel.length);
                currentLevel = [];
            }
            currentLevel.push(node);
            if (node.children) {
                for (var j = 0; j < node.children.length; j++) {
                    fifo.push(node.children[j]);
                }
            }
        }
        func(currentLevel);
        return Math.max(max, currentLevel.length);
    }
    return 0;
}