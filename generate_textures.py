#!/usr/bin/env python3
"""Draw the Kragle: a vanilla potion bottle with something white and thick in it.

Composited from the game's own two potion textures rather than drawn fresh, because the
bottle has to read as a potion at a glance - that is the whole of the joke and the whole of
the warning. The overlay is the liquid and takes a colour; the bottle over it is the glass,
the cork and the shading, and is used exactly as it ships.

Usage: python3 generate_textures.py [client jar]
"""
import glob
import os
import sys
import zipfile

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "src/main/resources/assets/kragle/textures/item/kragle.png")

# Not pure white: a white liquid against a light bottle is a bottle of nothing. This is the
# off-white of PVA glue, which is what the thing is.
GLUE = (238, 236, 226)


def minecraft_version():
    for line in open(os.path.join(HERE, "gradle.properties")):
        key, sep, value = line.partition("=")
        if sep and key.strip() == "minecraft_version":
            return value.strip()
    return None


def find_jar():
    if len(sys.argv) > 1:
        return sys.argv[1]
    cache = os.path.expanduser("~/.gradle/caches/fabric-loom")
    version = minecraft_version()
    found = glob.glob(os.path.join(cache, version or "*", "minecraft-client.jar"))
    if not found:
        found = glob.glob(os.path.join(cache, "*", "minecraft-client.jar"))
    if not found:
        sys.exit("no cached Minecraft client jar found: build once, or pass a jar path")
    return max(found, key=os.path.getmtime)


def read(jar, path):
    from io import BytesIO
    return Image.open(BytesIO(jar.read("assets/minecraft/textures/item/" + path))).convert("RGBA")


def main():
    with zipfile.ZipFile(find_jar()) as jar:
        overlay = read(jar, "potion_overlay.png")
        bottle = read(jar, "potion.png")

    # The overlay ships as a greyscale mask the game multiplies a colour through; done here
    # once instead, because this potion is only ever the one colour.
    liquid = Image.new("RGBA", overlay.size, (0, 0, 0, 0))
    for x in range(overlay.width):
        for y in range(overlay.height):
            r, g, b, a = overlay.getpixel((x, y))
            if not a:
                continue
            shade = r / 255.0
            liquid.putpixel((x, y), (
                int(GLUE[0] * shade), int(GLUE[1] * shade), int(GLUE[2] * shade), a))

    out = Image.new("RGBA", bottle.size, (0, 0, 0, 0))
    out.alpha_composite(liquid)
    out.alpha_composite(bottle)

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    out.save(OUT)
    print("wrote %s (%dx%d)" % (os.path.relpath(OUT, HERE), out.width, out.height))


if __name__ == "__main__":
    main()
