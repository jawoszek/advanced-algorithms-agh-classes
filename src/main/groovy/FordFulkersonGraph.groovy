class FoldFulkersonGraph {

    static double algorithm(Graph graph, int startId, int endId) {
        graph.reset()
        Node startingNode = graph.nodes[startId]
        Path path

        double maxFlow = 0

        while (path = startingNode.findPathTo(endId)) {
            println "found path $path"
            double currentFlow = Double.MAX_VALUE

            for (Connection connection : path.path) {
                currentFlow = Math.min(currentFlow, connection.possibleFlow())
            }

            maxFlow += currentFlow

            for (Connection connection : path.path) {
                connection.flow += currentFlow
                connection.backFlow -= currentFlow
            }
        }
        maxFlow
    }

    static void main(String[] args) {
        int startId = 1
        int endId = 4
        Graph graph = graphFromFile()
        double foundFlow = algorithm(graph, startId, endId)
        println foundFlow
    }

    static Graph graphFromFile() {
        String text = new File('src/main/resources/conflictExampleForClass2.txt').getText()
        List<String> lines = text.split('\n')
        Map<Integer, Node> nodes = [:]

        for (String line : lines) {
            def (String idS, String idToS, String capacityS) = line.split('\t')
            int id = idS as int
            int idTo = idToS as int
            double capacity = capacityS as double
            Node node = nodes.get(id, new Node(id: id))
            Node connectedNode = nodes.get(idTo, new Node(id: idTo))
            Connection connection = new Connection(from: node, to: connectedNode, capacity: capacity)
            node.connections.add(connection)

            nodes.putAll([(id): node, (idTo): connectedNode])
        }

        new Graph(nodes: nodes)
    }

    static class Path {
        List<Connection> path

        @Override
        String toString() {
            path.collect { "(From node $it.from.id to node $it.to.id)" }.join('->')
        }
    }


    static class Graph {
        Map<Integer, Node> nodes

        void reset() {
            nodes.values().each { it.reset() }
        }

        @Override
        String toString() {
            "Graph{nodes=$nodes}"
        }
    }

    static class Node {
        int id
        List<Connection> connections = []

        Path findPathTo(int nodeId) {
            Set<Node> visited = [this].toSet()
            List<Node> queue = [this]
            Map<Node, Connection> parentConnection = [:]
            Connection lastConnection = null

            while (queue) {
                Node current = queue.pop()
                println current.id
                println queue
                for (Connection connection : current.connections) {
                    Node potential = connection.to
                    if (visited.contains(potential) || connection.possibleFlow() <= 0) {
                        continue
                    }

                    visited.add(potential)
                    queue.add(potential)
                    parentConnection[potential] = connection

                    if (potential.id == nodeId) {
                        lastConnection = connection
                        break
                    }
                }
            }

            if (!lastConnection) {
                return null
            }

            Connection currentConnection = lastConnection
            Path path = new Path(path: [])

            while (currentConnection) {
                path.path.add(0, currentConnection)
                currentConnection = parentConnection[currentConnection.from]
            }

            path
        }

        void reset() {
            connections.each { it.reset() }
        }

        @Override
        String toString() {
            "Node{id=$id}"
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

    static class Connection {
        Node from
        Node to
        double capacity
        double flow = 0
        double backFlow = 0

        double possibleFlow() {
            capacity - flow
        }

        void reset() {
            this.flow = 0
            this.backFlow = 0
        }

        @Override
        String toString() {
            "Connection{from=$from.id, to=$to.id, capacity=$capacity, flow=$flow, backFlow=$backFlow}"
        }
    }
}
