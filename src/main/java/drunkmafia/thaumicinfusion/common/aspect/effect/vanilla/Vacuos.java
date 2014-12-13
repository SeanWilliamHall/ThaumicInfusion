package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.aspect.tileentity.VacuosTile;
import drunkmafia.thaumicinfusion.common.util.BlockHelper;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.utils.InventoryUtils;

/**
 * Created by DrunkMafia on 06/11/2014.
 * See http://www.wtfpl.net/txt/copying for licence
 */
@Effect(aspect = ("vacuos"), cost = 4, hasTileEntity = true)
public class Vacuos extends AspectEffect {

    @Override
    public TileEntity getTile(){
        return new VacuosTile();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote)
            return true;

        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity == null || !(tileEntity instanceof IInventory))
            return false;

        player.displayGUIChest((IInventory) tileEntity);
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if(!world.isRemote)
            InventoryUtils.dropItems(world, x, y, z);
    }
}
