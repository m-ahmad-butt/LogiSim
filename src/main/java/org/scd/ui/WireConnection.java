package org.scd.ui;
import java.awt.*;

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
        if (startPoint == null || endPoint == null || canvas == null) {
            return;
        }
        
        g2d.setColor(wireColor);
        g2d.setStroke(new BasicStroke(2));
        
        int x1 = startPoint.x;
        int y1 = startPoint.y;
        int x2 = endPoint.x;
        int y2 = endPoint.y;
        
        int sourceRow = sourceGate.getRow();
        int targetRow = getTargetRow();
        int sourceCol = sourceGate.getColumn();
        int targetCol = getTargetColumn();
        
        // Calculate vertical offset for multiple wires from same source
        int verticalOffset = wireIndexFromSource * 8; // Increased spacing
        
        // Horizontal offset from component edges
        int horizontalOffset = 35;
        
        // Determine direction
        boolean goingLeft = (targetCol < sourceCol);
        boolean goingDown = (targetRow > sourceRow);
        
        if (sourceRow == targetRow) {
            // Same row routing
            boolean directlyAdjacent = Math.abs(targetCol - sourceCol) == 1;
            
            if (directlyAdjacent && verticalOffset == 0) {
                // Direct connection for adjacent components
                g2d.drawLine(x1, y1, x2, y2);
            } else {
                // Route through channel above the row
                int channelIndex = Math.max(0, sourceRow - 1);
                int routingChannelY = (channelIndex < canvas.getRoutingChannels().size()) 
                    ? canvas.getRoutingChannels().get(channelIndex) - verticalOffset
                    : y1 - 50 - verticalOffset;
                
                int midX1 = goingLeft ? x1 - horizontalOffset : x1 + horizontalOffset;
                int midX2 = goingLeft ? x2 + horizontalOffset : x2 - horizontalOffset;
                
                g2d.drawLine(x1, y1, midX1, y1);
                g2d.drawLine(midX1, y1, midX1, routingChannelY);
                g2d.drawLine(midX1, routingChannelY, midX2, routingChannelY);
                g2d.drawLine(midX2, routingChannelY, midX2, y2);
                g2d.drawLine(midX2, y2, x2, y2);
            }
        } else {
            // Different rows routing
            int routingChannelY;
            
            if (goingDown) {
                // Route through channel below source row
                int channelIndex = sourceRow;
                routingChannelY = (channelIndex < canvas.getRoutingChannels().size()) 
                    ? canvas.getRoutingChannels().get(channelIndex) + verticalOffset
                    : y1 + 50 + verticalOffset;
            } else {
                // Route through channel above target row
                int channelIndex = Math.max(0, targetRow - 1);
                routingChannelY = (channelIndex < canvas.getRoutingChannels().size()) 
                    ? canvas.getRoutingChannels().get(channelIndex) - verticalOffset
                    : y2 - 50 - verticalOffset;
            }
            
            int midX1 = goingLeft ? x1 - horizontalOffset : x1 + horizontalOffset;
            int midX2 = goingLeft ? x2 + horizontalOffset : x2 - horizontalOffset;
            
            // Draw the path
            g2d.drawLine(x1, y1, midX1, y1);                      // Exit source
            g2d.drawLine(midX1, y1, midX1, routingChannelY);      // Vertical to channel
            g2d.drawLine(midX1, routingChannelY, midX2, routingChannelY); // Horizontal in channel
            g2d.drawLine(midX2, routingChannelY, midX2, y2);      // Vertical to target
            g2d.drawLine(midX2, y2, x2, y2);                      // Enter target
        }
        
        // Draw connection point indicator
        g2d.fillOval(endPoint.x - 3, endPoint.y - 3, 6, 6);
    }
    
    private int getTargetRow() {
        if (targetComponent instanceof GateComponent) {
            return ((GateComponent) targetComponent).getRow();
        } else if (targetComponent instanceof LEDComponent) {
            return ((LEDComponent) targetComponent).getRow();
        }
        return 0;
    }
    
    private int getTargetColumn() {
        if (targetComponent instanceof GateComponent) {
            return ((GateComponent) targetComponent).getColumn();
        } else if (targetComponent instanceof LEDComponent) {
            return ((LEDComponent) targetComponent).getColumn();
        }
        return 0;
    }
    
    public java.util.List<int[]> getSegments() {
        java.util.List<int[]> segments = new java.util.ArrayList<>();
        
        if (startPoint == null || endPoint == null || canvas == null) {
            return segments;
        }
        
        int x1 = startPoint.x;
        int y1 = startPoint.y;
        int x2 = endPoint.x;
        int y2 = endPoint.y;
        
        int sourceRow = sourceGate.getRow();
        int targetRow = getTargetRow();
        int sourceCol = sourceGate.getColumn();
        int targetCol = getTargetColumn();
        int horizontalOffset = 35;
        int verticalOffset = wireIndexFromSource * 8;
        
        boolean goingLeft = (targetCol < sourceCol);
        boolean goingDown = (targetRow > sourceRow);
        
        if (sourceRow == targetRow) {
            boolean directlyAdjacent = Math.abs(targetCol - sourceCol) == 1;
            
            if (directlyAdjacent && verticalOffset == 0) {
                segments.add(new int[]{x1, y1, x2, y2});
            } else {
                int channelIndex = Math.max(0, sourceRow - 1);
                int routingChannelY = (channelIndex < canvas.getRoutingChannels().size()) 
                    ? canvas.getRoutingChannels().get(channelIndex) - verticalOffset
                    : y1 - 50 - verticalOffset;
                
                int midX1 = goingLeft ? x1 - horizontalOffset : x1 + horizontalOffset;
                int midX2 = goingLeft ? x2 + horizontalOffset : x2 - horizontalOffset;
                
                segments.add(new int[]{x1, y1, midX1, y1});
                segments.add(new int[]{midX1, y1, midX1, routingChannelY});
                segments.add(new int[]{midX1, routingChannelY, midX2, routingChannelY});
                segments.add(new int[]{midX2, routingChannelY, midX2, y2});
                segments.add(new int[]{midX2, y2, x2, y2});
            }
        } else {
            int routingChannelY;
            
            if (goingDown) {
                int channelIndex = sourceRow;
                routingChannelY = (channelIndex < canvas.getRoutingChannels().size()) 
                    ? canvas.getRoutingChannels().get(channelIndex) + verticalOffset
                    : y1 + 50 + verticalOffset;
            } else {
                int channelIndex = Math.max(0, targetRow - 1);
                routingChannelY = (channelIndex < canvas.getRoutingChannels().size()) 
                    ? canvas.getRoutingChannels().get(channelIndex) - verticalOffset
                    : y2 - 50 - verticalOffset;
            }
            
            int midX1 = goingLeft ? x1 - horizontalOffset : x1 + horizontalOffset;
            int midX2 = goingLeft ? x2 + horizontalOffset : x2 - horizontalOffset;
            
            segments.add(new int[]{x1, y1, midX1, y1});
            segments.add(new int[]{midX1, y1, midX1, routingChannelY});
            segments.add(new int[]{midX1, routingChannelY, midX2, routingChannelY});
            segments.add(new int[]{midX2, routingChannelY, midX2, y2});
            segments.add(new int[]{midX2, y2, x2, y2});
        }
        
        return segments;
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