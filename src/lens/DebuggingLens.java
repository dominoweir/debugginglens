package lens;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;

class DebuggingLens extends JComponent implements ItemListener {

    // used to save cursor position
    Point currentPoint;
    Point oldPoint;
    Container contentPane;
    ArrayList<Component> componentsInRegion;
    private int width, height; // width and height of the debugging lens

    // Control Panel stuff
    private JFrame lensControlPanel;
    private JCheckBox borderLocationsFilt, borderWidthsFilt, componentSizesFilt, componentClassesFilt, fontMetricsFilt, layoutManagerFilt;
    private JPanel filtersPnl;
    private TitledBorder filtersBorder;

    public DebuggingLens(AbstractButton aButton, Container contentPane) {
        CheckBoxListener listener = new CheckBoxListener(aButton,this, contentPane);
        this.contentPane =  contentPane;
        addMouseListener(listener);
        addMouseMotionListener(listener);
        componentsInRegion = new ArrayList<>();

        // init lens size
        width = 100;
        height = 100;

        // control panel stuff
        initCtrlPnl();
        setupCtrlPnl();
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

    // construct the components on the control panel
    private void initCtrlPnl() {

        // control panel
        lensControlPanel = new JFrame("Debugging Lens Control Panel");
        filtersPnl = new JPanel();
            filtersBorder = new TitledBorder("Filters");
            filtersBorder.setTitleJustification(TitledBorder.LEFT);
            filtersBorder.setTitlePosition(TitledBorder.TOP);
            filtersPnl.setBorder(filtersBorder);

            borderLocationsFilt = new JCheckBox("Border Locations");
            borderWidthsFilt = new JCheckBox("Border Widths");
            componentSizesFilt = new JCheckBox("Component Sizes");
            componentClassesFilt = new JCheckBox("Component Classes");
            fontMetricsFilt = new JCheckBox("Component Sizes");
            layoutManagerFilt = new JCheckBox("Component Classes");

        lensControlPanel.pack();
        lensControlPanel.setVisible(true);

    }
    // setup and customize the components on the control panel
    private void setupCtrlPnl() {
        lensControlPanel.setLayout(new GridLayout(1, 1));
        lensControlPanel.add(filtersPnl);
            filtersPnl.setLayout(new GridLayout(6, 1));
            filtersPnl.add(borderLocationsFilt);
            filtersPnl.add(borderWidthsFilt);
            filtersPnl.add(componentSizesFilt);
            filtersPnl.add(componentClassesFilt);
            filtersPnl.add(fontMetricsFilt);
            filtersPnl.add(layoutManagerFilt);

        lensControlPanel.pack();
        lensControlPanel.setVisible(true);
    }

    // react to change button clicks
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
