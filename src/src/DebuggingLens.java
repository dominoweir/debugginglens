package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class DebuggingLens extends JComponent implements MouseMotionListener {

    int x, y, width, height;

    public DebuggingLens(){
        x = 0;
        y = 0;
        width = 100;
        height = 100;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.RED);
        g.drawRect(x, y, width, height);
    }


    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.x = e.getX();
        this.y = e.getY();
        repaint();
    }
}
