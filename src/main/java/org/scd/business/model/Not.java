package org.scd.business.model;


public class Not extends Gate {
    
    public Not(int componentId, int positionX, int positionY) {
        super(componentId, "NOT", 1, positionX, positionY);
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
