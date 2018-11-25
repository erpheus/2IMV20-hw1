/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package volume;

/**
 *
 * @author michel
 */
public class GradientVolume {

    private int dimX, dimY, dimZ;
    private VoxelGradient zero = new VoxelGradient();
    VoxelGradient[] data;
    Volume volume;
    double maxmag;

    public GradientVolume(Volume vol) {
        volume = vol;
        dimX = vol.getDimX();
        dimY = vol.getDimY();
        dimZ = vol.getDimZ();
        data = new VoxelGradient[dimX * dimY * dimZ];
        compute();
        maxmag = -1.0;
    }

    public VoxelGradient getGradient(int x, int y, int z) {
        return data[x + dimX * (y + dimY * z)];
    }

    
    public void setGradient(int x, int y, int z, VoxelGradient value) {
        data[x + dimX * (y + dimY * z)] = value;
    }

    public void setVoxel(int i, VoxelGradient value) {
        data[i] = value;
    }

    public VoxelGradient getVoxel(int i) {
        return data[i];
    }

    public int getDimX() {
        return dimX;
    }

    public int getDimY() {
        return dimY;
    }

    public int getDimZ() {
        return dimZ;
    }

    private void compute() {

        // this just initializes all gradients to the vector (0,0,0)
        for (int i=0; i<data.length; i++) {
            data[i] = zero;
        }

        for (int x=0;x<dimX; x++) {
            for (int y=0; y<dimY; y++) {
                for (int z=0; z<dimZ; z++) {
                    double xMinus = 0;
                    double xPlus = 0;
                    double yMinus = 0;
                    double yPlus = 0;
                    double zMinus = 0;
                    double zPlus = 0;

                    if (x > 0) {
                        xMinus = volume.getVoxel(x-1, y, z);
                    }

                    if (x < dimX - 1) {
                        xPlus = volume.getVoxel(x+1, y, z);
                    }

                    if (y > 0) {
                        yMinus = volume.getVoxel(x, y-1, z);
                    }

                    if (y < dimY - 1) {
                        yPlus = volume.getVoxel(x, y+1, z);
                    }

                    if (z > 0) {
                        zMinus = volume.getVoxel(x, y, z-1);
                    }

                    if (z < dimZ - 1) {
                        zPlus = volume.getVoxel(x, y, z+1);
                    }

                    int xGradient = (int) (0.5 *(xPlus-xMinus));
                    int yGradient = (int) (0.5 *(yPlus-yMinus));
                    int zGradient = (int) (0.5 *(zPlus-zMinus));

                    VoxelGradient gradient =  new VoxelGradient(xGradient, yGradient, zGradient);
                    data[x + dimX * (y + dimY * z)] = gradient;

                }
            }
        }
                
    }

    
    public double getMaxGradientMagnitude() {
        if (maxmag >= 0) {
            return maxmag;
        } else {
            double magnitude = data[0].mag;
            for (int i=0; i<data.length; i++) {
                magnitude = data[i].mag > magnitude ? data[i].mag : magnitude;
            }   
            maxmag = magnitude;
            return magnitude;
        }
    }
    

}
