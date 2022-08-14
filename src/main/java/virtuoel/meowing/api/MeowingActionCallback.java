package virtuoel.meowing.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

@FunctionalInterface
public interface MeowingActionCallback
{
	Event<MeowingActionCallback> EVENT = EventFactory.createArrayBacked(MeowingActionCallback.class,
		(listeners) -> (entity, world) ->
		{
			for (final MeowingActionCallback event : listeners)
			{
				event.doActionEffects(entity, world);
			}
		}
	);
	
	void doActionEffects(LivingEntity entity, World world);
}
