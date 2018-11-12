class Solid {

    String id
    List<Face> faces = []

    static void main(String[] args) {
        Point ep1 = new Point(x: 0, y:0, z:0)
        Point ep2 = new Point(x: 10, y:0, z:0)
        Point p1 = new Point(x: 3, y:1, z:1)
        Point p2 = new Point(x: 4, y:1, z:2)

        Edge e1 = new Edge(p1: ep1, p2: ep2)
        Edge e2 = new Edge(p1: p1, p2: p2)

        println dist(e1, p1)
        println dist(e1, e2)

        List<Solid> solids1 = solidsFromFile('src/main/resources/simpleSolid.txt')

        println dist(solids1[0], solids1[1])

        List<Solid> solids2 = solidsFromFile('src/main/resources/solidsFromClass3.txt')

        println dist(solids2[0], solids2[1])

        Face face1 = solids1[0].faces[0]
        Face face2 = solids1[1].faces[0]
        println dist(face1, face2)
        println face1.edges().collect { it.toString() }
        println face2.edges().collect { it.toString() }

        Edge edge1 = face1.edges()[0]
        Edge edge2 = face2.edges()[0]

        println edge1
        println edge2
        println dist(edge1, edge2)
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
    }

    private static class Edge {
        Point p1, p2


        @Override
        public String toString() {
            return "{" +
                    "" + p1 +
                    ", " + p2 +
                    '}';
        }
    }

    private static class Point {
        double x, y, z

        List<Face> faces
        List<Edge> edges

        List<Double> coordinates() {
            [x, y, z]
        }

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
        public String toString() {
            return "($x, $y, $z)"
        }
    }

    static double dist(Point p1, Point p2) {
        Math.sqrt((p1.x - p2.x)**2 + (p1.y - p2.y)**2 + (p1.z - p2.z)**2)
    }

    static double dist(Edge e, Point p) {
        double x1 = p.x
        double y1 = p.y
        double z1 = p.z

        double x2 = e.p1.x
        double y2 = e.p1.y
        double z2 = e.p1.z
        double x3 = e.p2.x
        double y3 = e.p2.y
        double z3 = e.p2.z

        double b = Math.sqrt(Math.pow((x2 - x3), 2)
                + Math.pow((y2 - y3), 2)
                + Math.pow((z2 - z3), 2))

        double S = Math.sqrt(Math.pow((y2 - y1) * (z3 - z1) - (z2 - z1) * (y3 - y1), 2) +
                Math.pow((z2 - z1) * (x3 - x1) - (x2 - x1) * (z3 - z1), 2) +
                Math.pow((x2 - x1) * (y3 - y1) - (y2 - y1) * (x3 - x1), 2)) / 2

        return 2 * S / b
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

        // TODO compare approaches
        // first approach
//        (p0.difference(q0)).add(((u.multiply(b*e-c*d)).difference(v.multiply(a*e-b*d))).multiply(1/denominator)).length()
    }
//
    static double dist(Edge e, Face f) {
        f.edges().collect { dist(it, e) }.min()
    }
//
    static double dist(Face f, Point p) {
        f.edges().collect { dist(it, p) }.min()
    }
//
    static double dist(Face f1, Face f2) {
        f1.edges().collect { e1 -> f2.edges().collect { e2 -> dist(e1, e2)} }.flatten().min()
    }
//
    static double dist(Solid s1, Solid s2) {
        s1.faces.collect { f1 -> s2.faces.collect { f2 -> dist(f1, f2)} }.flatten().min()
    }

}
