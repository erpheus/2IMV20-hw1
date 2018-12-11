package volvis.raycasters;

import gui.TransferFunction2DEditor;
import util.Interpolation;
import util.VectorMath;
import volume.GradientVolume;
import volume.Volume;
import volume.VoxelGradient;
import volvis.TransferFunction;

import java.awt.image.BufferedImage;

public abstract class BaseRaycaster {
    protected BufferedImage image;
    protected Volume volume;
    protected GradientVolume gradients;
    TransferFunction tFunc;
    int displacement = 1;
    long lastRunningTime;
    boolean shadingEnabled = false;
    boolean frontToBackEnabled = false;
    double frontToBackCutoff = 0.99;

    protected static double TARGET_RENDERING_TIME = 1000.0 / 15; // 15 fps
    TransferFunction2DEditor tfEditor2D;


    // Common calculation variables
    double[] viewVec, uVec, vVec, volumeCenter;
    double volume_max;
    int image_half;

    public void setUp(Volume volume, BufferedImage image, TransferFunction tFunc, TransferFunction2DEditor tfEditor2D,
                      GradientVolume gradients, boolean shadingEnabled) {
        this.volume = volume;
        this.gradients = gradients;
        this.image = image;
        this.tFunc = tFunc;
        this.tfEditor2D = tfEditor2D;
        this.shadingEnabled = shadingEnabled;
    }

    void castSetUp(double[] viewMatrix) {
        // clear image
        clearImage();

        // vector uVec and vVec define a plane through the origin,
        // perpendicular to the view vector viewVec
        viewVec = new double[3];
        uVec = new double[3];
        vVec = new double[3];
        VectorMath.setVector(viewVec, viewMatrix[2], viewMatrix[6], viewMatrix[10]);
        VectorMath.setVector(uVec, viewMatrix[0], viewMatrix[4], viewMatrix[8]);
        VectorMath.setVector(vVec, viewMatrix[1], viewMatrix[5], viewMatrix[9]);

        // image is square
        image_half = image.getWidth() / 2;

        volumeCenter = new double[3];
        VectorMath.setVector(volumeCenter, volume.getDimX() / 2, volume.getDimY() / 2, volume.getDimZ() / 2);

        // sample on a plane through the origin of the volume data
        volume_max = volume.getMaximum();
    }

    private void calcDisplacement(boolean interactiveMode) {
        if (interactiveMode) {
            displacement = displacementForRunningTime(lastRunningTime, displacement);
        } else {
            displacement = 1;
        }
    }

    protected abstract int displacementForRunningTime(long lastRunningTime, int lastDisplacement);

    public double cast(double[] viewMatrix, boolean interactiveMode) {
        calcDisplacement(interactiveMode);

        long startTime = System.currentTimeMillis();

        castSetUp(viewMatrix);
        internal_cast(viewMatrix);

        long endTime = System.currentTimeMillis();
        lastRunningTime = (endTime - startTime);
        return (double) lastRunningTime;
    }

    public abstract void internal_cast(double[] viewMatrix);

