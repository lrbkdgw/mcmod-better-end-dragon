#!/usr/bin/env python3
"""Procedurally generates the (very small) PNG assets used by the mod.

Run with:  python3 tools/generate_textures.py
"""
import os
import struct
import zlib

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
ITEM_DIR = os.path.join(ROOT, "src/main/resources/assets/betterenddragon/textures/item")
BLOCK_DIR = os.path.join(ROOT, "src/main/resources/assets/betterenddragon/textures/block")


def write_png(path, pixels):
    height = len(pixels)
    width = len(pixels[0])
    raw = bytearray()
    for row in pixels:
        raw.append(0)
        for px in row:
            raw.extend(px)

    def chunk(tag, data):
        out = struct.pack(">I", len(data)) + tag + data
        return out + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    png += chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as handle:
        handle.write(png)


def clamp(value):
    return max(0, min(255, int(round(value))))


def scale(colour, factor):
    return tuple(clamp(c * factor) for c in colour)


def crystal_icon(base, core, size=16):
    """A stylised end-crystal: an outlined rhombus with a bright inner core."""
    pixels = []
    cx = (size - 1) / 2.0
    cy = (size - 1) / 2.0
    for y in range(size):
        row = []
        for x in range(size):
            dx = abs(x - cx) / 5.5
            dy = abs(y - cy) / 7.5
            d = dx + dy
            if d > 1.02:
                row.append((0, 0, 0, 0))
                continue
            if d > 0.80:
                row.append(scale(base, 0.35) + (255,))
                continue
            inner_dx = abs(x - cx) / 2.6
            inner_dy = abs(y - cy) / 3.8
            if inner_dx + inner_dy <= 1.0:
                shade = 1.0 + (cy - y) * 0.03
                row.append(scale(core, shade) + (255,))
                continue
            shade = 1.15 - (y / float(size)) * 0.45
            if (x + y) % 5 == 0:
                shade += 0.12
            row.append(scale(base, shade) + (255,))
        pixels.append(row)
    return pixels


def device_texture(accent, size=16):
    """Dark polished plate with a glowing rune in the middle."""
    pixels = []
    cx = (size - 1) / 2.0
    cy = (size - 1) / 2.0
    dark = (26, 22, 32)
    for y in range(size):
        row = []
        for x in range(size):
            border = x == 0 or y == 0 or x == size - 1 or y == size - 1
            d = abs(x - cx) / 6.0 + abs(y - cy) / 6.0
            if border:
                row.append(scale(dark, 0.6) + (255,))
            elif d <= 0.45:
                row.append(scale(accent, 1.25) + (255,))
            elif d <= 0.72:
                row.append(scale(accent, 0.8) + (255,))
            elif d <= 0.95:
                row.append(scale(accent, 0.35) + (255,))
            else:
                noise = 1.0 + (((x * 7 + y * 13) % 5) - 2) * 0.05
                row.append(scale(dark, noise) + (255,))
            row[-1] = row[-1]
        pixels.append(row)
    return pixels


ITEMS = {
    "wither_crystal": ((44, 38, 54), (96, 84, 116)),
    "nether_crystal": ((142, 52, 44), (224, 126, 88)),
    "deep_crystal": ((26, 44, 138), (86, 148, 236)),
    "truth_crystal": ((150, 66, 180), (238, 196, 255)),
}

BLOCKS = {
    "keep_inventory_device": (226, 176, 60),
    "creative_flight_device": (86, 208, 226),
    "return_home_device": (96, 214, 108),
}


def main():
    for name, (base, core) in ITEMS.items():
        write_png(os.path.join(ITEM_DIR, name + ".png"), crystal_icon(base, core))
    for name, accent in BLOCKS.items():
        write_png(os.path.join(BLOCK_DIR, name + ".png"), device_texture(accent))
    print("textures written")


if __name__ == "__main__":
    main()
