#!/usr/bin/env python3
"""Generates the 16x16 textures used by Better End Dragon.

The mod ships no copied vanilla art: every texture is drawn here from scratch.
Run with `python3 tools/generate_textures.py` from the repository root.
"""
import os
import struct
import zlib

SIZE = 16
ITEM_DIR = "src/main/resources/assets/betterenddragon/textures/item"
BLOCK_DIR = "src/main/resources/assets/betterenddragon/textures/block"


def write_png(path, pixels):
    raw = b""
    for y in range(SIZE):
        raw += b"\x00"
        for x in range(SIZE):
            raw += bytes(pixels[y][x])

    def chunk(tag, data):
        c = struct.pack(">I", len(data)) + tag + data
        return c + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)

    header = struct.pack(">IIBBBBB", SIZE, SIZE, 8, 6, 0, 0, 0)
    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", header)
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as fh:
        fh.write(png)


def blank():
    return [[(0, 0, 0, 0) for _ in range(SIZE)] for _ in range(SIZE)]


def mix(color, other, amount):
    return tuple(int(color[i] * (1 - amount) + other[i] * amount) for i in range(3))


def crystal(base, glow, spark=False):
    """A diamond shaped crystal with a bright core and a darker outline."""
    px = blank()
    cx = cy = 7.5
    for y in range(SIZE):
        for x in range(SIZE):
            d = abs(x - cx) + abs(y - cy)
            if d > 7.2:
                continue
            if d > 6.2:
                color = mix(base, (0, 0, 0), 0.55)
                px[y][x] = (color[0], color[1], color[2], 255)
                continue
            shade = 0.55 - (y / SIZE) * 0.45 + (0.25 if x < cx else 0.0)
            color = mix(base, glow, max(0.0, min(1.0, shade)))
            if d < 2.5:
                color = mix(color, (255, 255, 255), 0.45)
            if spark and (x + y) % 7 == 0 and d < 5.5:
                color = mix(color, (255, 255, 255), 0.6)
            px[y][x] = (color[0], color[1], color[2], 255)
    return px


def device(base, rune):
    """A dark bordered block with a glowing rune in the middle."""
    px = blank()
    for y in range(SIZE):
        for x in range(SIZE):
            edge = min(x, y, SIZE - 1 - x, SIZE - 1 - y)
            color = mix(base, (0, 0, 0), 0.65 if edge < 1 else 0.35 if edge < 2 else 0.0)
            noise = ((x * 7 + y * 13) % 5) / 40.0
            color = mix(color, (255, 255, 255), noise)
            px[y][x] = (color[0], color[1], color[2], 255)

    for (x, y) in rune:
        color = mix((255, 255, 255), base, 0.15)
        px[y][x] = (color[0], color[1], color[2], 255)
    return px


def ring(cx, cy, r):
    out = []
    for y in range(SIZE):
        for x in range(SIZE):
            d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            if abs(d - r) < 0.6:
                out.append((x, y))
    return out


def cross(cx, cy, size):
    out = []
    for i in range(-size, size + 1):
        out.append((cx + i, cy))
        out.append((cx, cy + i))
    return out


def arrow_up(cx, cy):
    out = []
    for i in range(-4, 5):
        out.append((cx, cy + i))
    for i in range(0, 4):
        out.append((cx - i, cy - 4 + i))
        out.append((cx + i, cy - 4 + i))
    return out


def main():
    write_png(os.path.join(ITEM_DIR, "wither_crystal.png"),
              crystal((34, 32, 40), (96, 92, 110)))
    write_png(os.path.join(ITEM_DIR, "nether_crystal.png"),
              crystal((124, 46, 38), (226, 116, 72)))
    write_png(os.path.join(ITEM_DIR, "abyssal_crystal.png"),
              crystal((22, 38, 132), (86, 148, 255)))
    write_png(os.path.join(ITEM_DIR, "truth_crystal.png"),
              crystal((156, 112, 208), (236, 222, 255), spark=True))

    write_png(os.path.join(BLOCK_DIR, "keep_inventory_device.png"),
              device((92, 66, 24), ring(7, 7, 4.5) + cross(7, 7, 1)))
    write_png(os.path.join(BLOCK_DIR, "creative_flight_device.png"),
              device((28, 92, 116), arrow_up(7, 8)))
    write_png(os.path.join(BLOCK_DIR, "return_device.png"),
              device((32, 96, 48), ring(7, 7, 5.0) + ring(7, 7, 2.0)))


if __name__ == "__main__":
    main()
