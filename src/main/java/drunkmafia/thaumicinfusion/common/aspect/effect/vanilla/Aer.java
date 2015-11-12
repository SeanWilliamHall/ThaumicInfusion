/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.aspect.effect.vanilla;

import drunkmafia.thaumicinfusion.common.aspect.AspectEffect;
import drunkmafia.thaumicinfusion.common.util.annotation.Effect;
import drunkmafia.thaumicinfusion.common.util.annotation.OverrideBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import thaumcraft.api.WorldCoordinates;

import java.util.List;
import java.util.Random;

@Effect(aspect = "aer")
public class Aer extends AspectEffect {

    static long maxCooldown = 2000L;
    long cooldown;

    @Override
    public int getCost() {
        return 2;
    }

    @Override
    public void aspectInit(World world, WorldCoordinates pos) {
        super.aspectInit(world, pos);
        if (!world.isRemote)
            this.updateTick(world, pos.x, pos.y, pos.z, world.rand);
    }

    @OverrideBlock(overrideBlockFunc = false)
    public void updateTick(World world, int x, int y, int z, Random random) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
        if (world.isRemote)
            return;

        if (this.cooldown + Aer.maxCooldown < System.currentTimeMillis()) {
            AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox(this.pos.x, this.pos.y, this.pos.z, this.pos.x + 1, this.pos.y + 1, this.pos.z + 1).expand(1, 1, 1);
            List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, axisalignedbb);

            for (EntityPlayer player : players)
                player.addPotionEffect(new PotionEffect(Potion.jump.getId(), 100));

            this.cooldown = System.currentTimeMillis();
        }


    }

    @OverrideBlock(overrideBlockFunc = false)
    public void onBlockAdded(World world, int x, int y, int z) {
        world.scheduleBlockUpdate(x, y, z, world.getBlock(x, y, z), 1);
    }
}
