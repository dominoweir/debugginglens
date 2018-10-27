package lens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.event.MouseInputAdapter;

class DebuggingLens extends JComponent implements ItemListener {

    Point currentPoint;
    Point oldPoint;
    Container contentPane;
    ArrayList<Component> componentsInRegion;
    int width = 100;
    int height = 100;

    public DebuggingLens(AbstractButton aButton, Container contentPane) {
        CheckBoxListener listener = new CheckBoxListener(aButton,this, contentPane);
        this.contentPane =  contentPane;
        addMouseListener(listener);
        addMouseMotionListener(listener);
        componentsInRegion = new ArrayList<>();
    }

    private void updateComponentsInRegion(){
        double oldX = oldPoint.getX();
        double oldY = oldPoint.getY();
        double newX = currentPoint.getX();
        double newY = currentPoint.getY();
        int smallerX, biggerX, smallerY, biggerY;
        if(oldX > newX){
            smallerX = (int) newX;
            biggerX = (int) oldX;
        }
        else{
            smallerX = (int) oldX;
            biggerX = (int) newX;
        }
        if(oldY > newY){
            smallerY = (int) newY;
            biggerY = (int) oldY;
        }
        else{
            smallerY = (int) oldY;
            biggerY = (int) newY;
        }

        for(int i = smallerX; i < biggerX; i++){
            for(int j = smallerY; j < biggerY + height; j++){
                Point p = new Point(i, j);
                Point containerPoint = SwingUtilities.convertPoint(this, p, contentPane);
                Component component = SwingUtilities.getDeepestComponentAt(contentPane, containerPoint.x, containerPoint.y);
                if(componentsInRegion.contains(component)){
                    componentsInRegion.remove(component);
                }
            }
        }



    }

    // react to change button clicks.
    public void itemStateChanged(ItemEvent e) {
        setVisible(e.getStateChange() == ItemEvent.SELECTED);
    }

    protected void paintComponent(Graphics g) {

        updateComponentsInRegion();

        for (Component c : componentsInRegion){
            if(!(c instanceof JPanel)){
                g.setColor(Color.red);
                g.drawRect(c.getX(), c.getY(), c.getWidth(), c.getHeight());
            }
        }

        if (currentPoint != null) {
            g.setColor(Color.black);
            g.drawRect(currentPoint.x, currentPoint.y, width, height);
        }
    }

    public void setCurrentPoint(Point p) {
        oldPoint = currentPoint;
        currentPoint = p;
    }
}

/**
 * Listen for all events that our check box is likely to be
 * interested in.  Redispatch them to the check box.
 */
class CheckBoxListener extends MouseInputAdapter {
    Toolkit toolkit;
    Component liveButton;
    DebuggingLens glassPane;
    Container contentPane;

    public CheckBoxListener(Component liveButton, DebuggingLens glassPane, Container contentPane) {
        toolkit = Toolkit.getDefaultToolkit();
        this.liveButton = liveButton;
        this.glassPane = glassPane;
        this.contentPane = contentPane;
    }

    public void mouseMoved(MouseEvent e) {
        redispatchMouseEvent(e, true);
    }

    public void mouseDragged(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mouseClicked(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mouseEntered(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mouseExited(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mousePressed(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mouseReleased(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    // A basic implementation of re-dispatching events.
    private void redispatchMouseEvent(MouseEvent e, boolean repaint) {
        Point glassPanePoint = e.getPoint();
        Container container = contentPane;
        Point containerPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, contentPane);
        // we're not in the content pane
        if (containerPoint.y < 0) {
            // The mouse event is over non-system window decorations, such as the ones provided by
            // the Java look and feel. Could be handled specially.
        }
        else {
            // The mouse event is probably over the content pane, but we still need to find out exactly which component it's over.
            Component component = SwingUtilities.getDeepestComponentAt(container, containerPoint.x, containerPoint.y);
            // forward events over to the check box.
            if ((component != null) && (component.equals(liveButton))) {
                Point componentPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, component);
                component.dispatchEvent(new MouseEvent(component, e.getID(), e.getWhen(), e.getModifiers(), componentPoint.x, componentPoint.y, e.getClickCount(), e.isPopupTrigger()));
            }
        }
        // update the glass pane if requested.
        if (repaint) {
            glassPane.setCurrentPoint(glassPanePoint);
            glassPane.repaint();
        }
    }
}
