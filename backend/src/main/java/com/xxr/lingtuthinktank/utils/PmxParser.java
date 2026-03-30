package com.xxr.lingtuthinktank.utils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 3D 模型解析器 (仅支持 PMX 2.0/2.1)
 */
public class PmxParser {

    public static Map<String, Object> parsePmx(File file) {
        Map<String, Object> info = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(file);
                FileChannel channel = fis.getChannel()) {

            // PMX Header 至少 4 + 4 + 1 + 8 + ...
            ByteBuffer buffer = ByteBuffer.allocate(100);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            channel.read(buffer);
            buffer.flip();

            // 1. Signature "PMX "
            byte[] signature = new byte[4];
            buffer.get(signature);
            if (!new String(signature).equals("PMX ")) {
                return null; // Not PMX
            }

            // 2. Version
            float version = buffer.getFloat();
            info.put("version", version);

            // 3. Globals Count
            byte globalCount = buffer.get();

            // 4. Globals (Encoding, AddUV, VertexIndexSize, TextureIndexSize,
            // MaterialIndexSize, BoneIndexSize, MorphIndexSize, RigidBodyIndexSize)
            byte[] globals = new byte[globalCount];
            buffer.get(globals);
            int encoding = globals[0]; // 0: UTF-16LE, 1: UTF-8

            // 5. Model Name (Text)
            // Need to read Text size first (int)
            int nameSize = buffer.getInt();
            byte[] nameBytes = new byte[nameSize];
            // Refill buffer if needed... for simplification assuming small header
            // For robust parser, actual stream reading is needed.
            // Simplified: Just return parsed basic info for demo

            info.put("valid", true);

        } catch (Exception e) {
            e.printStackTrace();
            info.put("error", e.getMessage());
        }
        return info;
    }
}
