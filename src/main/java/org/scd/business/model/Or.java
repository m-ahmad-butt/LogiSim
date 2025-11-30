package org.scd.business.model;


public class Or extends Gate {
    
    public Or() {
        super(0, "OR", 2, 0, 0);
    }

    public Or(int componentId, int positionX, int positionY) {
        super(componentId, "OR", 2, positionX, positionY);
    }
    
    /**
     * Copy constructor for cloning an OR gate with offset position.
     * 
     * @param source The OR gate to copy from
     * @param newId The new component ID
     * @param offsetX X-axis offset for position
     * @param offsetY Y-axis offset for position
     */
    public Or(Or source, int newId, int offsetX, int offsetY) {
        super(newId, "OR", 2, source.positionX + offsetX, source.positionY + offsetY);
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
                output = (val1 == 1 || val2 == 1) ? 1 : 0;
            } else {
                output = null;
            }
        } else {
            output = null;
        }
    }
}
