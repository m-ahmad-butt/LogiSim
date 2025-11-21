package org.scd.business.model;


public class And extends Gate {
    
    public And(int componentId, int positionX, int positionY) {
        super(componentId, "AND", 2, positionX, positionY);
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
