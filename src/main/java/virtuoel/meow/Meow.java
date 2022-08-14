package virtuoel.meow;

import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.service.MixinService;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Meow implements ModInitializer
{
	public static final String MOD_ID = "meow";
	
	public static final ILogger LOGGER = MixinService.getService().getLogger(MOD_ID);
	
	public Meow()
	{
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
		{
			if (entity instanceof LivingEntity living)
			{
				if (entity instanceof TameableEntity tameable && !tameable.isOwner(player))
				{
					return ActionResult.PASS;
				}
				
				if (entity.getType() != EntityType.CAT && entity.getType() != EntityType.OCELOT)
				{
					return ActionResult.PASS;
				}
				
				if (!player.getStackInHand(Hand.OFF_HAND).isOf(Items.STRING))
				{
					return ActionResult.PASS;
				}
				
				final ItemStack stack = entity.getPickBlockStack();
				
				if (stack != null && !stack.isEmpty())
				{
					final NbtCompound nbt = new NbtCompound();
					final NbtCompound entityData = new NbtCompound();
					
					living.writeCustomDataToNbt(entityData);
					
					if (entity instanceof CatEntity cat)
					{
						nbt.putInt("CustomModelData", Registry.CAT_VARIANT.getRawId(cat.getVariant()) + 1);
					}
					else if (entity instanceof OcelotEntity ocelot)
					{
						if (entityData.contains("Trusting") && !entityData.getBoolean("Trusting"))
						{
							return ActionResult.PASS;
						}
						
						nbt.putInt("CustomModelData", 1);
					}
					
					nbt.put("EntityTag", entityData);
					
					stack.setNbt(nbt);
					
					if (entity.hasCustomName())
					{
						stack.setCustomName(entity.getCustomName());
					}
					
					entity.dropStack(stack);
					entity.remove(RemovalReason.DISCARDED);
					
					return ActionResult.SUCCESS;
				}
			}
			
			return ActionResult.PASS;
		});
	}
	
	@Override
	public void onInitialize()
	{
		
	}
	
	public static Identifier id(String path)
	{
		return new Identifier(MOD_ID, path);
	}
	
	public static Identifier id(String path, String... paths)
	{
		return id(paths.length == 0 ? path : path + "/" + String.join("/", paths));
	}
}
