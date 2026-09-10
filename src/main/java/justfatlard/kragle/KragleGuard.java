package justfatlard.kragle;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * What glued means when somebody tries to take the block anyway.
 *
 * <p>Breaking is refused and said out loud, with the name of whoever glued it, because a block
 * that simply will not break and says nothing reads as a bug rather than as somebody's.
 *
 * <p>Explosions are the other half, and the half that matters: a rule that only stops a
 * pickaxe is a rule that anybody gets round with one block of TNT.
 */
public final class KragleGuard {
	private KragleGuard() {}

	public static void register() {
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
			if (!(level instanceof ServerLevel server)) return true;
			if (!(player instanceof ServerPlayer breaker)) return true;

			KragleMarks.Owner owner = KragleMarks.get(server).ownerOf(pos);
			if (owner == null || Kragle.mayTouch(server, breaker, pos)) return true;

			breaker.sendSystemMessage(Component.translatable("kragle.refused", owner.name())
				.withStyle(ChatFormatting.RED), true);
			server.playSound(null, pos, SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 0.5F, 1.6F);
			return false;
		});

		// A block that is gone is not glued any more. The owner broke it, or an operator did;
		// either way the mark outliving the block would glue whatever is built there next.
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (level instanceof ServerLevel server) KragleMarks.get(server).forget(pos);
		});
	}
}
