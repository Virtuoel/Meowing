package virtuoel.meowing;

import java.util.List;

import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.service.MixinService;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.CatVariant;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.event.GameEvent;
import virtuoel.meowing.api.MeowingActionCallback;

public class Meowing implements ModInitializer
{
	public static final String MOD_ID = "meowing";
	
	public static final ILogger LOGGER = MixinService.getService().getLogger(MOD_ID);
	
	public Meowing()
	{
		
	}
	
	@Override
	public void onInitialize()
	{
		final SoundEvent[] sounds =
		{
			SoundEvents.ENTITY_CAT_STRAY_AMBIENT,
			SoundEvents.ENTITY_CAT_AMBIENT,
			SoundEvents.ENTITY_CAT_PURREOW,
			SoundEvents.ENTITY_CAT_PURR,
			SoundEvents.ENTITY_CAT_HISS,
			SoundEvents.ENTITY_CAT_BEG_FOR_FOOD,
			SoundEvents.ENTITY_CAT_DEATH,
			SoundEvents.ENTITY_OCELOT_AMBIENT,
			SoundEvents.ENTITY_OCELOT_DEATH
		};
		
		MeowingActionCallback.EVENT.register((entity, world) ->
		{
			final int index = entity instanceof PlayerEntity player ? player.getInventory().selectedSlot % sounds.length : entity.getRandom().nextInt(sounds.length);
			final SoundEvent sound = sounds[index];
			
			world.playSoundFromEntity(null, entity, sound, entity.getSoundCategory(), 1.0F, entity.getSoundPitch());
			
			if (sound == SoundEvents.ENTITY_CAT_HISS)
			{
				final List<PhantomEntity> phantoms = entity.world.getEntitiesByClass(PhantomEntity.class, entity.getBoundingBox().expand(16.0), EntityPredicates.VALID_ENTITY);
				
				phantoms.forEach(e -> e.setTarget(null));
				
				final List<CreeperEntity> creepers = entity.world.getEntitiesByClass(CreeperEntity.class, entity.getBoundingBox().expand(16.0), EntityPredicates.VALID_ENTITY);
				
				creepers.forEach(e -> e.setTarget(null));
			}
		});
		
		ServerPlayNetworking.registerGlobalReceiver(ACTION_PACKET, (server, player, handler, buf, responseSender) ->
		{
			server.execute(() ->
			{
				if (!player.isSpectator() && canMeow(player))
				{
					MeowingActionCallback.EVENT.invoker().doActionEffects(player, player.getEntityWorld());
				}
			});
		});
		
		UseItemCallback.EVENT.register((player, world, hand) ->
		{
			final ItemStack held = player.getStackInHand(hand);
			if (!player.isSpectator() && isCat(held))
			{
				player.swingHand(hand);
				return TypedActionResult.success(held);
			}
			
			return TypedActionResult.pass(held);
		});
		
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) ->
		{
			final ItemStack held = player.getStackInHand(hand);
			if (!player.isSpectator() && isCat(held))
			{
				if (world.getBlockState(hitResult.getBlockPos()).isOf(Blocks.SPAWNER))
				{
					player.swingHand(hand);
					
					if (!world.isClient)
					{
						BlockPos pos = hitResult.getBlockPos();
						Direction direction = hitResult.getSide();
						if (player.canPlaceOn(pos, direction, held))
						{
							EntityType<?> entityType = ((SpawnEggItem) held.getItem()).getEntityType(held.getNbt());
							if (null != entityType.spawnFromItemStack(
								(ServerWorld) world,
								held,
								player,
								pos.offset(direction),
								SpawnReason.SPAWN_EGG,
								true,
								direction == Direction.UP)
							)
							{
								if (!player.isCreative())
								{
									held.decrement(1);
								}
								world.emitGameEvent(player, GameEvent.ENTITY_PLACE, pos);
							}
						}
					}
					
					return ActionResult.SUCCESS;
				}
			}
			
			return ActionResult.PASS;
		});
		
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
		{
			if (player.isSpectator())
			{
				return ActionResult.PASS;
			}
			
			if (entity instanceof LivingEntity living)
			{
				if (living.isBaby())
				{
					return ActionResult.PASS;
				}
				
				if (entity instanceof TameableEntity tameable)
				{
					if (tameable.isSitting() || !tameable.isOwner(player))
					{
						return ActionResult.PASS;
					}
				}
				
				if (entity.getType() != EntityType.CAT && entity.getType() != EntityType.OCELOT)
				{
					return ActionResult.PASS;
				}
				
				if (isCat(player.getStackInHand(hand)))
				{
					return ActionResult.SUCCESS;
				}
				
				if (world.isClient)
				{
					return ActionResult.PASS;
				}
				
				if (!player.getStackInHand(Hand.MAIN_HAND).isOf(Items.STRING) && !player.getStackInHand(Hand.OFF_HAND).isOf(Items.STRING))
				{
					return ActionResult.PASS;
				}
				
				final ItemStack stack = entity.getPickBlockStack();
				
				if (stack != null && !stack.isEmpty())
				{
					final NbtCompound nbt = new NbtCompound();
					final NbtCompound entityData = new NbtCompound();
					final NbtList lore = new NbtList();
					
					living.writeCustomDataToNbt(entityData);
					
					if (entity instanceof CatEntity cat)
					{
						final CatVariant variant = cat.getVariant();
						nbt.putInt("CustomModelData", Registry.CAT_VARIANT.getRawId(variant) + 1);
						
						final Identifier id = Registry.CAT_VARIANT.getId(variant);
						lore.add(NbtString.of(Text.Serializer.toJson(Texts.setStyleIfAbsent(Text.translatable(String.format("entity.%s.cat.variant.%s", id.getNamespace(), id.getPath())), Style.EMPTY.withItalic(false).withColor(Formatting.GRAY)))));
					}
					else if (entity instanceof OcelotEntity ocelot)
					{
						if (entityData.contains("Trusting") && !entityData.getBoolean("Trusting"))
						{
							return ActionResult.PASS;
						}
						
						nbt.putInt("CustomModelData", 1);
					}
					
					entityData.putBoolean("PersistenceRequired", true);
					
					entityData.putString("CustomName", Text.Serializer.toJson(entity.getCustomName()));
					
					nbt.put("EntityTag", entityData);
					
					stack.setNbt(nbt);
					
					stack.setRepairCost(1000);
					stack.setCustomName(entity.getName());
					
					if (!lore.isEmpty())
					{
						stack.getOrCreateSubNbt("display").put("Lore", lore);
					}
					
					if (!player.hasStackEquipped(EquipmentSlot.HEAD))
					{
						player.equipStack(EquipmentSlot.HEAD, stack);
					}
					else if (!player.hasStackEquipped(EquipmentSlot.MAINHAND))
					{
						player.equipStack(EquipmentSlot.MAINHAND, stack);
					}
					else if (!player.hasStackEquipped(EquipmentSlot.OFFHAND))
					{
						player.equipStack(EquipmentSlot.OFFHAND, stack);
					}
					else
					{
						final ItemEntity itemEntity = entity.dropStack(stack);
						
						if (itemEntity != null)
						{
							itemEntity.resetPickupDelay();
						}
					}
					
					entity.remove(RemovalReason.DISCARDED);
					
					return ActionResult.SUCCESS;
				}
			}
			
			return ActionResult.PASS;
		});
	}
	
	public static boolean canMeow(LivingEntity entity)
	{
		return slotContainsCat(entity, EquipmentSlot.HEAD) || slotContainsCat(entity, EquipmentSlot.MAINHAND) || slotContainsCat(entity, EquipmentSlot.OFFHAND);
	}
	
	public static boolean slotContainsCat(LivingEntity entity, EquipmentSlot slot)
	{
		return isCat(entity.getEquippedStack(slot));
	}
	
	public static boolean isCat(ItemStack stack)
	{
		return (stack.isOf(Items.CAT_SPAWN_EGG) || stack.isOf(Items.OCELOT_SPAWN_EGG)) && stack.hasNbt() && stack.getNbt().getInt("CustomModelData") > 0;
	}
	
	public static Identifier id(String path)
	{
		return new Identifier(MOD_ID, path);
	}
	
	public static Identifier id(String path, String... paths)
	{
		return id(paths.length == 0 ? path : path + "/" + String.join("/", paths));
	}
	
	public static final Identifier ACTION_PACKET = id("action");
}
