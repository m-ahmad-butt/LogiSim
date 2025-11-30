package org.scd.business.model;


public class And extends Gate {
    
    public And() {
        super(0, "AND", 2, 0, 0);
    }

    public And(int componentId, int positionX, int positionY) {
        super(componentId, "AND", 2, positionX, positionY);
    }
    
    /**
     * Copy constructor for cloning an AND gate with offset position.
     * 
     * @param source The AND gate to copy from
     * @param newId The new component ID
     * @param offsetX X-axis offset for position
     * @param offsetY Y-axis offset for position
     */
    public And(And source, int newId, int offsetX, int offsetY) {
        super(newId, "AND", 2, source.positionX + offsetX, source.positionY + offsetY);
        // Clone inputs without connections (connections come from connectors)
        this.input1 = new Input(source.input1);
        this.input2 = new Input(source.input2);
        this.row = source.row;
        this.column = source.column;
    }
    
    @Override
    public void calculate() {
        if (input1 != null && input2 != null) {
            Integer val1 = input1.getValue();
            Integer val2 = input2.getValue();
            if (val1 != null && val2 != null) {
                output = (val1 == 1 && val2 == 1) ? 1 : 0;
            } else {
                output = null;
            }
        } else {
            output = null;
        }
    }
}
