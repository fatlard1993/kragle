package justfatlard.kragle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Which blocks have been glued, and by whom.
 *
 * <p>Kept here rather than on the block, because a block has nowhere to keep it: the glue is on
 * any block at all, including the ones that are only a state and carry nothing of their own.
 * Per world, because a position means nothing without one.
 *
 * <p>The owner is a name as much as a permission. Somebody has to be told who to ask.
 */
public final class KragleMarks extends SavedData {
	private static final String STORAGE_KEY = "kragle_marks";

	private record Mark(long pos, UUID owner, String ownerName) {
		static final Codec<Mark> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.LONG.fieldOf("pos").forGetter(Mark::pos),
			net.minecraft.core.UUIDUtil.CODEC.fieldOf("owner").forGetter(Mark::owner),
			Codec.STRING.optionalFieldOf("ownerName", "").forGetter(Mark::ownerName)
		).apply(instance, Mark::new));
	}

	private record Data(List<Mark> marks) {
		static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Mark.CODEC.listOf().optionalFieldOf("marks", List.of()).forGetter(Data::marks)
		).apply(instance, Data::new));
	}

	/** Who glued a block, and what they were called when they did it. */
	public record Owner(UUID id, String name) {}

	public static final Codec<KragleMarks> CODEC = Data.CODEC.xmap(KragleMarks::fromData, KragleMarks::toData);
	private static final SavedDataType<KragleMarks> TYPE = new SavedDataType<>(
		Identifier.parse(STORAGE_KEY), KragleMarks::new, CODEC, DataFixTypes.LEVEL);

	private final Map<Long, Owner> glued = new HashMap<>();

	public static KragleMarks get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(TYPE);
	}

	private static KragleMarks fromData(Data data) {
		KragleMarks marks = new KragleMarks();
		for (Mark mark : data.marks()) {
			marks.glued.put(mark.pos(), new Owner(mark.owner(), mark.ownerName()));
		}
		return marks;
	}

	private Data toData() {
		List<Mark> out = new ArrayList<>();
		for (Map.Entry<Long, Owner> entry : this.glued.entrySet()) {
			out.add(new Mark(entry.getKey(), entry.getValue().id(), entry.getValue().name()));
		}
		return new Data(out);
	}

	/** Who glued this block, or null for a block nobody has. */
	public Owner ownerOf(BlockPos pos) {
		return this.glued.get(pos.asLong());
	}

	public boolean isGlued(BlockPos pos) {
		return this.glued.containsKey(pos.asLong());
	}

	public void glue(BlockPos pos, UUID owner, String ownerName) {
		this.glued.put(pos.asLong(), new Owner(owner, ownerName));
		this.setDirty();
	}

	/** Forget a block: it was unglued, or it is gone and the glue went with it. */
	public boolean forget(BlockPos pos) {
		boolean had = this.glued.remove(pos.asLong()) != null;
		if (had) this.setDirty();
		return had;
	}

	public int count() {
		return this.glued.size();
	}
}
