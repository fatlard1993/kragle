# Kragle

A Minecraft Fabric mod. A bottle of glue. What you put it on, nobody else takes apart.

## What This Mod Does

Right-click any block with the Kragle and that block is yours. Nobody else can break it, and no explosion takes it either. Sneak and right-click a block you glued and the glue comes off.

Any block at all: a chest, a spawner, one block of somebody's roof, the bell in the middle of a village. It does not care what the block is, which is the point.

## No Recipe

There is not going to be one. A block nobody can break is not a thing to be farmed. It lives in a creative tab and comes out of `/give`, and that is the whole supply — an operator hands somebody a bottle for a reason.

A bottle does one block, and that is what keeps it in proportion. They stack like anything else — to 64, or as far as [stackz](../stackz) takes a stack where it is installed.

## Who Can Take It Off

The player who glued it, and operators. Nobody else, including whoever owns the land it is standing on.

Both the refusal and the glue say who to ask. A block that simply will not break and says nothing reads as a bug rather than as somebody's.

With [block-tip](https://github.com/fatlard1993/block-tip) installed, a glued block's card says "Kragled by" and the name of whoever glued it, so the question is answered before anybody starts swinging.

## Explosions

A glued block is taken out of the blast rather than given a blast resistance. Resistance belongs to a kind of block; this belongs to one block in one place, and obsidian-proof cobblestone is not a thing to make every cobblestone.

Everything around it goes as it would have. Only the glued block stays.

## Pandorical

Registered through [Pandorical](https://github.com/fatlard1993/pandorical), so Kragle itself is installed on the server only: the bottle's item, model and texture reach each client from the server on join. The Kragle is a real item, though, and a real item has to be known to every client, so **Pandorical is required on every client**; there is no vanilla-client fallback. The glue itself is all server-side, so a glued block holds no matter what the player trying to break it has installed.

## Development

Installing is in [DEVELOPMENT.md](DEVELOPMENT.md).

## License

MIT, see [LICENSE](LICENSE).
