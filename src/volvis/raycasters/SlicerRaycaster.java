package volvis.raycasters;

import volvis.TFColor;

public class SlicerRaycaster extends BaseRaycaster {

    @Override
    protected int displacementForRunningTime(long lastRunningTime, int lastDisplacement) {
        return 1;
    }

    @Override
    public void internal_cast(double[] viewMatrix) {
        double[] pixelCoord = new double[3];
        TFColor voxelColor = new TFColor();

        for (int j = displacement/2; j < image.getHeight(); j+= displacement) {
            for (int i = displacement/2; i < image.getWidth(); i+= displacement) {
                pixelCoord[0] = uVec[0] * (i - image_half) + vVec[0] * (j - image_half)
                        + volumeCenter[0];
                pixelCoord[1] = uVec[1] * (i - image_half) + vVec[1] * (j - image_half)
                        + volumeCenter[1];
                pixelCoord[2] = uVec[2] * (i - image_half) + vVec[2] * (j - image_half)
                        + volumeCenter[2];

                int val = getVoxel(pixelCoord);

                // Map the intensity to a grey value by linear scaling
                voxelColor.r = val/volume_max;
                voxelColor.g = voxelColor.r;
                voxelColor.b = voxelColor.r;
                voxelColor.a = val > 0 ? 1.0 : 0.0;  // this makes intensity 0 completely transparent and the rest opaque
                // Alternatively, apply the transfer function to obtain a color
                // voxelColor = tFunc.getColor(val);

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
