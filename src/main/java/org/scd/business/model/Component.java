package org.scd.business.model;

import java.io.Serializable;


public interface Component extends Serializable {
    int getComponentId();
    String getComponentType();
    int getPositionX();
    int getPositionY();
    int getRow();
    int getColumn();
    void setRowColumn(int row, int column);
    void calculate();
}
