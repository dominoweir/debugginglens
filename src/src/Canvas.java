package src;

import javax.swing.*;

public class Canvas extends JFrame{

    public Canvas(){
        DebuggingLens dl = new DebuggingLens();
        JPanel main = new JPanel();

        main.add(dl);
        this.add(main);
    }

    public static void main(String[] args){
        Canvas canvas = new Canvas();
        canvas.pack();
        canvas.setVisible(true);
    }
}
