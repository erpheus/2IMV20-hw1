package volvis.raycasters;

import util.VectorMath;
import volvis.TFColor;

public class MIPRaycaster extends BaseRaycaster {

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
        TFColor voxelColor = new TFColor();

        for (int j = displacement/2; j < image.getHeight(); j+= displacement) {
            for (int i = displacement/2; i < image.getWidth(); i+= displacement) {
                double k = displacement/2 + (-1 * (maximumSteps / 2));
                double max_k = maximumSteps/2 - displacement/2;
                double maximumIntensity = -1;

                while(k <= max_k) {//Math.max(volume.getDimX(), Math.max(volume.getDimY(), volume.getDimZ()))) {
                    pixelCoord[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter) + volumeCenter[0] + k*viewVec[0];
                    pixelCoord[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter) + volumeCenter[1] + k*viewVec[1];
                    pixelCoord[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter) + volumeCenter[2] + k*viewVec[2];
                    k+= displacement;
                    double val = interpVoxels(pixelCoord);
                    if (val > maximumIntensity) {
                        maximumIntensity = val;
                    }
                }

                double val = maximumIntensity;

                // Map the intensity to a grey value by linear scaling
                voxelColor.r = val/volume_max;
                voxelColor.g = voxelColor.r;
                voxelColor.b = voxelColor.r;
                voxelColor.a = val > 0 ? 1.0 : 0.0;  // this makes intensity 0 completely transparent and the rest opaque

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
