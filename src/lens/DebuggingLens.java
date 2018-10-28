package lens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;

public class DebuggingLens extends JComponent implements ItemListener {

    // used to save cursor position
    Point currentPoint;
    Point oldPoint;
    Container contentPane;
    JCheckBox checkBox;
    ArrayList<Component> componentsInRegion;
    private int width, height; // width and height of the debugging lens

    // Control Panel stuff
    private JFrame lensControlPanel;
    private JCheckBox borderLocationsFilt, borderWidthsFilt, componentSizesFilt, componentLocationsFilt, componentClassesFilt, fontMetricsFilt, layoutManagerFilt;
    private JPanel filtersPnl;
    private TitledBorder filtersBorder;

    public DebuggingLens(Container contentPane) {
        this.contentPane =  contentPane;
        componentsInRegion = new ArrayList<>();

        // init lens size
        width = 100;
        height = 100;

        // init lens position
        Point absoluteLocation = MouseInfo.getPointerInfo().getLocation();
        currentPoint = SwingUtilities.convertPoint(this, absoluteLocation, contentPane);
        oldPoint = currentPoint;

        // control panel stuff
        initCtrlPnl();
        setupCtrlPnl();

        // listener setup
        CheckBoxListener listener = new CheckBoxListener(checkBox,this, contentPane);
        addMouseListener(listener);
        addMouseMotionListener(listener);
        checkBox.addItemListener(this);
    }

    private void updateComponentsInRegion(){
        int newX = (int) currentPoint.getX();
        int newY = (int) currentPoint.getY();
        boolean inRegion;

        ArrayDeque<Component> componentQueue = new ArrayDeque<Component>();
        componentQueue.addAll(Arrays.asList(contentPane.getComponents()));

        while(!componentQueue.isEmpty()){
            inRegion = false;
            Component c = componentQueue.pop();

            // check if there are any child components within the current component
            try{
                JPanel asPanel = (JPanel) c;
                componentQueue.addAll(Arrays.asList(asPanel.getComponents()));
            } catch(ClassCastException e){
                
            }

            for(int i = newX; i <= newX + width; i++){
                for(int j = newY; j <= newY + height; j++){
                    if(c.getX() <= i && i <= c.getX() + c.getWidth()){
                        if(c.getY() <= j && j <= c.getY() + c.getHeight()){
                            inRegion = true;
                            if(!componentsInRegion.contains(c)){
                                componentsInRegion.add(c);
                            }
                        }
                    }
                }
                if(inRegion){
                    break;
                }
            }
            if(!inRegion){
                if(componentsInRegion.contains(c)){
                    componentsInRegion.remove(c);
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

            checkBox = new JCheckBox("Glass pane \"visible\"");
            borderLocationsFilt = new JCheckBox("Border Locations");
            borderWidthsFilt = new JCheckBox("Border Widths");
            componentSizesFilt = new JCheckBox("Component Sizes");
            componentLocationsFilt = new JCheckBox("Component Locations");
            componentClassesFilt = new JCheckBox("Component Classes");
            fontMetricsFilt = new JCheckBox("Font Details");
            layoutManagerFilt = new JCheckBox("Layout Managers");

            borderLocationsFilt.setSelected(true);

        lensControlPanel.pack();
        lensControlPanel.setVisible(true);

    }
    // setup and customize the components on the control panel
    private void setupCtrlPnl() {
        lensControlPanel.setLayout(new GridLayout(1, 1));
        lensControlPanel.add(filtersPnl);
            filtersPnl.setLayout(new GridLayout(8, 1));
            filtersPnl.add(checkBox);
            filtersPnl.add(borderLocationsFilt);
            filtersPnl.add(borderWidthsFilt);
            filtersPnl.add(componentSizesFilt);
            filtersPnl.add(componentLocationsFilt);
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

        for(Component c : componentsInRegion) {

            // this is important for updating the next annotation location
            FontMetrics fm = c.getFontMetrics(c.getFont());
            int fontHeight = fm.getHeight();

            // these will be updated as annotations are added to the component
            int annotationX = c.getX();
            int annotationY = c.getY() + c.getHeight() + fontHeight;

            if(borderLocationsFilt.isSelected()){
                g.setColor(Color.red);
                g.drawRect(c.getX(), c.getY(), c.getWidth(), c.getHeight());
                g.setColor(Color.black);
            }

            if(borderWidthsFilt.isSelected()){

            }

            if(componentSizesFilt.isSelected()){
                int componentWidth = c.getWidth();
                int componentHeight = c.getHeight();
                String widthString = "Width: " + Integer.toString(componentWidth);
                String heightString = "Height: " + Integer.toString(componentHeight);

                g.drawString(widthString, annotationX, annotationY);
                annotationY += fontHeight;
                g.drawString(heightString, annotationX, annotationY);
                annotationY += fontHeight;
            }

            if(componentLocationsFilt.isSelected()){
                int componentX = c.getX();
                int componentY = c.getY();

                String xString = "X: " + Integer.toString(componentX);
                String yString = "Y: " + Integer.toString(componentY);

                g.drawString(xString, annotationX, annotationY);
                annotationY += fontHeight;
                g.drawString(yString, annotationX, annotationY);
                annotationY += fontHeight;
            }

            if(componentClassesFilt.isSelected()){
                String className = c.getClass().toString().split(" ")[1];
                g.drawString(className, annotationX, annotationY);
                annotationY += fontHeight;
            }

            if(fontMetricsFilt.isSelected()){
                String fontName = fm.getFont().getFontName();
                String fontSize = Integer.toString(fm.getFont().getSize());
                String fontString = fontName + " (" + fontSize + " pt)";
                g.drawString(fontString, annotationX, annotationY);
                annotationY += fontHeight;
            }

            if(layoutManagerFilt.isSelected()){
                // only JPanels can have layout managers
                if(c instanceof JPanel){
                    JPanel p = (JPanel) c;
                    LayoutManager lm = p.getLayout();
                    String layoutName = lm.toString();
                    g.drawString(layoutName, annotationX, annotationY);
                }
            }
        }

        // actually draw the lens filter overlay
        g.setColor(Color.black);
        g.drawRect(currentPoint.x, currentPoint.y, width, height);
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
