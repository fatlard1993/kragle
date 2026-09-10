package justfatlard.kragle.mixin;

import java.util.List;
import justfatlard.kragle.KragleMarks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A glued block is not in the blast.
 *
 * <p>Taken out of the list of what an explosion found rather than by giving the block a
 * resistance: resistance is a property of a kind of block, and this is a property of one
 * block in one place. Obsidian-proof cobblestone is not a thing to make every cobblestone.
 *
 * <p>The list is what the explosion is about to break, so removing an entry is the whole of
 * it: the block stays, and everything around it goes as it would have.
 */
@Mixin(ServerExplosion.class)
public abstract class ExplosionMixin {
	@Shadow
	@Final
	private ServerLevel level;

	@Inject(method = "calculateExplodedPositions", at = @At("RETURN"), cancellable = true)
	private void kragle$spareGluedBlocks(CallbackInfoReturnable<List<BlockPos>> cir) {
		List<BlockPos> found = cir.getReturnValue();
		if (found == null || found.isEmpty()) return;

		KragleMarks marks = KragleMarks.get(this.level);
		if (marks.count() == 0) return;

		List<BlockPos> kept = new java.util.ArrayList<>(found.size());
		for (BlockPos pos : found) {
			if (!marks.isGlued(pos)) kept.add(pos);
		}
		if (kept.size() != found.size()) cir.setReturnValue(kept);
	}
}
