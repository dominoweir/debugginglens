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

        // set up the content pane, where the "main GUI" lives.
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new FlowLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        panel.add(new JButton("Button 1"));
        panel.add(new JButton("Button 2"));
        panel.add(new JButton("Button 3"));

        contentPane.add(panel);

        // Create the radio buttons.
        JRadioButton firstButton = new JRadioButton("Option 1");
        firstButton.setSelected(true);
        JRadioButton secondButton = new JRadioButton("Option 2");


        // Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(firstButton);
        group.add(secondButton);

        contentPane.add(firstButton);
        contentPane.add(secondButton);

        contentPane.add(new JComboBox<>(new String[]{"Combo 1", "Combo 2"}));

        // set up the glass pane, which appears over both menu bar and content pane and is an item listener on the change button.
        myDebuggingLens = new DebuggingLens(frame.getContentPane());
        frame.setGlassPane(myDebuggingLens);

        // show the window
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