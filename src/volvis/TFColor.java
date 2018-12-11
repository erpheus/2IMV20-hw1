/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package volvis;

/**
 *
 * @author michel
 */
public class TFColor {
    public double r, g, b, a;

    public TFColor() {
        r = g = b = a = 1.0;
    }
    
    public TFColor(double red, double green, double blue, double alpha) {
        r = red;
        g = green;
        b = blue;
        a = alpha;
    }
    
    @Override
    public String toString() {
        String text = "(" + r + ", " + g + ", " + b + ", " + a + ")";
        return text;
    }

    public void copy(TFColor copiedColor) {
        r = copiedColor.r;
        g = copiedColor.g;
        b = copiedColor.b;
        a = copiedColor.a;
    }

    public void composite(TFColor newColor, double alpha) {
        r = alpha * newColor.r + ((1-alpha) * r);
        b = alpha * newColor.b + ((1-alpha) * b);
        g = alpha * newColor.g + ((1-alpha) * g);
    }

    public void composite(TFColor newColor) {
        composite(newColor, newColor.a);
    }

    public double compositeAdd(TFColor newColor, double alpha, double remaining) {
        r += remaining * alpha * newColor.r;
        b += remaining * alpha * newColor.b;
        g += remaining * alpha * newColor.g;
        return remaining - (remaining * alpha);
    }

}
