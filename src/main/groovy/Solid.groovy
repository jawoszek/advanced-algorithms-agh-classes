class Solid {

    static void main(String[] args) {
        Point ep1 = new Point(x: 0, y:0, z:0)
        Point ep2 = new Point(x: 10, y:0, z:0)
        Point p1 = new Point(x: 4, y:1, z:1)

        Edge e = new Edge(p1: ep1, p2: ep2)

        println distance(e, p1)
    }

    List<Face> faces

    private static class Face {
        Point p1, p2, p3
    }

    private static class Edge {
        Point p1, p2
    }

    private static class Point {
        double x, y, z

        List<Face> faces
        List<Edge> edges
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
                + Math.pow((z2 - z3), 2));

        double S = Math.sqrt(Math.pow((y2 - y1) * (z3 - z1) - (z2 - z1) * (y3 - y1), 2) +
                Math.pow((z2 - z1) * (x3 - x1) - (x2 - x1) * (z3 - z1), 2) +
                Math.pow((x2 - x1) * (y3 - y1) - (y2 - y1) * (x3 - x1), 2)) / 2;

        return 2 * S / b
    }

//    static double dist(Edge e1, Edge e2) {
//        Math.sqrt((e1.p1.x -)**2 + ()**2 + ()**2)
//    }
//
//    static double dist(Edge e, Face f) {
//
//    }
//
//    static double dist(Face P, Point P) {
//
//    }
//
//    static double dist(Face f1, Face f2) {
//
//    }
//
//    static double dist(Solid s1, Solid s2) {
//
//    }

}
