package virtuoel.meowing;

import org.lwjgl.glfw.GLFW;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public class MeowingClient implements ClientModInitializer
{
	public static final KeyBinding ACTION_KEY = new KeyBinding("key.meowing.action", GLFW.GLFW_KEY_LEFT_ALT, "key.categories.meowing");
	
	@Override
	public void onInitializeClient()
	{
		KeyBindingHelper.registerKeyBinding(ACTION_KEY);
		
		ClientTickEvents.START_CLIENT_TICK.register(client ->
		{
			if (ACTION_KEY.wasPressed() && ClientPlayNetworking.canSend(Meowing.ACTION_PACKET))
			{
				ClientPlayNetworking.send(Meowing.ACTION_PACKET, new PacketByteBuf(Unpooled.buffer()));
			}
		});
		
		ItemTooltipCallback.EVENT.register((stack, context, lines) ->
		{
			if (!lines.isEmpty() && Meowing.isCat(stack))
			{
				lines.set(0, Text.empty().append(stack.getName()).formatted(stack.getRarity().formatting));
			}
		});
	}
}
