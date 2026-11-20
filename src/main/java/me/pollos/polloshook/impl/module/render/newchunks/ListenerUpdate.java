package me.pollos.polloshook.impl.module.render.newchunks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.pollos.polloshook.api.event.listener.ModuleListener;
import me.pollos.polloshook.impl.events.update.UpdateEvent;
import me.pollos.polloshook.impl.module.render.newchunks.util.ChunkData;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

public class ListenerUpdate extends ModuleListener<NewChunks, UpdateEvent> {
   public ListenerUpdate(NewChunks module) {
      super(module, UpdateEvent.class);
   }

   public void call(UpdateEvent event) {
      ChunkPos playerChunk = mc.player.getChunkPos();
      String dimension = mc.world.getRegistryKey().getValue().getPath();
      for (WorldChunk chunk : this.loopChunks(playerChunk.x, playerChunk.z)) {
         List<Block> blocks = new ArrayList<>();
         Arrays.stream(chunk.getSectionArray()).forEach(section ->
            section.getBlockStateContainer().forEachValue(state -> blocks.add(state.getBlock()))
         );
         if (dimension.equals("overworld") && !blocks.contains(Blocks.COPPER_ORE)) {
            continue;
         }
         if (dimension.equals("the_nether") && !blocks.contains(Blocks.NETHER_GOLD_ORE)) {
            continue;
         }

         ChunkData data = new ChunkData(chunk.getPos().x, chunk.getPos().z);
         if (!this.module.chunkDataList.contains(data)) {
            this.module.chunkDataList.add(data);
         }
      }
   }

   private List<WorldChunk> loopChunks(int posX, int posZ) {
      List<WorldChunk> chunks = new ArrayList();

      for (int x = -17; x <= 17; ++x) {
         for (int z = -17; z <= 17; ++z) {
            WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(posX + x, posZ + z);
            if (chunk != null && !((NewChunks) this.module).loadedChunks.contains(chunk)) {
               ((NewChunks) this.module).loadedChunks.add(chunk);
               chunks.add(chunk);
            }
         }
      }

      return chunks;
   }
}