    private void clearImage() {
        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                image.setRGB(i, j, 0);
            }
        }
    }

    protected void setPixelRegion(int i, int j, int pixelColor) {
        if (displacement == 1) {
            image.setRGB(i, j, pixelColor);
        } else {
            int centering = displacement/2;
            int min_i = Math.max(-centering + i, 0);
            int min_j = Math.max(-centering + j, 0);
            int max_i = Math.min(displacement - centering + i, image.getWidth());
            int max_j = Math.min(displacement - centering + j, image.getHeight());
            for (int real_i = min_i; real_i < max_i; real_i++) {
                for (int real_j = min_j; real_j < max_j; real_j++) {
                    image.setRGB(real_i, real_j, pixelColor);
                }
            }
        }
    }


    short getVoxel(double[] coord) {
        return getVoxel(
                (int) Math.floor(coord[0]),
                (int) Math.floor(coord[1]),
                (int) Math.floor(coord[2])
        );
    }

    short getVoxel(int x, int y, int z) {

        if (x < 0 || x >= volume.getDimX() || y < 0 || y >= volume.getDimY()
                || z < 0 || z >= volume.getDimZ()) {
            return 0;
        }

        return volume.getVoxel(x, y, z);
    }

    double interpVoxels(double[] coord) {
        // Find the eight surrounding points
        int x0 = (int) Math.floor(coord[0]);
        int x1 = (int) Math.ceil(coord[0]);
        int y0 = (int) Math.floor(coord[1]);
        int y1 = (int) Math.ceil(coord[1]);
        int z0 = (int) Math.floor(coord[2]);
        int z1 = (int) Math.ceil(coord[2]);

        // Interpolate
        return Interpolation.triLerp(
                coord[0], coord[1], coord[2],
                getVoxel(x0, y0, z0), getVoxel(x0, y0, z1), getVoxel(x0, y1, z0), getVoxel(x0, y1, z1),
                getVoxel(x1, y0, z0), getVoxel(x1, y0, z1), getVoxel(x1, y1, z0), getVoxel(x1, y1, z1),
                x0, x1, y0, y1, z0, z1
        );
    }

    double interpVoxelGradients(double[] coord, int dim) {
        // Find the eight surrounding points
        int x0 = (int) Math.floor(coord[0]);
        int x1 = (int) Math.ceil(coord[0]);
        int y0 = (int) Math.floor(coord[1]);
        int y1 = (int) Math.ceil(coord[1]);
        int z0 = (int) Math.floor(coord[2]);
        int z1 = (int) Math.ceil(coord[2]);

        // Interpolate
        return Interpolation.triLerp(
                coord[0], coord[1], coord[2],
                getVoxelGradient(x0, y0, z0, dim), getVoxelGradient(x0, y0, z1, dim), getVoxelGradient(x0, y1, z0, dim), getVoxelGradient(x0, y1, z1, dim),
                getVoxelGradient(x1, y0, z0, dim), getVoxelGradient(x1, y0, z1, dim), getVoxelGradient(x1, y1, z0, dim), getVoxelGradient(x1, y1, z1, dim),
                x0, x1, y0, y1, z0, z1
        );
    }

    double getVoxelGradient(int x, int y, int z, int dim) {
        if (x < 0 || x >= volume.getDimX() || y < 0 || y >= volume.getDimY()
                || z < 0 || z >= volume.getDimZ()) {
            return 0;
        }

        VoxelGradient vg = gradients.getGradient(x, y, z);
        switch (dim) {
            case 0:
                return vg.x;
            case 1:
                return vg.y;
            case 2:
                return vg.z;
            default:
                return 0;
        }
    }

    double calculateDistance(double[] pixelCoord, double[] uVec, double[] vVec) {
        double[] volumeCenter = new double[3];
        VectorMath.setVector(volumeCenter, volume.getDimX() / 2, volume.getDimY() / 2, volume.getDimZ() / 2);
        int imageCenter = image.getWidth() / 2;
        double[] imageCenterVec = new double[3];
        imageCenterVec[0] = uVec[0] * (imageCenter) + vVec[0] * (imageCenter);
        imageCenterVec[1] = uVec[1] * (imageCenter) + vVec[1] * (imageCenter);
        imageCenterVec[2] = uVec[2] * (imageCenter) + vVec[2] * (imageCenter);
        return 2*VectorMath.distance(imageCenterVec, volumeCenter);
    }

    public void setShadingEnabled(boolean shadingEnabled) {
        this.shadingEnabled = shadingEnabled;
    }

    public void setFrontToBackEnabled(boolean frontToBackEnabled) {
        this.frontToBackEnabled = frontToBackEnabled;
    }

    public void setFrontToBackCutoff(double frontToBackCutoff) {
        this.frontToBackCutoff = frontToBackCutoff;
    }
}
