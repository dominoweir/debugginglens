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
        // create and set up the window.
        JFrame frame = new JFrame("GlassPaneDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // start creating and adding components.
        JCheckBox changeButton = new JCheckBox("Glass pane \"visible\"");
        changeButton.setSelected(false);

        // set up the content pane, where the "main GUI" lives.
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new FlowLayout());
        // contentPane.add(changeButton);
        contentPane.add(new JButton("Button 1"));
        contentPane.add(new JButton("Button 2"));

        // set up the glass pane, which appears over both menu bar and content pane and is an item listener on the change button.
        myDebuggingLens = new DebuggingLens(changeButton, frame.getContentPane());
        changeButton.addItemListener(myDebuggingLens);
        frame.setGlassPane(myDebuggingLens);

        // show the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // schedule a job for the event-dispatching thread: creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}