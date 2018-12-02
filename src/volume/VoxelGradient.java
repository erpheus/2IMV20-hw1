/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package volume;

/**
 *
 * @author michel
 */
public class VoxelGradient {

    public float x, y, z;
    public float mag;
    
    public VoxelGradient() {
        x = y = z = mag = 0.0f;
    }
    
    public VoxelGradient(float gx, float gy, float gz) {
        x = gx;
        y = gy;
        z = gz;
        mag = (float) Math.sqrt(x*x + y*y + z*z);
    }

    public double orientationNormDotProduct(double[] otherVector) {
        if (mag == 0) {
            return 0;
        }
        return Math.max(
                otherVector[0] * x/mag + otherVector[1] * y/mag + otherVector[2] * z/mag,
                0 //otherVector[0] * (-x/mag) + otherVector[1] * (-y/mag) + otherVector[2] * (-z/mag)
        );
    }
    
}
