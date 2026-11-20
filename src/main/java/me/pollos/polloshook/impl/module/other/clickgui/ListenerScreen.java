package me.pollos.polloshook.impl.module.other.clickgui;

import me.pollos.polloshook.api.event.listener.ModuleListener;
import me.pollos.polloshook.impl.events.gui.ScreenEvent;

public class ListenerScreen extends ModuleListener<ClickGUI, ScreenEvent> {
   public ListenerScreen(ClickGUI module) {
      super(module, ScreenEvent.class);
   }

   public void call(ScreenEvent event) {
      if (this.module.getInstance() != null && event.getScreen() != this.module.getInstance() && !event.isCanceled()) {
         this.module.onScreenReplaced();
      }

   }
}
