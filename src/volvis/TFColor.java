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

    public void composite(TFColor newColor) {

        r = newColor.a * newColor.r + ((1-newColor.a) * r);
        b = newColor.a * newColor.b + ((1-newColor.a) * b);
        g = newColor.a * newColor.g + ((1-newColor.a) * g);

    }
}
