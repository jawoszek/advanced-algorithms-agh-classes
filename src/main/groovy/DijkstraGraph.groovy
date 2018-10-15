class DijkstraGraph {

    static Path algorithm(Graph graph, int startId, int endId) {
        graph.reset()

        Node firstNode = graph.nodes[startId]
        firstNode.currentCumulativeWeight = 0
        Set<Node> nodesInQueue = [firstNode].toSet()

        while (nodesInQueue) {
            Node currentNode = nodesInQueue.min { it.currentCumulativeWeight }
            nodesInQueue.remove(currentNode)

            if (currentNode.id == endId) {
                return pathToNode(currentNode)
            }

            for (Connection connection: currentNode.connections) {
                Node potentialNode = connection.to
                int potentialWeight = currentNode.currentCumulativeWeight + connection.weight

                if (potentialWeight < potentialNode.currentCumulativeWeight) {
                    potentialNode.currentCumulativeWeight = potentialWeight
                    potentialNode.currentParent = currentNode
                    nodesInQueue.add(potentialNode)
                }
            }
        }

        null
    }

    static Path pathToNode(Node node) {
        Node nodeToAdd = node
        List<Node> nodesInPath = []

        while (nodeToAdd) {
            nodesInPath.add(0, nodeToAdd)
            nodeToAdd = nodeToAdd.currentParent
        }

        new Path(path: nodesInPath)
    }

    static void main(String[] args) {
        int startId = 1
        int endId = 20
        Graph graph = graphFromFile()
        Path foundPath = algorithm(graph, startId, endId)
        println foundPath
    }

    static Graph graphFromFile() {
        String text = new File('src/main/resources/graphFromClass1.txt').getText()
        List<String> lines = text.split('\n')
        Map<Integer, Node> nodes = [:]

        for (String line: lines) {
            def (int id, int idTo, int weight) = line.split('; ').collect { it as Integer }
            Node node = nodes.get(id, new Node(id: id))
            Node connectedNode = nodes.get(idTo, new Node(id: idTo))
            Connection connection = new Connection(from: node, to: connectedNode, weight: weight)
            node.connections.add(connection)

            nodes.putAll([(id): node, (idTo): connectedNode])
        }

        new Graph(nodes: nodes)
    }
}

class Path {
    List<Node> path

    @Override
    String toString() {
        path.collect { "(node $it.id with length: $it.currentCumulativeWeight)" }.join('->')
    }
}

class Graph {
    Map<Integer, Node> nodes

    void reset() {
        nodes.values().each { it.reset() }
    }

    @Override
    String toString() {
        "Graph{nodes=$nodes}"
    }
}

class Node {
    int id
    List<Connection> connections = []
    int currentCumulativeWeight = Integer.MAX_VALUE
    Node currentParent = null

    void reset() {
        currentCumulativeWeight = Integer.MAX_VALUE
        currentParent = null
    }

    @Override
    String toString() {
        "Node{id=$id, connections=$connections, currentCumulativeWeight=$currentCumulativeWeight, currentParent=$currentParent}"
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Node node = (Node) o

        if (id != node.id) return false

        return true
    }

    int hashCode() {
        return id
    }
}

class Connection {
    Node from
    Node to
    int weight

    @Override
    String toString() {
        "Connection{from=$from.id, to=$to.id, weight=$weight}"
    }
}
