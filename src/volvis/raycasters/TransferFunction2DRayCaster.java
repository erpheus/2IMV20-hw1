package volvis.raycasters;

import gui.TransferFunction2DEditor;
import util.VectorMath;
import volume.Volume;
import volvis.TFColor;
import volvis.TransferFunction;

import java.awt.image.BufferedImage;

public class TransferFunction2DRayCaster extends BaseRaycaster {

    @Override
    protected int displacementForRunningTime(long lastRunningTime, int lastDisplacement) {
        // we use the cube root to update the displacement as it has a cubic relationship with the rendering time
        // because it is used inside three nested loops during rendering.
        return (int) Math.ceil(lastDisplacement * Math.cbrt((double)lastRunningTime / TARGET_RENDERING_TIME));
    }

    @Override
    public void internal_cast(double[] viewMatrix) {
        castSetUp(viewMatrix);

        double[] pixelCoord = new double[3];
        double distanceThreshold = calculateDistance(pixelCoord, uVec, vVec);
        double viewVecSize = VectorMath.length(viewVec);
        double maximumSteps = distanceThreshold/viewVecSize;
        int imageCenter = image.getWidth() / 2;
        TFColor voxelColor;
        TFColor currentVoxelColor;

        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                double k = +1 * (maximumSteps / 2) - displacement/2 ;
                double min_k = displacement/2 + (-1 * (maximumSteps / 2));

                voxelColor = new TFColor(0,0,0,1);
                while(k >= min_k) {//Math.max(volume.getDimX(), Math.max(volume.getDimY(), volume.getDimZ()))) {
                    pixelCoord[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter) + volumeCenter[0] + k*viewVec[0];
                    pixelCoord[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter) + volumeCenter[1] + k*viewVec[1];
                    pixelCoord[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter) + volumeCenter[2] + k*viewVec[2];
                    k-= displacement;
                    double val = interpVoxels(pixelCoord);
                    currentVoxelColor = tFunc.getColor((int)(val));
                    voxelColor.composite(currentVoxelColor);
                }

                // BufferedImage expects a pixel color packed as ARGB in an int
                int c_alpha = voxelColor.a <= 1.0 ? (int) Math.floor(voxelColor.a * 255) : 255;
                int c_red = voxelColor.r <= 1.0 ? (int) Math.floor(voxelColor.r * 255) : 255;
                int c_green = voxelColor.g <= 1.0 ? (int) Math.floor(voxelColor.g * 255) : 255;
                int c_blue = voxelColor.b <= 1.0 ? (int) Math.floor(voxelColor.b * 255) : 255;
                int pixelColor = (c_alpha << 24) | (c_red << 16) | (c_green << 8) | c_blue;
                image.setRGB(i, j, pixelColor);

            }
        }
    }

}
