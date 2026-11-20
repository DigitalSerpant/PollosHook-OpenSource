package me.pollos.polloshook.impl.module.render.fullbright;

import me.pollos.polloshook.api.event.listener.SafeModuleListener;
import me.pollos.polloshook.asm.ducks.util.ISimpleOption;
import me.pollos.polloshook.impl.events.update.TickEvent;
import me.pollos.polloshook.impl.module.render.fullbright.mode.FullbrightMode;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class ListenerTick extends SafeModuleListener<Fullbright, TickEvent> {
   public ListenerTick(Fullbright module) {
      super(module, TickEvent.class);
   }

   public void safeCall(TickEvent event) {
      FullbrightMode mode = this.module.mode.getValue();
      if (mode != FullbrightMode.NIGHT_VISION && mc.player.getStatusEffect(StatusEffects.NIGHT_VISION) != null) {
         mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
      }

      switch (mode) {
         case NIGHT_VISION -> mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1));
         case GAMMA -> setGamma();
      }
   }

   @SuppressWarnings("unchecked")
   private void setGamma() {
      ISimpleOption<Double> gamma = (ISimpleOption<Double>)(Object)mc.options.getGamma();
      gamma.polloshook$setValue(1000.0D);
   }
}
