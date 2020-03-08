package com.company;

import javax.swing.JFrame;
import java.awt.Dimension;

class Window {
    static JFrame frame;
    Window(int w, int h, Main game) {
        frame = new JFrame("Top Down");
        game.setFocusable(true);
        frame.add(game);
        frame.setPreferredSize(new Dimension(w,h));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
