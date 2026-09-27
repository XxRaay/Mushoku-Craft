package com.mushokucraft.magic.circle;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;

import java.util.Arrays;

public class MagicCirclePattern {
    public static final int SIZE = 16;
    public static final int TOTAL_PIXELS = SIZE * SIZE;

    private final byte[] pixels;

    public MagicCirclePattern() {
        this.pixels = new byte[TOTAL_PIXELS];
    }

    public MagicCirclePattern(byte[] pixels) {
        if (pixels == null || pixels.length != TOTAL_PIXELS) {
            this.pixels = new byte[TOTAL_PIXELS];
            if (pixels != null) {
                System.arraycopy(pixels, 0, this.pixels, 0, Math.min(pixels.length, TOTAL_PIXELS));
            }
        } else {
            this.pixels = Arrays.copyOf(pixels, TOTAL_PIXELS);
        }
    }

    public MagicCirclePattern(String[] asciiRows) {
        this.pixels = new byte[TOTAL_PIXELS];
        if (asciiRows != null) {
            for (int y = 0; y < Math.min(asciiRows.length, SIZE); y++) {
                String row = asciiRows[y];
                for (int x = 0; x < Math.min(row.length(), SIZE); x++) {
                    if (row.charAt(x) == '#') {
                        setPixel(x, y, true);
                    }
                }
            }
        }
    }

    public boolean getPixel(int x, int y) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return false;
        return pixels[y * SIZE + x] == 1;
    }

    public void setPixel(int x, int y, boolean value) {
        if (x >= 0 && x < SIZE && y >= 0 && y < SIZE) {
            pixels[y * SIZE + x] = (byte) (value ? 1 : 0);
        }
    }

    public byte[] getRawBytes() {
        return pixels;
    }

    public int countFilled() {
        int count = 0;
        for (byte b : pixels) {
            if (b == 1) count++;
        }
        return count;
    }

    /**
     * Checks if this pattern is an EXACT 1:1 match with another pattern.
     */
    public boolean matches(MagicCirclePattern other) {
        if (other == null) return false;
        return Arrays.equals(this.pixels, other.pixels);
    }

    public int countDifferences(MagicCirclePattern other) {
        if (other == null) return countFilled();
        int diff = 0;
        for (int i = 0; i < TOTAL_PIXELS; i++) {
            if (this.pixels[i] != other.pixels[i]) {
                diff++;
            }
        }
        return diff;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putByteArray("Pixels", Arrays.copyOf(this.pixels, TOTAL_PIXELS));
        return tag;
    }

    public static MagicCirclePattern load(CompoundTag tag) {
        if (tag != null && tag.contains("Pixels")) {
            return new MagicCirclePattern(tag.getByteArray("Pixels"));
        }
        return new MagicCirclePattern();
    }

    public void writeToBuf(ByteBuf buf) {
        buf.writeBytes(this.pixels);
    }

    public static MagicCirclePattern readFromBuf(ByteBuf buf) {
        byte[] bytes = new byte[TOTAL_PIXELS];
        buf.readBytes(bytes);
        return new MagicCirclePattern(bytes);
    }

    public MagicCirclePattern copy() {
        return new MagicCirclePattern(this.pixels);
    }
}
