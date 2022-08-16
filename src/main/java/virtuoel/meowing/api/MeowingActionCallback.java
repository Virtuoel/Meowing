package virtuoel.meowing.api;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

@FunctionalInterface
@ApiStatus.Experimental
public interface MeowingActionCallback
{
	@ApiStatus.Experimental
	Event<MeowingActionCallback> EVENT = EventFactory.createArrayBacked(MeowingActionCallback.class,
		(listeners) -> (entity, world) ->
		{
			for (final MeowingActionCallback event : listeners)
			{
				event.doActionEffects(entity, world);
			}
		}
	);
	
	@ApiStatus.Experimental
	void doActionEffects(LivingEntity entity, World world);
}
