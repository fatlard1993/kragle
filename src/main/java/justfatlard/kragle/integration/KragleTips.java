package justfatlard.kragle.integration;

import justfatlard.block_tip.api.BlockTipApi;
import justfatlard.kragle.KragleMarks;

/**
 * Glue is invisible, so the card is where a player finds out a block will not come apart before
 * they stand there swinging at it. Said ahead of everything else for that reason, and naming
 * whoever did it because they are the one to ask.
 */
public final class KragleTips {
	private KragleTips() {}

	/** Above loot-ender's 10: a block you cannot break outranks whatever else it is. */
	private static final int PRIORITY = 20;

	public static void register() {
		BlockTipApi.describe(PRIORITY, (level, pos, state, player) -> {
			KragleMarks.Owner owner = KragleMarks.get(level).ownerOf(pos);
			return owner == null ? null : "Kragled by " + owner.name();
		});
	}
}
