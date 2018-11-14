class Solid {

    String id
    List<Face> faces = []

    static void main(String[] args) {
        Edge testingEdge = new Edge(p1: new Point(x:5, y:1, z:1), p2: new Point(x:6, y:1, z:1))
        Point testingPoint = new Point(x:8, y:1, z:1)

        println dist(testingEdge, testingPoint)
//
        Point ep1 = new Point(x: 0, y:0, z:0)
        Point ep2 = new Point(x: 10, y:0, z:0)
        Point p1 = new Point(x: 3, y:1, z:1)
        Point p2 = new Point(x: 4, y:1, z:2)

        Edge e1 = new Edge(p1: ep1, p2: ep2)
        Edge e2 = new Edge(p1: p1, p2: p2)

//        println dist(e1, p1)
//        println dist(e1, e2)

        List<Solid> solids1 = solidsFromFile('src/main/resources/simpleSolid.txt')

//        println dist(solids1[0], solids1[1])

        List<Solid> solids2 = solidsFromFile('src/main/resources/solidsFromClass3.txt')

        println 'dist between solids: ' + dist(solids2[0], solids2[1])

        Face face1 = solids1[0].faces[0]
        Face face2 = solids1[1].faces[0]
        println 'dist between faces: ' + dist(face1, face2)
//        println face1.edges().collect { it.toString() }
//        println face2.edges().collect { it.toString() }

        Edge edge1 = face1.edges()[0]
        Edge edge2 = face2.edges()[0]

//        println edge1
//        println edge2
//        println dist(edge1, edge2)
    }

    private static List<Solid> solidsFromFile(String filePath) {
        String text = new File(filePath).getText()
        List<String> lines = text.split('\n')

        List<Solid> solids = []
        Solid solid = null
        List<Point> points = []

        for (String line : lines) {
            def newSolid = line =~ /-+\s*SOLID\s*\[(\d+)\]\s*-+/
            def newPoint = line =~ /([^\s]+)\s*;\s*([^\s]+)\s*;\s*([^\s]+)/

            if (newSolid) {
                if (solid) {
                    solids.add(solid)
                    solid = new Solid(id: newSolid[0][1])
                } else {
                    solid = new Solid(id: newSolid[0][1])
                }
            } else if (newPoint) {
                double x = Double.parseDouble(newPoint[0][1])
                double y = Double.parseDouble(newPoint[0][2])
                double z = Double.parseDouble(newPoint[0][3])
                points.add(new Point(x: x, y: y, z: z))

                if (points.size() >= 3) {
                    solid.faces.add(new Face(p1: points[0], p2: points[1], p3: points[2]))
                    points = []
                }
            }
        }

        if (solid) {
            solids.add(solid)
        }

        solids
    }

    private static class Face {
        Point p1, p2, p3

        List<Edge> edges(){
            [new Edge(p1: p1, p2: p2), new Edge(p1: p2, p2: p3), new Edge(p1: p3, p2: p1)]
        }

        List<Edge> edgesAndMiddle() {
            [new Edge(p1: p1, p2: p2), new Edge(p1: p2, p2: p3), new Edge(p1: p3, p2: p1),
             new Edge(p1: p1, p2: new Edge(p1: p2, p2: p3).middle()),
             new Edge(p1: p2, p2: new Edge(p1: p3, p2: p1).middle()),
             new Edge(p1: p3, p2: new Edge(p1: p1, p2: p2).middle())]
        }

        List<Point> pointsAndMiddles() {
            [p1, p2, p3] + edges().collect { it.middle() }
        }


        @Override
        String toString() {
            return "{" +
                    "" + p1 +
                    ", " + p2 +
                    ", " + p3 +
                    '}';
        }
    }

    private static class Edge {
        Point p1, p2


        Point middle() {
            p1.add(p2.add(p1.multiply(-1)).multiply(0.5))
        }

        boolean containsPoint(Point p) {
            double small = 0.00000001
            double length = dist(p1, p2)
            double distTo1 = dist(p1, p)
            double distTo2 = dist(p2, p)

            return Math.abs(length - distTo1 - distTo2) < small
        }

        @Override
        public String toString() {
            return "{" +
                    "" + p1 +
                    ", " + p2 +
                    '}'
        }
    }

    private static class Point {
        double x, y, z

        Point difference(Point other) {
            new Point(x: x-other.x, y: y-other.y, z: z-other.z)
        }

        double dot(Point other) {
            x*other.x + y*other.y + z*other.z
        }

        Point add(double value) {
            new Point(x: x+value, y: y+value, z: z+value)
        }

        Point add(Point other) {
            new Point(x: x+other.x, y: y+other.y, z: z+other.z)
        }

        Point multiply(double value) {
            new Point(x: x*value, y: y*value, z: z*value)
        }

        double length() {
            return dist(this, new Point(x:0, y:0, z:0))
        }


        @Override
        String toString() {
            return "($x, $y, $z)"
        }
    }

    static double dist(Point p1, Point p2) {
        Math.sqrt((p1.x - p2.x)**2 + (p1.y - p2.y)**2 + (p1.z - p2.z)**2)
    }

    static double dist(Edge e, Point p) {
        Point vector = e.p1.add(e.p2.multiply(-1)).multiply(1/dist(e.p1, e.p2))
        Point vectorToPoint = p.add(e.p2.multiply(-1))

        double t = vector.dot(vectorToPoint)
        Point pointOnLine = e.p2.add(vector.multiply(t))
        if (!e.containsPoint(pointOnLine)) {
            return Math.min(dist(p, e.p1), dist(p, e.p2))
        }
        dist(p, pointOnLine)
    }

    static double dist(Edge e1, Edge e2) {
        double almostZero = 0.00000001
        Point p0 = e1.p1
        Point p1 = e1.p2
        Point q0 = e2.p1
        Point q1 = e2.p2
        Point u = p1.difference(p0)
        Point v = q1.difference(q0)
        Point w0 = p0.difference(q0)

        double a = u.dot(u)
        double b = u.dot(v)
        double c = v.dot(v)
        double d = u.dot(w0)
        double e = v.dot(w0)

        double denominator = a*c-b*b
        double sc, sN, sD = (b*e - c*d) / denominator
        double tc, tN, tD = (a*e - b*d) / denominator

        if (denominator <= almostZero) {
            sN = 0
            sD = 1
            tN = e
            tD = c
        } else {
            sN = b*e - c*d
            tN = a*e - b*d
            if (sN < 0) {
                sN = 0
                tN = e
                tD = c
            } else {
                if (sN > sD) {
                    sN = sD
                    tN = e + b
                    tD = c
                }
            }
        }

        if (tN < 0) {
            tN = 0
            if (d > 0) {
                sN = 0
            } else if (-d > a) {
                sN = sD
            } else {
                sN = -d
                sD = a
            }
        } else if (tN > tD) {
            tN = tD
            if (-d + b < 0) {
                sN = 0
            } else if (-d + b > a) {
                sN = sD
            } else {
                sN = -d + b
                sD = a
            }
        }

        sc = Math.abs(sN) < almostZero ? 0 : sN / sD
        tc = Math.abs(tN) < almostZero ? 0 : tN / tD

        Point one = p0.add(u.multiply(sc))
        Point two = q0.add(v.multiply(tc))

        dist(one, two)
    }

    static double dist(Edge e, Face f) {
        f.edgesAndMiddle().collect { dist(it, e) }.min()
    }

    static List<Double> crossProduct(Point v1, Point v2, Point pointOnPlane) {
        double x = v1.y*v2.z - v1.z*v2.y
        double y = v1.z*v2.x - v1.x*v2.z
        double z = v1.x*v2.y - v1.y*v2.x

        double d = -pointOnPlane.x * x - pointOnPlane.y * y - pointOnPlane.z*z

        [x, y, z, d]
    }

    static double distanceToPlane(Point p, Point v1, Point v2, Point pointOnPlane) {
        def (double x, double y, double z, double d) = crossProduct(v1, v2, pointOnPlane)

        x * p.x + y * p.y + p.z * z + d / Math.sqrt(x**2 + y**2 + z**2)
    }

    static double dist(Face f, Point p) {
        Point v1 = f.p1.difference(f.p2)
        Point v2 = f.p2.difference(f.p3)
        Point v3 = f.p3.difference(f.p1)
        def (double x, double y, double z) = crossProduct(v1, v2, p)
        Point vector = new Point(x: x, y: y, z: z)

        double distToPlane = Math.abs(distanceToPlane(p, v1, v2, f.p1))

        double distToEdgePlane1 = distanceToPlane(p, v1, vector, f.p1)
        double distToEdgePlane2 = distanceToPlane(p, v2, vector, f.p2)
        double distToEdgePlane3 = distanceToPlane(p, v3, vector, f.p3)

        if (distToEdgePlane1 > 0 || distToEdgePlane2 > 0 || distToEdgePlane3 > 0) {
            def distBetweenEdges = f.edgesAndMiddle().collect { dist(it, p) }
            return distBetweenEdges.min()
        }

        distToPlane
    }

    static double dist(Face f1, Face f2) {
        double dist1 = f1.pointsAndMiddles().collect { dist(f2, it) }.min()
        double dist2 = f2.pointsAndMiddles().collect { dist(f1, it) }.min()

        Math.min(dist1, dist2)
    }

    static double dist(Solid s1, Solid s2) {
        s1.faces.collect { f1 -> s2.faces.collect { f2 -> dist(f1, f2)} }.flatten().min()
    }

}
