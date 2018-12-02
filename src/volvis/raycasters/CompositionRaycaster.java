package volvis.raycasters;

import util.VectorMath;
import volvis.TFColor;

public class CompositionRaycaster extends BaseRaycaster{

    @Override
    protected int displacementForRunningTime(long lastRunningTime, int lastDisplacement) {
        // we use the cube root to update the displacement as it has a cubic relationship with the rendering time
        // because it is used inside three nested loops during rendering.
        return (int) Math.ceil(lastDisplacement * Math.cbrt((double)lastRunningTime / TARGET_RENDERING_TIME));
    }

    @Override
    public void internal_cast(double[] viewMatrix) {
        double[] pixelCoord = new double[3];
        double distanceThreshold = calculateDistance(pixelCoord, uVec, vVec);
        double viewVecSize = VectorMath.length(viewVec);
        double maximumSteps = distanceThreshold/viewVecSize;
        int imageCenter = image.getWidth() / 2;
        TFColor voxelColor;
        TFColor currentVoxelColor;
        double max_k = +1 * (maximumSteps / 2) - displacement/2 ;
        double min_k = displacement/2 + (-1 * (maximumSteps / 2));

        for (int j = displacement/2; j < image.getHeight(); j+= displacement) {
            for (int i = displacement/2; i < image.getWidth(); i+= displacement) {
                voxelColor = new TFColor(0,0,0,1);
                for(double k = max_k; k >= min_k; k-= displacement) {
                    pixelCoord[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter) + volumeCenter[0] - k*viewVec[0];
                    pixelCoord[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter) + volumeCenter[1] - k*viewVec[1];
                    pixelCoord[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter) + volumeCenter[2] - k*viewVec[2];
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
                setPixelRegion(i, j, pixelColor);

            }
        }
    }
}
