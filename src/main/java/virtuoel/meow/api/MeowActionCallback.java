package virtuoel.meow.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

@FunctionalInterface
public interface MeowActionCallback
{
	Event<MeowActionCallback> EVENT = EventFactory.createArrayBacked(MeowActionCallback.class,
		(listeners) -> (entity, world) ->
		{
			for (final MeowActionCallback event : listeners)
			{
				event.doActionEffects(entity, world);
			}
		}
	);
	
	void doActionEffects(LivingEntity entity, World world);
}
