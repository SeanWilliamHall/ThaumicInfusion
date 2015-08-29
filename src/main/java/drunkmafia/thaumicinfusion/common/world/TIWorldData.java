/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.world;

import drunkmafia.thaumicinfusion.common.util.helper.ReflectionLookup;
import drunkmafia.thaumicinfusion.common.util.quadtree.QuadTree;
import drunkmafia.thaumicinfusion.common.world.data.BlockSavable;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.BlockSyncPacketC;
import drunkmafia.thaumicinfusion.net.packet.server.DataRemovePacketC;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import thaumcraft.api.WorldCoordinates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TIWorldData implements ISavable {

    private static ReflectionLookup<World> worldLookup = new ReflectionLookup<World>(World.class);

    public World world;
    public QuadTree<ChunkData> chunkDatas = new QuadTree<ChunkData>(ChunkData.class, -2000000, -2000000, 2000000, 2000000);

    public static TIWorldData getWorldData(World world) {
        if (world == null || !(world instanceof IWorldDataProvider))
            return null;

        IWorldDataProvider dataProvider = (IWorldDataProvider) world;
        TIWorldData worldData = dataProvider.getWorldData();

        if(!world.isRemote) world = DimensionManager.getWorld(world.provider.dimensionId);
        if (worldData == null) dataProvider.setWorldData(worldData = new TIWorldData());

        worldData.world = world;
        return worldData;
    }

    public static World getWorld(IBlockAccess blockAccess) {
        if (worldLookup == null) worldLookup = new ReflectionLookup<World>(World.class);
        return blockAccess != null ? blockAccess instanceof World ? (World) blockAccess : worldLookup.getObjectFrom(blockAccess) : null;
    }

    /**
     * Adds block to world data
     *
     * @param block  to be saved to the world data
     * @param init   if true will initialize the data
     * @param packet will sync to the client if true
     */
    public void addBlock(BlockSavable block, boolean init, boolean packet){
        if (block == null)
            return;

        if (world == null)
            world = DimensionManager.getWorld(block.getCoords().dim);

        if (init && !block.isInit())
            block.dataLoad(world);

        WorldCoordinates coordinates = block.getCoords();

        ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(coordinates.x >> 4, coordinates.z >> 4);
        ChunkData chunkData = chunkDatas.get(chunkPos.getCenterXPos(), chunkPos.getCenterZPosition(), null);
        if (chunkData == null) {
            chunkData = new ChunkData(chunkPos);
            chunkDatas.set(chunkPos.getCenterXPos(), chunkPos.getCenterZPosition(), chunkData);
        }
        chunkData.addBlock(block, coordinates.x, coordinates.y, coordinates.z);

        if (!world.isRemote && packet)
            ChannelHandler.instance().sendToDimension(new BlockSyncPacketC(block, chunkDatas.getCount()), world.provider.dimensionId);
    }

    public List<ChunkData> getChunksInRange(int xMin, int zMin, int xMax, int zMax) {
        return chunkDatas.searchWithinObject(xMin, zMin, xMax, zMax);
    }

    public void addBlock(BlockSavable block){
        addBlock(block, false, false);
    }

    public void postLoad(){
        for(BlockSavable savable : getAllStoredData()) {
            if(savable == null) continue;

            if (world == null) world = DimensionManager.getWorld(savable.getCoords().dim);
            else savable.getCoords().dim = world.provider.dimensionId;

            if (!savable.isInit())  savable.dataLoad(world);
        }
    }

    public <T> T getBlock(Class<T> type, WorldCoordinates coords) {
        ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(coords.x >> 4, coords.z >> 4);
        ChunkData chunkData = chunkDatas.get(chunkPos.getCenterXPos(), chunkPos.getCenterZPosition(), null);
        return chunkData != null ? chunkData.getBlock(type, coords.x, coords.y, coords.z) : null;
    }

    public void removeData(Class<? extends BlockSavable> type, WorldCoordinates coords, boolean sendPacket) {
        ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(coords.x >> 4, coords.z >> 4);
        ChunkData chunkData = chunkDatas.get(chunkPos.getCenterXPos(), chunkPos.getCenterZPosition(), null);
        if(chunkData != null) {
            chunkData.removeData(type, coords.x, coords.y, coords.z);
            if (sendPacket) {
                coords.dim = world.provider.dimensionId;
                ChannelHandler.instance().sendToAll(new DataRemovePacketC(type, coords));
            }
        }
    }

    public BlockSavable[] getAllStoredData() {
        ArrayList<BlockSavable> savables = new ArrayList<BlockSavable>();
        for(ChunkData chunks : chunkDatas.getValues())
            Collections.addAll(savables, chunks.getAllBlocks());
        return savables.size() != 0 ? savables.toArray(new BlockSavable[1]) : new BlockSavable[0];
    }

    @Override
    public void readNBT(NBTTagCompound tag) {
        int size = tag.getInteger("Chunks");

        for(int i = 0; i < size; i++){
            if(!tag.hasKey("Chunk:" + i))
                continue;

            ChunkData chunkData = SavableHelper.loadDataFromNBT(tag.getCompoundTag("Chunk:" + i));
            if(chunkData != null){
                for (BlockSavable data : chunkData.getAllBlocks())
                    addBlock(data);
            }
        }
    }

    @Override
    public void writeNBT(NBTTagCompound tag) {
        ChunkData[] chunks = chunkDatas.getValues();
        tag.setInteger("Chunks", chunks.length);

        for (int i = 0; i < chunks.length; i++) {
            ChunkData chunkData = chunks[i];
            if (chunkData == null) continue;
            tag.setTag("Chunk:" + i, SavableHelper.saveDataToNBT(chunkData));
        }
    }
}