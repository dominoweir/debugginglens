package lens;

import javax.swing.*;
import java.awt.*;

public class GlassPaneDemo {
    static private DebuggingLens myDebuggingLens;

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("GlassPaneDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Start creating and adding components.
        JCheckBox changeButton =
                new JCheckBox("Glass pane \"visible\"");
        changeButton.setSelected(false);

        //Set up the content pane, where the "main GUI" lives.
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new FlowLayout());
        contentPane.add(changeButton);
        contentPane.add(new JButton("Button 1"));
        contentPane.add(new JButton("Button 2"));

        //Set up the menu bar, which appears above the content pane.
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        menu.add(new JMenuItem("Do nothing"));
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);

        //Set up the glass pane, which appears over both menu bar
        //and content pane and is an item listener on the change
        //button.
        myDebuggingLens = new DebuggingLens(changeButton, menuBar,
                frame.getContentPane());
        changeButton.addItemListener(myDebuggingLens);
        frame.setGlassPane(myDebuggingLens);

        //Show the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}