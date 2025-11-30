package org.scd.business.model;


public class Not extends Gate {
    
    public Not() {
        super(0, "NOT", 1, 0, 0);
    }

    public Not(int componentId, int positionX, int positionY) {
        super(componentId, "NOT", 1, positionX, positionY);
    }
    
    /**
     * Copy constructor for cloning a NOT gate with offset position.
     * 
     * @param source The NOT gate to copy from
     * @param newId The new component ID
     * @param offsetX X-axis offset for position
     * @param offsetY Y-axis offset for position
     */
    public Not(Not source, int newId, int offsetX, int offsetY) {
        super(newId, "NOT", 1, source.positionX + offsetX, source.positionY + offsetY);
        this.input1 = new Input(source.input1);
        this.row = source.row;
        this.column = source.column;
    }
    
    @Override
    public void calculate() {
        if (input1 != null) {
            Integer val = input1.getValue();
            if (val != null) {
                output = (val == 1) ? 0 : 1;
            } else {
                output = null;
            }
        } else {
            output = null;
        }
    }
}
