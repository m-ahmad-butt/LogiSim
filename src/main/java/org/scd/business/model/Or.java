package org.scd.business.model;


public class Or extends Gate {
    
    public Or() {
        super(0, "OR", 2, 0, 0);
    }

    public Or(int componentId, int positionX, int positionY) {
        super(componentId, "OR", 2, positionX, positionY);
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
