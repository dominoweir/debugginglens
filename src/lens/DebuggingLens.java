package lens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;

public class DebuggingLens extends JComponent implements ItemListener {

    private boolean isLocked;
    private Container contentPane;
    private JCheckBox checkBox;
    private ArrayList<Component> componentsInRegion;
    private Point topLeftPoint;
    private Point resizingAnchorPoint;
    private Resizing isResizing;
    private int width, height;
    private HashSet<Point> annotations;

    // control Panel stuff
    private JFrame lensControlPanel;
    private JCheckBox borderLocationsFilt, componentSizesFilt, componentLocationsFilt, componentClassesFilt, fontMetricsFilt, layoutManagerFilt;
    private JPanel filtersPanel;
    private TitledBorder filtersBorder;

    // the current state of lens resizing (if it is being resized the position indicated the corner being dragged by the user)
    enum Resizing {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, FALSE
    }

    public DebuggingLens(Container contentPane) {
        isLocked = false;
        isResizing = Resizing.FALSE;
        this.contentPane =  contentPane;

        // keeps track of all the components within the region of the debugging lens
        componentsInRegion = new ArrayList<>();
        annotations = new HashSet<>();

        // init lens size
        width = 200;
        height = 200;

        // init lens position: convert the mouse's absolute location on the screen in to terms of the content pane
        Point absoluteLocation = MouseInfo.getPointerInfo().getLocation();
        topLeftPoint = SwingUtilities.convertPoint(this, absoluteLocation, contentPane);

        // initialize and construct the control panel
        initControlPanel();

        // enable/disable button listener setup
        CheckBoxListener checkBoxListener = new CheckBoxListener(checkBox,this, contentPane);
        addMouseListener(checkBoxListener);
        addMouseMotionListener(checkBoxListener);
        checkBox.addItemListener(this);

        // lock lens in place listener setup
        setFocusable(true);
        requestFocus();
        LockListener lockListener = new LockListener(this);
        addKeyListener(lockListener);
        setVisible(true);
    }

