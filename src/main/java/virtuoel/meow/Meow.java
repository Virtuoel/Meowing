package virtuoel.meow;

import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.service.MixinService;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class Meow implements ModInitializer
{
	public static final String MOD_ID = "meow";
	
	public static final ILogger LOGGER = MixinService.getService().getLogger(MOD_ID);
	
	public Meow()
	{
		
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
