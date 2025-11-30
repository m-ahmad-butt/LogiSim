package org.scd.ui;

public class ResourcePath {
    private static ResourcePath instance;
    
    // Base paths
    private String gatesBasePath = "Gates/";
    private String ledBasePath = "LED/";
    
    // Gate folder names
    private String andFolder = "And/";
    private String orFolder = "Or/";
    private String notFolder = "Not/";
    
    private ResourcePath() {
    }
    
    public static ResourcePath getInstance() {
        if (instance == null) {
            instance = new ResourcePath();
        }
        return instance;
    }
    
    // AND gate image paths
    public String getAndWithoutInputs() {
        return gatesBasePath + andFolder + "and_without_inputs.png";
    }
    
    // OR gate image paths
    public String getOrWithoutInputs() {
        return gatesBasePath + orFolder + "or_without_inputs.png";
    }
    
    // NOT gate image paths
    public String getNotWithoutInput() {
        return gatesBasePath + notFolder + "not_without_input.png";
    }
    
    // LED image paths
    public String getLedOff() {
        return ledBasePath + "led_off.png";
    }
    
    public String getLedOn() {
        return ledBasePath + "led_on.png";
    }
    
    public void setGatesBasePath(String path) {
        this.gatesBasePath = path;
    }
    
    public void setLedBasePath(String path) {
        this.ledBasePath = path;
    }
}