    // refreshes the list of the components within the debugging lens
    private void updateComponentsInRegion(){
        int newX = topLeftPoint.x;
        int newY = topLeftPoint.y;
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
            // getLocation(), getX(), and getY() gives position relative to parent, convert to contentPane coordinates
            // so nested components display correctly
            Point absPos = new Point(SwingUtilities.convertPoint(c.getParent(), c.getLocation(), contentPane));
            // check if there are any child components within the current component
            // cast c, which allows us to use getComponents() method to access its children
            // then add children to queue (so we can later look if they have children too)
            if(c instanceof JPanel){
                JPanel asPanel = (JPanel) c;
                componentQueue.addAll(Arrays.asList(asPanel.getComponents()));
            }
            else if(c instanceof JSplitPane){
                JSplitPane asPane = (JSplitPane) c;
                componentQueue.addAll(Arrays.asList(asPane.getComponents()));
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
        String[] hexCodes = { "#e6194b", "#000075", "#3cb44b", "#f58231", "#800000", "#469990", "#4363d8", "#911eb4", "#f032e6", "#008080", "#9a6324", "#808000", "#808080"};
        int index = i % hexCodes.length;
        return Color.decode(hexCodes[index]);
    }

    // constructs the components on the control panel
    private void initControlPanel() {
        lensControlPanel = new JFrame("Debugging Lens Control Panel");
        filtersPanel = new JPanel();

        filtersBorder = new TitledBorder("Filters");
        filtersBorder.setTitleJustification(TitledBorder.LEFT);
        filtersBorder.setTitlePosition(TitledBorder.TOP);
        filtersPanel.setBorder(filtersBorder);

        checkBox = new JCheckBox("Lens on/off");
        borderLocationsFilt = new JCheckBox("Border Locations");
        componentSizesFilt = new JCheckBox("Component Sizes");
        componentLocationsFilt = new JCheckBox("Component Locations");
        componentClassesFilt = new JCheckBox("Component Classes");
        fontMetricsFilt = new JCheckBox("Font Details");
        layoutManagerFilt = new JCheckBox("Layout Managers");

        borderLocationsFilt.setSelected(true);

        lensControlPanel.setLayout(new BorderLayout());
        lensControlPanel.add(checkBox, BorderLayout.NORTH);
        lensControlPanel.add(filtersPanel, BorderLayout.CENTER);

        filtersPanel.setLayout(new GridLayout(7, 1));
        filtersPanel.add(borderLocationsFilt);
        filtersPanel.add(componentSizesFilt);
        filtersPanel.add(componentLocationsFilt);
        filtersPanel.add(componentClassesFilt);
        filtersPanel.add(fontMetricsFilt);
        filtersPanel.add(layoutManagerFilt);

        lensControlPanel.setSize(250, 300);
        lensControlPanel.setVisible(true);
    }

    // react to change button clicks
    public void itemStateChanged(ItemEvent e) {
        setVisible(e.getStateChange() == ItemEvent.SELECTED);
    }

    protected void paintComponent(Graphics g) {

        // request focus on every redraw
        requestFocus();
      
        // get mouse location (for use in rubberbanding)
        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mouseLocation, contentPane);

        // upper left coordinates of the new lens
        int xMin = topLeftPoint.x;
        int yMin = topLeftPoint.y;

        // if resizing determine the drawing coordinates around the anchor point
        if(isResizing != Resizing.FALSE) {
            xMin = Math.min(resizingAnchorPoint.x, mouseLocation.x);
            yMin = Math.min(resizingAnchorPoint.y, mouseLocation.y);
            width = Math.abs(resizingAnchorPoint.x - mouseLocation.x);
            height = Math.abs(resizingAnchorPoint.y - mouseLocation.y);
        }

        // set clipping rectangle for lens (will not draw anything outside of the lens bounds)
        g.setClip(xMin, yMin, width + 1, height + 1);

        // refresh list of components within the lens
        updateComponentsInRegion();

        // counter used for color selection
        int i = 0;

        // draw annotations for each component
        for(Component c : componentsInRegion) {
            Color color = generateDistinctColor(i);
            i++;
            g.setColor(color);
            drawAnnotations(c, g);
        }

        // actually draw the lens filter overlay
        g.setColor(Color.black);
        g.drawRect(xMin, yMin, width, height);

        // check if mouse is hovering over a corner of the lens, bring up circle to show it can be resized by dragging
        if(isLocked && isResizing == Resizing.FALSE){
            int mouseX = mouseLocation.x;
            int mouseY = mouseLocation.y;

            g.setClip(contentPane.getX(), contentPane.getY(), contentPane.getWidth(), contentPane.getHeight());
            g.setColor(Color.blue);

            if(topLeftPoint.x - 5 <= mouseX && mouseX <= topLeftPoint.x + 5){
                // top left
                if(topLeftPoint.y - 5 <= mouseY && mouseY <= topLeftPoint.y + 5){
                    g.fillOval(topLeftPoint.x - 5, topLeftPoint.y - 5, 10, 10);
                }
                else if(topLeftPoint.y - 5 + height <= mouseY && mouseY <= topLeftPoint.y + 5 + height){
                    // bottom left
                    g.fillOval(topLeftPoint.x - 5, topLeftPoint.y - 5 + height, 10, 10);
                }
            }
            else if(topLeftPoint.x - 5  + width <= mouseX && mouseX <= topLeftPoint.x + 5 + width){
                if(topLeftPoint.y - 5 <= mouseY && mouseY <= topLeftPoint.y + 5){
                    // top right
                    g.fillOval(topLeftPoint.x - 5 + width, topLeftPoint.y - 5, 10, 10);
                }
                else if(topLeftPoint.y - 5 + height <= mouseY && mouseY <= topLeftPoint.y + 5 + height){
                    // bottom right
                    g.fillOval(topLeftPoint.x - 5 + width, topLeftPoint.y - 5 + height, 10, 10);
                }
            }
        }
    }

