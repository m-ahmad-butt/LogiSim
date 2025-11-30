package org.scd.ui;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WireConnection {
    private static int idCounter = 0;
    
    private int connectionId;
    private GateComponent sourceGate;
    private Object targetComponent;
    private int targetInputIndex;
    private Color wireColor;
    private Point startPoint;
    private Point endPoint;
    private CircuitCanvas canvas;
    private int wireIndexFromSource;
    
    public WireConnection(GateComponent source, Object target, int targetInputIndex, CircuitCanvas canvas, int wireIndexFromSource) {
        this.connectionId = ++idCounter;
        this.sourceGate = source;
        this.targetComponent = target;
        this.targetInputIndex = targetInputIndex;
        this.canvas = canvas;
        this.wireIndexFromSource = wireIndexFromSource;
        this.wireColor = generateRandomColor();
        updatePoints();
    }
    
    private Color generateRandomColor() {
        int r = (int)(Math.random() * 200) + 55;
        int g = (int)(Math.random() * 200) + 55;
        int b = (int)(Math.random() * 200) + 55;
        return new Color(r, g, b);
    }

    public void updatePoints() {
        startPoint = sourceGate.getOutputPoint();        

        if (targetComponent instanceof GateComponent) {
            GateComponent targetGate = (GateComponent) targetComponent;
            if (targetInputIndex == 0) {
                endPoint = targetGate.getInput1Point();
            } else {
                endPoint = targetGate.getInput2Point();
            }
        } else if (targetComponent instanceof LEDComponent) {
            LEDComponent targetLED = (LEDComponent) targetComponent;
            endPoint = targetLED.getInputPoint();
        }
    }
    
    public void draw(Graphics2D g2d) {
        if (startPoint == null || endPoint == null) {
            return;
        }
        
        g2d.setColor(wireColor);
        g2d.setStroke(new BasicStroke(2));
        
        List<Point> path = calculatePath();
        
        for (int i = 0; i < path.size() - 1; i++) {
            Point p1 = path.get(i);
            Point p2 = path.get(i+1);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
        
        // Draw connection point indicator
        g2d.fillOval(endPoint.x - 3, endPoint.y - 3, 6, 6);
    }
    
    public java.util.List<int[]> getSegments() {
        java.util.List<int[]> segments = new java.util.ArrayList<>();
        
        if (startPoint == null || endPoint == null) {
            return segments;
        }
        
        List<Point> path = calculatePath();
        
        for (int i = 0; i < path.size() - 1; i++) {
            Point p1 = path.get(i);
            Point p2 = path.get(i+1);
            segments.add(new int[]{p1.x, p1.y, p2.x, p2.y});
        }
        
        return segments;
    }
    
    private List<Point> calculatePath() {
        int x1 = startPoint.x;
        int y1 = startPoint.y;
        int x2 = endPoint.x;
        int y2 = endPoint.y;
        
        // Strategy 1: Straight line (if aligned)
        int threshold = 10;
        if (Math.abs(y1 - y2) < threshold || Math.abs(x1 - x2) < threshold) {
            List<Point> path = new ArrayList<>();
            path.add(startPoint);
            path.add(endPoint);
            if (!isPathBlocked(path)) return path;
        }
        
        // Define candidate paths
        List<List<Point>> candidates = new ArrayList<>();
        
        // HV (Horizontal-Vertical)
        List<Point> hv = new ArrayList<>();
        hv.add(startPoint);
        hv.add(new Point(x2, y1));
        hv.add(endPoint);
        candidates.add(hv);
        
        // VH (Vertical-Horizontal)
        List<Point> vh = new ArrayList<>();
        vh.add(startPoint);
        vh.add(new Point(x1, y2));
        vh.add(endPoint);
        candidates.add(vh);
        
        // VHV (Standard Z) - Mid Y
        int midY = (y1 + y2) / 2;
        List<Point> vhv = new ArrayList<>();
        vhv.add(startPoint);
        vhv.add(new Point(x1, midY));
        vhv.add(new Point(x2, midY));
        vhv.add(endPoint);
        candidates.add(vhv);
        
        // HVH (Standard Z) - Mid X
        int midX = (x1 + x2) / 2;
        List<Point> hvh = new ArrayList<>();
        hvh.add(startPoint);
        hvh.add(new Point(midX, y1));
        hvh.add(new Point(midX, y2));
        hvh.add(endPoint);
        candidates.add(hvh);
        
        // VHV - Go Above (Avoid obstacles by going up)
        // Find min Y of all components to be safe? Or just a fixed offset?
        // Let's try a few fixed offsets relative to source/target
        int[] yOffsets = {-40, 40, -80, 80};
        for (int offset : yOffsets) {
            int altY = Math.min(y1, y2) + offset;
             // Ensure we don't go off-screen (too high)
            if (altY < 20) altY = 20;
            
            List<Point> altVhv = new ArrayList<>();
            altVhv.add(startPoint);
            altVhv.add(new Point(x1, altY));
            altVhv.add(new Point(x2, altY));
            altVhv.add(endPoint);
            candidates.add(altVhv);
        }

        // Check candidates
        for (List<Point> path : candidates) {
            if (!isPathBlocked(path)) {
                return path;
            }
        }
        
        // Fallback: Return standard path based on direction even if blocked
        if (x2 > x1) {
            return hv; // Prefer HV for rightward
        } else {
            return vhv; // Prefer VHV for leftward
        }
    }
    
    private boolean isPathBlocked(List<Point> path) {
        if (canvas == null) return false;
        
        for (int i = 0; i < path.size() - 1; i++) {
            Point p1 = path.get(i);
            Point p2 = path.get(i+1);
            
            // Check against Gates
            for (GateComponent gate : canvas.getGates()) {
                if (checkCollision(p1, p2, gate, i == 0, i == path.size() - 2)) {
                    return true;
                }
            }
            
            // Check against LEDs
            for (LEDComponent led : canvas.getLEDs()) {
                if (checkCollision(p1, p2, led, i == 0, i == path.size() - 2)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean checkCollision(Point p1, Point p2, Component component, boolean isFirstSegment, boolean isLastSegment) {
        // Skip source component for first segment
        if (isFirstSegment && component == sourceGate) return false;
        
        // Skip target component for last segment
        if (isLastSegment && component == targetComponent) return false;
        
        Rectangle bounds = component.getBounds();
        // Shrink bounds slightly to allow touching edges
        Rectangle shrunk = new Rectangle(bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4);
        
        return shrunk.intersectsLine(p1.x, p1.y, p2.x, p2.y);
    }
    
    public Point findIntersection(WireConnection other) {
        java.util.List<int[]> mySegments = this.getSegments();
        java.util.List<int[]> otherSegments = other.getSegments();
        
        for (int[] seg1 : mySegments) {
            for (int[] seg2 : otherSegments) {
                Point intersection = getLineIntersection(seg1[0], seg1[1], seg1[2], seg1[3],
                                                         seg2[0], seg2[1], seg2[2], seg2[3]);
                if (intersection != null) {
                    return intersection;
                }
            }
        }
        
        return null;
    }
    
    private Point getLineIntersection(int x1, int y1, int x2, int y2, 
                                      int x3, int y3, int x4, int y4) {
        double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (Math.abs(denom) < 0.001) return null;
        
        double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
        double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;
        
        if (ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1) {
            int x = (int)(x1 + ua * (x2 - x1));
            int y = (int)(y1 + ua * (y2 - y1));
            return new Point(x, y);
        }
        
        return null;
    }
    
    public boolean overlapsWith(WireConnection other) {
        if (this.startPoint == null || this.endPoint == null || 
            other.startPoint == null || other.endPoint == null) {
            return false;
        }
        
        return linesIntersect(
            this.startPoint.x, this.startPoint.y, this.endPoint.x, this.endPoint.y,
            other.startPoint.x, other.startPoint.y, other.endPoint.x, other.endPoint.y
        );
    }
    
    private boolean linesIntersect(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
        double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denom == 0) return false;
        
        double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
        double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;
        
        return (ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1);
    }
    
    // Getters
    public int getConnectionId() { return connectionId; }
    public GateComponent getSourceGate() { return sourceGate; }
    public Object getTargetComponent() { return targetComponent; }
    public int getTargetInputIndex() { return targetInputIndex; }
    public Color getWireColor() { return wireColor; }
    public Point getStartPoint() { return startPoint; }
    public Point getEndPoint() { return endPoint; }
}