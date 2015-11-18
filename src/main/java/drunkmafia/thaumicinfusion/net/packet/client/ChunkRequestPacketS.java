/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.net.packet.client;

import drunkmafia.thaumicinfusion.common.world.ChunkData;
import drunkmafia.thaumicinfusion.common.world.TIWorldData;
import drunkmafia.thaumicinfusion.net.ChannelHandler;
import drunkmafia.thaumicinfusion.net.packet.server.ChunkSyncPacketC;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ChunkRequestPacketS implements IMessage {

    private ChunkCoordIntPair pos;
    private int dim;

    public ChunkRequestPacketS() {
    }

    public ChunkRequestPacketS(ChunkCoordIntPair pos, int dim) {
        this.pos = pos;
        this.dim = dim;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = new ChunkCoordIntPair(buf.readInt(), buf.readInt());
        this.dim = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.pos.getCenterXPos() >> 4);
        buf.writeInt(this.pos.getCenterZPosition() >> 4);

        buf.writeInt(this.dim);
    }

    public static class Handler implements IMessageHandler<ChunkRequestPacketS, IMessage> {
        @Override
        public IMessage onMessage(ChunkRequestPacketS message, MessageContext ctx) {
            ChunkCoordIntPair pos = message.pos;
            if (pos == null || ctx.side.isClient())
                return null;

            TIWorldData worldData = TIWorldData.getWorldData(ChannelHandler.getServerWorld(message.dim));
            if (worldData == null)
                return null;

            ChunkData data = worldData.chunkDatas.get(pos.getCenterXPos(), pos.getCenterZPosition(), null);
            if (data != null)
                return new ChunkSyncPacketC(data);

            return null;
        }
    }
}
