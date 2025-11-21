package org.scd.ui;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends JFrame {
    private CardLayout cl;
    private JPanel card;

    public Dashboard() {

        //main frame
        cl = new CardLayout();
        card = new JPanel(cl);
        mainPanel mainPage = new mainPanel(); //main panel
        card.add(mainPage,"mainPage");
        cl.show(card,"mainPage");
        add(card);
        
        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        
        JMenuItem newMenuItem = new JMenu("New");
        JMenuItem loadMenuItem = new JMenu("Load");
        JMenuItem saveMenuItem = new JMenu("Save");
        JMenuItem exportMenuItem = new JMenu("Export");
        
        menuBar.add(newMenuItem);
        menuBar.add(loadMenuItem);
        menuBar.add(saveMenuItem);
        menuBar.add(exportMenuItem);

        setJMenuBar(menuBar);
        
        // Make window fullscreen
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

}