    private void drawAnnotations(Component c, Graphics g){
        // this is important for updating the next annotation location
        FontMetrics fm = c.getFontMetrics(c.getFont());
        int fontHeight = fm.getHeight();

        // getLocation(), getX(), and getY() gives position relative to parent, convert to contentPane coords Sso nested components display correctly
        Point absolutePosition = new Point(SwingUtilities.convertPoint(c.getParent(), c.getLocation(), contentPane));

        // these will be updated as annotations are added to the component
        int annotationX = absolutePosition.x;
        int annotationY = absolutePosition.y + c.getHeight() + fontHeight;

        if(borderLocationsFilt.isSelected()){
            g.drawRect(absolutePosition.x, absolutePosition.y, c.getWidth(), c.getHeight());
        }

        if(componentSizesFilt.isSelected()){
            int componentWidth = c.getWidth();
            int componentHeight = c.getHeight();
            String widthString = "W: " + Integer.toString(componentWidth);
            String heightString = "H: " + Integer.toString(componentHeight);

            g.drawString(widthString, annotationX, annotationY);
            annotationY += fontHeight;
            g.drawString(heightString, annotationX, annotationY);
            annotationY += fontHeight;
        }

        if(componentLocationsFilt.isSelected()){
            int componentX = absolutePosition.x;
            int componentY = absolutePosition.y;

            String xString = "X: " + Integer.toString(componentX);
            String yString = "Y: " + Integer.toString(componentY);

            g.drawString(xString, annotationX, annotationY);
            annotationY += fontHeight;
            g.drawString(yString, annotationX, annotationY);
            annotationY += fontHeight;
        }

        if(componentClassesFilt.isSelected()){
            String className = c.getClass().toString().split(" ")[1];
            if(fm.stringWidth(className) > c.getWidth()){
                int processed = 0;
                while(processed < className.length()){
                    StringBuilder sb = new StringBuilder();
                    while(fm.stringWidth(sb.toString()) < c.getWidth() && processed < className.length()){
                        sb.append(className.charAt(processed));
                        processed++;
                    }
                    g.drawString(sb.toString(), annotationX, annotationY);
                    annotationY += fontHeight;
                }
            }
            else{
                g.drawString(className, annotationX, annotationY);
                annotationY += fontHeight;
            }
        }

        if(fontMetricsFilt.isSelected()){
            String fontName = fm.getFont().getFontName();
            String fontSize = Integer.toString(fm.getFont().getSize());
            String fontString = fontName + " (" + fontSize + " pt)";
            if(fm.stringWidth(fontString) > c.getWidth()){
                int processed = 0;
                while(processed < fontString.length()){
                    StringBuilder sb = new StringBuilder();
                    while(fm.stringWidth(sb.toString()) < c.getWidth() && processed < fontString.length()){
                        sb.append(fontString.charAt(processed));
                        processed++;
                    }
                    g.drawString(sb.toString(), annotationX, annotationY);
                    annotationY += fontHeight;
                }
            }
            else{
                g.drawString(fontString, annotationX, annotationY);
                annotationY += fontHeight;
            }
        }

        if(layoutManagerFilt.isSelected()){
            // only JPanels can have layout managers
            if(c instanceof JPanel){
                JPanel p = (JPanel) c;
                LayoutManager lm = p.getLayout();
                String layoutName = lm.toString();
                if(fm.stringWidth(layoutName) > c.getWidth()){
                    int processed = 0;
                    while(processed < layoutName.length()){
                        StringBuilder sb = new StringBuilder();
                        while(fm.stringWidth(sb.toString()) < c.getWidth() && processed < layoutName.length()){
                            sb.append(layoutName.charAt(processed));
                            processed++;
                        }
                        g.drawString(sb.toString(), annotationX, annotationY);
                        annotationY += fontHeight;
                    }
                }
                else{
                    g.drawString(layoutName, annotationX, annotationY);
                }
            }
        }
    }

    // check if the mouse is located on or very near one of the corners of the lens
    public Resizing mouseIsOnCorner(){
        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mouseLocation, contentPane);
        int mouseX = mouseLocation.x;
        int mouseY = mouseLocation.y;

        // one of the two left hand corners
        if(topLeftPoint.x - 5 <= mouseX && mouseX <= topLeftPoint.x + 5){
            // top left corner
            if(topLeftPoint.y - 5 <= mouseY && mouseY <= topLeftPoint.y + 5){
                return Resizing.TOP_LEFT;
            }
            // bottom left corner
            else if(topLeftPoint.y - 5 + height <= mouseY && mouseY <= topLeftPoint.y + 5 + height){
                return Resizing.BOTTOM_LEFT;
            }
        }
        // one of the two right hand corners
        else if(topLeftPoint.x - 5  + width <= mouseX && mouseX <= topLeftPoint.x + 5 + width){
            // top right corner
            if(topLeftPoint.y - 5 <= mouseY && mouseY <= topLeftPoint.y + 5){
                return Resizing.TOP_RIGHT;
            }
            // bottom right corner
            else if(topLeftPoint.y - 5 + height <= mouseY && mouseY <= topLeftPoint.y + 5 + height){
                return Resizing.BOTTOM_RIGHT;
            }
        }
        // not on any corner
        return Resizing.FALSE;
    }

    public void setTopLeftPoint(Point p) { topLeftPoint = p; }

    public Resizing getIsResizing(){ return isResizing; }

    public void setIsResizing(Resizing isResizing) { this.isResizing = isResizing; }

    public void setIsLocked(boolean isLocked){ this.isLocked = isLocked; }

    // takes the enum description of the corner being resized and saves the relevant anchor corner point
    public void setResizingAnchorPoint(Resizing corner) {
        switch (corner) {
            case TOP_LEFT:
                // bottom right corner is the anchor
                resizingAnchorPoint = new Point(topLeftPoint.x + width, topLeftPoint.y + height);
                break;
            case TOP_RIGHT:
                // bottom left corner is the anchor
                resizingAnchorPoint = new Point(topLeftPoint.x, topLeftPoint.y + height);
                break;
            case BOTTOM_LEFT:
                // top right corner is the anchor
                resizingAnchorPoint = new Point(topLeftPoint.x + width, topLeftPoint.y);
                break;
            case BOTTOM_RIGHT:
                // top left corner is the anchor
                resizingAnchorPoint = topLeftPoint;
                break;
            default:
                resizingAnchorPoint = null;
                break;
        }
    }

    // gets anchor point
    public Point getResizingAnchorPoint() { return resizingAnchorPoint; }

    public boolean getIsLocked(){ return isLocked; }
}

