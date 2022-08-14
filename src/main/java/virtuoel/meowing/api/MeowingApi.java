package virtuoel.meowing.api;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class MeowingApi
{
	public static boolean slotContainsCat(LivingEntity entity, EquipmentSlot slot)
	{
		final ItemStack stack = entity.getEquippedStack(slot);
		
		return (stack.isOf(Items.CAT_SPAWN_EGG) || stack.isOf(Items.OCELOT_SPAWN_EGG)) && stack.hasNbt() && stack.getNbt().getInt("CustomModelData") > 0;
	}
}
