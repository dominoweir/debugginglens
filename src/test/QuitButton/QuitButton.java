package test.QuitButton;

import lens.DebuggingLens;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;


public class QuitButton{

    private static JPanel initUI() {

        JPanel panel = new JPanel();

        panel.setLayout(null);

        JButton quitButton = new JButton("Quit");
        quitButton.setBounds(50, 60, 80, 30);
        quitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });

        panel.add(quitButton);

        return panel;

    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        JPanel quitPanel = initUI();


        frame.setTitle("Quit button");
        frame.setSize(300, 200);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.getContentPane().add(quitPanel);

        DebuggingLens dl = new DebuggingLens(frame.getContentPane());
        frame.setGlassPane(dl);
        frame.setPreferredSize(new Dimension(200, 150));
        frame.pack();
        frame.setVisible(true);
    }
}
