package lens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
    boolean locked, isResizing;
    private int width, height; // width and height of the debugging lens

    // Control Panel stuff
    private JFrame lensControlPanel;
    private JCheckBox borderLocationsFilt, borderWidthsFilt, componentSizesFilt, componentLocationsFilt, componentClassesFilt, fontMetricsFilt, layoutManagerFilt;
    private JPanel filtersPnl;
    private TitledBorder filtersBorder;

    public DebuggingLens(Container contentPane) {
        setFocusable(true);
        locked = false;
        isResizing = false;
        this.contentPane =  contentPane;

        // keeps track of all the components within the region of the debugging lens
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

        // enable/disable listener setup
        CheckBoxListener checkBoxListener = new CheckBoxListener(checkBox,this, contentPane);
        addMouseListener(checkBoxListener);
        addMouseMotionListener(checkBoxListener);
        checkBox.addItemListener(this);

        // lock lens in place listener
        LockListener lockListener = new LockListener(this, contentPane);
        addKeyListener(lockListener);
        setVisible(true);
    }

    // refreshes the list of the components within the debugging lens
    private void updateComponentsInRegion(){
        int newX = currentPoint.x;
        int newY = currentPoint.y;
        boolean inRegion;

        // get children components of the contentPane into a queue
        ArrayDeque<Component> componentQueue = new ArrayDeque<Component>();
        componentQueue.addAll(Arrays.asList(contentPane.getComponents()));

        /* for all the components in queue:
         - find their children (and add them to the queue for inspection too)
         - determine if the component is within the lens boundaries and if it is add it to componentsInRegion */
        while(!componentQueue.isEmpty()){
            inRegion = false;
            Component c = componentQueue.pop();

            // getLocation(), getX(), and getY() gives position relative to parent, convert to contentPane coords
            // so nested components display correctly
            Point absPos = new Point(SwingUtilities.convertPoint(c.getParent(), c.getLocation(), contentPane));

            // check if there are any child components within the current component
            try{
                // cast c to JPanel to allow us to use getComponents() method to access its children
                JPanel asPanel = (JPanel) c;
                // add children to queue (so we can later look if they have children too)
                componentQueue.addAll(Arrays.asList(asPanel.getComponents()));
            } catch(ClassCastException e){
                
            }

            // loop through all the (x,y) pixels positions in the lens
            for(int i = newX; i <= newX + width; i++){
                for(int j = newY; j <= newY + height; j++){

                    // check if the current pixel is within component c
                    if(absPos.x <= i && i <= absPos.x + c.getWidth()){
                        if(absPos.y <= j && j <= absPos.y + c.getHeight()){

                            // if it is add to componentsInRegion and flag loop to break
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
            // if component does not overlap region remove it from componentsInRegion if it was there before (due to an old lens position)
            if(!inRegion){
                if(componentsInRegion.contains(c)){
                    componentsInRegion.remove(c);
                }
            }
        }
    }

    // returns a relatively distinct color particular to the integer passed
    private Color generateDistinctColor(int i) {

        // this list of good looking distinct colors obtained from https://sashat.me/2017/01/11/list-of-20-simple-distinct-colors/
        String[] hexCodes = { "#e6194b", "#000075", "#3cb44b", "#f58231", "#ffe119", "#800000", "#469990", "#4363d8", "#911eb4", "#46f0f0", "#f032e6", "#bcf60c", "#fabebe", "#008080", "#e6beff", "#9a6324", "#aaffc3", "#808000", "#ffd8b1", "#808080"};
        int index = i % hexCodes.length;
        return Color.decode(hexCodes[index]);
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

            checkBox = new JCheckBox("Lens on/off");
            borderLocationsFilt = new JCheckBox("Border Locations");
            borderWidthsFilt = new JCheckBox("Border Widths");
            componentSizesFilt = new JCheckBox("Component Sizes");
            componentLocationsFilt = new JCheckBox("Component Locations");
            componentClassesFilt = new JCheckBox("Component Classes");
            fontMetricsFilt = new JCheckBox("Font Details");
            layoutManagerFilt = new JCheckBox("Layout Managers");

            borderLocationsFilt.setSelected(true);
    }
    // setup and customize the components on the control panel
    private void setupCtrlPnl() {
        lensControlPanel.setLayout(new BorderLayout());
        lensControlPanel.add(checkBox, BorderLayout.NORTH);
        lensControlPanel.add(filtersPnl, BorderLayout.CENTER);
            filtersPnl.setLayout(new GridLayout(7, 1));
            filtersPnl.add(borderLocationsFilt);
            filtersPnl.add(borderWidthsFilt);
            filtersPnl.add(componentSizesFilt);
            filtersPnl.add(componentLocationsFilt);
            filtersPnl.add(componentClassesFilt);
            filtersPnl.add(fontMetricsFilt);
            filtersPnl.add(layoutManagerFilt);

        lensControlPanel.setSize(250, 300);
        lensControlPanel.setVisible(true);
    }

    // react to change button clicks
    public void itemStateChanged(ItemEvent e) {
        setVisible(e.getStateChange() == ItemEvent.SELECTED);
    }

    protected void paintComponent(Graphics g) {

        // if the lens is being resized rubberband it
        if(isResizing) {

        }

        // refresh list of components within the lens
        updateComponentsInRegion();

        // counter used for color selection
        int i = 0;

        for(Component c : componentsInRegion) {

            Color color = generateDistinctColor(i);
            i++;
            g.setColor(color);

            // this is important for updating the next annotation location
            FontMetrics fm = c.getFontMetrics(c.getFont());
            int fontHeight = fm.getHeight();

            // getLocation(), getX(), and getY() gives position relative to parent, convert to contentPane coords Sso nested components display correctly
            Point absPos = new Point(SwingUtilities.convertPoint(c.getParent(), c.getLocation(), contentPane));

            // these will be updated as annotations are added to the component
            int annotationX = absPos.x;
            int annotationY = absPos.y + c.getHeight() + fontHeight;

            if(borderLocationsFilt.isSelected()){
                g.drawRect(absPos.x, absPos.y, c.getWidth(), c.getHeight());
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
                int componentX = absPos.x;
                int componentY = absPos.y;

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

    public void setIsResizing(boolean isResizing) {
        this.isResizing = isResizing;
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

    // flag if the lens is being resized
    boolean isResizing = false;

    public CheckBoxListener(Component liveButton, DebuggingLens glassPane, Container contentPane) {
        toolkit = Toolkit.getDefaultToolkit();
        this.liveButton = liveButton;
        this.glassPane = glassPane;
        this.contentPane = contentPane;
    }

    public void mouseMoved(MouseEvent e) {

        if(glassPane.locked) redispatchMouseEvent(e, false);
        else{ redispatchMouseEvent(e, true); }

    }

    public void mouseDragged(MouseEvent e) { redispatchMouseEvent(e, true); }

    public void mouseClicked(MouseEvent e) { redispatchMouseEvent(e, false); }

    public void mouseEntered(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mouseExited(MouseEvent e) {
        redispatchMouseEvent(e, false);
    }

    public void mousePressed(MouseEvent e) {

        // todo: check if resize corner was clicked
        isResizing = true;

        redispatchMouseEvent(e, false);
    }

    public void mouseReleased(MouseEvent e) {

        isResizing = false;

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
            glassPane.setIsResizing(isResizing);
            glassPane.repaint();
        }
    }
}

class LockListener extends KeyAdapter {
    DebuggingLens dl;
    Container contentPane;

    public LockListener(DebuggingLens dl, Container contentPane){
        this.dl = dl;
        this.contentPane = contentPane;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char keyChar = e.getKeyChar();
        if(keyChar == 'l' || keyChar == 'L'){
            dl.locked = !dl.locked;
            System.out.println(e.toString());
        }

    }

    @Override
    public void keyPressed(KeyEvent e) {
        char keyChar = e.getKeyChar();
        if(keyChar == 'l' || keyChar == 'L'){
            dl.locked = !dl.locked;
            System.out.println(e.toString());
        }

    }
}
