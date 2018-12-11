package volvis.raycasters;

import gui.TransferFunction2DEditor;
import util.VectorMath;
import volume.Volume;
import volume.VoxelGradient;
import volvis.TFColor;
import volvis.TransferFunction;

import java.awt.image.BufferedImage;

import static util.VectorMath.inverseVector;
import static util.VectorMath.normalizedVector;

public class TransferFunction2DRayCaster extends BaseRaycaster {

    @Override
    protected int displacementForRunningTime(long lastRunningTime, int lastDisplacement) {
        // we use the cube root to update the displacement as it has a cubic relationship with the rendering time
        // because it is used inside three nested loops during rendering.
        return (int) Math.ceil(lastDisplacement * Math.cbrt((double)lastRunningTime / TARGET_RENDERING_TIME));
    }

    private double alphaForGradientAndIntensity(double val, double grad) {
        double fv = tfEditor2D.triangleWidget.baseIntensity;
        double r = tfEditor2D.triangleWidget.radius;
        double res;
        if ( val ==  fv) {
            res = 1;
        } else if (grad > 0 && fv >= val - r*grad && fv <= val + r*grad) {
            res = 1 - (1/r) * Math.abs((fv - val)/grad);
        } else {
            res = 0;
        }

        return tfEditor2D.triangleWidget.color.a * res;
    }

    @Override
    public void internal_cast(double[] viewMatrix) {
        double factor = 1.0;
        if (frontToBackEnabled) {
            factor = -1.0;
        }

        double Ka = 0.1, Kd = 0.7, Ks = 0.2, alpha = 10;

        double[] pixelCoord = new double[3];
        double distanceThreshold = calculateDistance(pixelCoord, uVec, vVec);
        double viewVecSize = VectorMath.length(viewVec);
        double maximumSteps = distanceThreshold/viewVecSize;
        int imageCenter = image.getWidth() / 2;
        TFColor voxelColor;
        TFColor compositingColor = new TFColor();
        double[] lVector = normalizedVector(inverseVector(viewVec));
        VoxelGradient gradient = new VoxelGradient();
        double max_k = +1 * (maximumSteps / 2) - displacement/2 ;
        double min_k = displacement/2 + (-1 * (maximumSteps / 2));

        for (int j = displacement/2; j < image.getHeight(); j+= displacement) {
            for (int i = displacement/2; i < image.getWidth(); i+= displacement) {

                voxelColor = new TFColor(0,0,0,1);
                double remaining = 1;

                for(double k = max_k; k >= min_k; k-= displacement) {
                    pixelCoord[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter) + volumeCenter[0] - factor*k*viewVec[0];
                    pixelCoord[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter) + volumeCenter[1] - factor*k*viewVec[1];
                    pixelCoord[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter) + volumeCenter[2] - factor*k*viewVec[2];
                    double val = interpVoxels(pixelCoord);

                    // Copy because we are going to modify it
                    compositingColor.copy(tfEditor2D.triangleWidget.color);
                    gradient.x = (float)interpVoxelGradients(pixelCoord, 0);
                    gradient.y = (float)interpVoxelGradients(pixelCoord, 1);
                    gradient.z = (float)interpVoxelGradients(pixelCoord, 2);
                    double mag = gradient.calc_mag();
                    compositingColor.a = alphaForGradientAndIntensity(val, mag);

                    if (shadingEnabled) {
                        if ( mag > 0 ) {
                            double ln = gradient.orientationNormDotProduct(lVector);
                            compositingColor.r = Ka + compositingColor.r * Kd * ln + Ks * Math.pow(ln, alpha);
                            compositingColor.g = Ka + compositingColor.g * Kd * ln + Ks * Math.pow(ln, alpha);
                            compositingColor.b = Ka + compositingColor.b * Kd * ln + Ks * Math.pow(ln, alpha);
                        }
                    }

                    if (frontToBackEnabled) {
                        remaining = voxelColor.compositeAdd(compositingColor, compositingColor.a, remaining);
                        if (remaining < 1 - frontToBackCutoff) {
                            break;
                        }
                    } else {
                        voxelColor.composite(compositingColor);
                    }
                }

                // BufferedImage expects a pixel color packed as ARGB in an int
                int c_alpha = voxelColor.a <= 1.0 ? (int) Math.floor(voxelColor.a * 255) : 255;
                int c_red = voxelColor.r <= 1.0 ? (int) Math.floor(voxelColor.r * 255) : 255;
                int c_green = voxelColor.g <= 1.0 ? (int) Math.floor(voxelColor.g * 255) : 255;
                int c_blue = voxelColor.b <= 1.0 ? (int) Math.floor(voxelColor.b * 255) : 255;
                int pixelColor = (c_alpha << 24) | (c_red << 16) | (c_green << 8) | c_blue;
                setPixelRegion(i, j, pixelColor);

            }
        }
    }

}
