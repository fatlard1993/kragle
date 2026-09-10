package justfatlard.kragle;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Putting the glue on, and taking it off again.
 *
 * <p>Right-click any block and it is yours: nobody else can break it, and nothing else can
 * either. Sneak and right-click one you glued and the glue comes off. Any block at all, which
 * is the point - a chest, a spawner, one block of somebody's roof.
 *
 * <p>A bottle does one block. That is the whole of what keeps it in proportion: this is not a
 * thing to be crafted and sprayed over a base, it is a thing somebody is handed a few of.
 */
public final class Kragle {
	private Kragle() {}

	/** Ops can unglue anything; it is the only way back from a player who left. */
	public static boolean isOperator(ServerPlayer player) {
		return Commands.LEVEL_GAMEMASTERS.check(player.createCommandSourceStack().permissions());
	}

	/** Whether this player may break, move or unglue this block. */
	public static boolean mayTouch(ServerLevel level, ServerPlayer player, BlockPos pos) {
		KragleMarks.Owner owner = KragleMarks.get(level).ownerOf(pos);
		return owner == null || owner.id().equals(player.getUUID()) || isOperator(player);
	}

	public static void register() {
		UseBlockCallback.EVENT.register(Kragle::onUse);
	}

	private static InteractionResult onUse(Player player, Level level, InteractionHand hand,
			BlockHitResult hit) {
		ItemStack held = player.getItemInHand(hand);
		if (!held.is(Main.KRAGLE)) return InteractionResult.PASS;
		if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer user)) {
			return InteractionResult.SUCCESS;
		}

		BlockPos pos = hit.getBlockPos();
		BlockState state = server.getBlockState(pos);
		if (state.isAir()) return InteractionResult.PASS;

		KragleMarks marks = KragleMarks.get(server);
		KragleMarks.Owner owner = marks.ownerOf(pos);

		if (player.isSecondaryUseActive()) return unglue(server, user, marks, owner, pos);

		if (owner != null) {
			user.sendSystemMessage(Component.translatable(
				owner.id().equals(user.getUUID())
					? "kragle.already_yours" : "kragle.already_theirs", owner.name())
				.withStyle(ChatFormatting.GRAY), true);
			return InteractionResult.SUCCESS;
		}

		marks.glue(pos, user.getUUID(), user.getName().getString());
		// Spent, unless the spending is somebody's creative inventory. One bottle, one block.
		if (!user.getAbilities().instabuild) held.shrink(1);

		server.playSound(null, pos, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 1.4F);
		splat(server, pos);
		user.sendSystemMessage(Component.translatable("kragle.glued",
			state.getBlock().getName()).withStyle(ChatFormatting.WHITE), true);
		return InteractionResult.SUCCESS;
	}

	private static InteractionResult unglue(ServerLevel level, ServerPlayer user, KragleMarks marks,
			KragleMarks.Owner owner, BlockPos pos) {
		if (owner == null) return InteractionResult.PASS;

		if (!owner.id().equals(user.getUUID()) && !isOperator(user)) {
			user.sendSystemMessage(Component.translatable("kragle.not_yours", owner.name())
				.withStyle(ChatFormatting.RED), true);
			return InteractionResult.SUCCESS;
		}

		marks.forget(pos);
		level.playSound(null, pos, SoundEvents.HONEY_BLOCK_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
		user.sendSystemMessage(Component.translatable("kragle.unglued")
			.withStyle(ChatFormatting.GRAY), true);
		return InteractionResult.SUCCESS;
	}

	/** A white splash on the face of the block, so the glue is a thing that happened. */
	private static void splat(ServerLevel level, BlockPos pos) {
		level.sendParticles(ParticleTypes.WHITE_SMOKE,
			pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.35, 0.35, 0.35, 0.01);
	}
}
