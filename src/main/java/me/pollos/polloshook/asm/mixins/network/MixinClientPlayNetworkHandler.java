package me.pollos.polloshook.asm.mixins.network;

import java.util.Iterator;
import me.pollos.polloshook.PollosHook;
import me.pollos.polloshook.api.managers.Managers;
import me.pollos.polloshook.api.module.Module;
import me.pollos.polloshook.api.module.ToggleableModule;
import me.pollos.polloshook.asm.ducks.util.IClientPlayerNetworkManager;
import me.pollos.polloshook.impl.events.chat.SendChatMessageEvent;
import me.pollos.polloshook.impl.events.network.GameJoinEvent;
import me.pollos.polloshook.impl.module.other.fastlatency.FastLatency;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageSignatureStorage;
import net.minecraft.network.message.MessageChain.Packer;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayNetworkHandler.class})
public abstract class MixinClientPlayNetworkHandler extends ClientCommonNetworkHandler implements IClientPlayerNetworkManager {
   @Shadow
   private LastSeenMessagesCollector lastSeenMessagesCollector;
   @Shadow
   private Packer messagePacker;
   @Shadow
   private MessageSignatureStorage signatureStorage;

   protected MixinClientPlayNetworkHandler(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
      super(client, connection, connectionState);
   }

   @Inject(
      method = {"onGameJoin"},
      at = {@At("TAIL")}
   )
   private void onGameJoinHook(GameJoinS2CPacket packet, CallbackInfo info) {
      GameJoinEvent gameJoinEvent = new GameJoinEvent(this.client.getNetworkHandler().getServerInfo());
      PollosHook.getEventBus().dispatch(gameJoinEvent);
      Iterator var4 = Managers.getModuleManager().getModules().iterator();

      while(true) {
         Module module;
         ToggleableModule toggleableModule;
         do {
            if (!var4.hasNext()) {
               return;
            }

            module = (Module)var4.next();
            if (!(module instanceof ToggleableModule)) {
               break;
            }

            toggleableModule = (ToggleableModule)module;
         } while(!toggleableModule.isEnabled());

         module.onGameJoin();
      }
   }

   @Inject(
      method = {"onPingResult"},
      at = {@At("TAIL")}
   )
   private void onPingResultHook(PingResultS2CPacket packet, CallbackInfo ci) {
      FastLatency FAST_LATENCY = (FastLatency)Managers.getModuleManager().get(FastLatency.class);
      if (FAST_LATENCY.isEnabled() && FAST_LATENCY.getMeasurer() != null) {
         FAST_LATENCY.getMeasurer().onPingResult(packet);
      }

   }

   @ModifyVariable(method = "sendChatMessage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private String modifyChatMessage(String content) {
      if (content.startsWith(Managers.getCommandManager().getPrefix())) {
         return content;
      }
      SendChatMessageEvent event = new SendChatMessageEvent(content);
      PollosHook.getEventBus().dispatch(event);
      return event.isCanceled() ? event.getMessage() : content;
   }

   public Packer getMessageChainPacker() {
      return this.messagePacker;
   }

   public LastSeenMessagesCollector getLastSeenMessageCollector() {
      return this.lastSeenMessagesCollector;
   }
}
