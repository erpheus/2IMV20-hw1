/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package volvis;

import com.jogamp.opengl.GL2;
import java.util.ArrayList;

import util.Debouncer;
import util.TFChangeListener;

import javax.swing.*;

/**
 *
 * @author michel
 */
public abstract class Renderer {
    int winWidth, winHeight;
    boolean visible = false;
    boolean interactiveMode = false;
    ArrayList<TFChangeListener> listeners = new ArrayList<TFChangeListener>();
    static int counter = 0;
    protected Debouncer debouncer = new Debouncer(() -> {
        counter++;
        SwingUtilities.invokeLater(() -> {
            interactiveMode = false;
            for (TFChangeListener listener : listeners) {
                listener.changed();
            }
        });
    }, 5000);

    public Renderer() {
        
    }

    public void setInteractiveMode(boolean interactive) {
        if (!interactive) {
            debouncer.call();
        } else {
            interactiveMode = interactive;
            debouncer.cancel();
        }
    }

    public void setInteractiveMode(boolean interactive, int timeout) {
        if (!interactive) {
            debouncer.call(timeout);
        } else {
            interactiveMode = interactive;
            debouncer.cancel();
        }
    }
    
    public void setWinWidth(int w) {
        winWidth = w;
    }

    public void setWinHeight(int h) {
        winHeight = h;
    }

    public int getWinWidth() {
        return winWidth;
    }

    public int getWinHeight() {
        return winHeight;
    }

    public void setVisible(boolean flag) {
        visible = flag;
    }

    public boolean getVisible() {
        return visible;
    }

    public void addTFChangeListener(TFChangeListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }
    
    public abstract void visualize(GL2 gl);
}