/**
 * Listen for all events that our check box is likely to be
 * interested in.  Redispatch them to the check box.
 */
class CheckBoxListener extends MouseInputAdapter {
    private Component liveButton;
    private DebuggingLens debuggingLens;
    private Container contentPane;

    public CheckBoxListener(Component liveButton, DebuggingLens debuggingLens, Container contentPane) {
        this.liveButton = liveButton;
        this.debuggingLens = debuggingLens;
        this.contentPane = contentPane;
    }

    public void mouseMoved(MouseEvent e) { redispatchMouseEvent(e, true); }

    public void mouseDragged(MouseEvent e) {
        if(debuggingLens.getIsResizing() != DebuggingLens.Resizing.FALSE){
            redispatchMouseEvent(e, true);
        }
        else{
            redispatchMouseEvent(e, false);
        }

    }

    public void mouseClicked(MouseEvent e) { redispatchMouseEvent(e, false); }

    public void mouseEntered(MouseEvent e) { redispatchMouseEvent(e, false); }

    public void mouseExited(MouseEvent e) { redispatchMouseEvent(e, false); }

    public void mousePressed(MouseEvent e) {

        // check if mouse is on corner if lens is locked
        if((debuggingLens.mouseIsOnCorner() != DebuggingLens.Resizing.FALSE) && debuggingLens.getIsLocked()){

            // enter resizing mode for the appropriate corner
            debuggingLens.setIsResizing(debuggingLens.mouseIsOnCorner());
            // set the anchor point for the lens to rubber band around
            debuggingLens.setResizingAnchorPoint(debuggingLens.mouseIsOnCorner());

            redispatchMouseEvent(e, true);
        }
        else{
            redispatchMouseEvent(e, false);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if(debuggingLens.getIsResizing() != DebuggingLens.Resizing.FALSE){

            // update the top left corner
            Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(mouseLocation, contentPane);

            // set the top left point equal to the final result of rubberbanding
            int xMin = Math.min(debuggingLens.getResizingAnchorPoint().x, mouseLocation.x);
            int yMin = Math.min(debuggingLens.getResizingAnchorPoint().y, mouseLocation.y);
            debuggingLens.setTopLeftPoint(new Point(xMin, yMin));

            // exit rubberbanding mode
            debuggingLens.setIsResizing(DebuggingLens.Resizing.FALSE);
            redispatchMouseEvent(e, true);

            // reset cursor to the default cursor
            debuggingLens.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        else{
            redispatchMouseEvent(e, false);
        }
    }

    private void redispatchMouseEvent(MouseEvent e, boolean repaint) {
        // convert the location of the event to a position on the content pane
        Point glassPanePoint = e.getPoint();
        Container container = contentPane;
        Point containerPoint = SwingUtilities.convertPoint(debuggingLens, glassPanePoint, contentPane);
        // check if we're in the content pane
        if (containerPoint.y > 0) {
            // The mouse event is probably over the content pane, but we still need to find out exactly which component it's over.
            Component component = SwingUtilities.getDeepestComponentAt(container, containerPoint.x, containerPoint.y);
            if ((component != null) && (component.equals(liveButton))) {
                // clear lens state
                debuggingLens.setIsLocked(false);
                debuggingLens.setIsResizing(DebuggingLens.Resizing.FALSE);
                // forward events over to the check box
                Point componentPoint = SwingUtilities.convertPoint(debuggingLens, glassPanePoint, component);
                component.dispatchEvent(new MouseEvent(component, e.getID(), e.getWhen(), e.getModifiers(), componentPoint.x, componentPoint.y, e.getClickCount(), e.isPopupTrigger()));
            }
        }
        // update the glass pane if requested.
        if (repaint) {
            if(!debuggingLens.getIsLocked()){
                debuggingLens.setTopLeftPoint(glassPanePoint);
            }
            debuggingLens.repaint();
        }
    }
}

/**
 * Listen for all key press events. Lock/unlock the debugging
 * lens if the key pressed was upper or lower case L
 */
class LockListener extends KeyAdapter {
    private DebuggingLens dl;

    public LockListener(DebuggingLens dl){
        this.dl = dl;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char keyChar = e.getKeyChar();
        if(keyChar == 'l' || keyChar == 'L'){
            dl.setIsLocked(!dl.getIsLocked());
        }
    }
}
