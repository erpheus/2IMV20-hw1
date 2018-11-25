package volvis.raycasters;

import gui.TransferFunction2DEditor;
import util.Interpolation;
import util.VectorMath;
import volume.Volume;
import volvis.TransferFunction;

import java.awt.image.BufferedImage;

public class BaseRaycaster {
    protected BufferedImage image;
    protected Volume volume;
    TransferFunction tFunc;
    TransferFunction2DEditor tfEditor2D;


    // Common calculation variables
    double[] viewVec, uVec, vVec, volumeCenter;
    double volume_max;
    int image_half;

    public void setUp(Volume new_volume, BufferedImage new_image, TransferFunction new_tFunc, TransferFunction2DEditor tfEditor2D) {
        volume = new_volume;
        image = new_image;
        tFunc = new_tFunc;
        this.tfEditor2D = tfEditor2D;
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

    public void cast(double[] viewMatrix) {
        throw new Error("not implemented");
    }

    protected void clearImage() {
        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                image.setRGB(i, j, 0);
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
}
