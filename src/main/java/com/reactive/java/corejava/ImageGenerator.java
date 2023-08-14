package com.reactive.java.corejava;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.crypto.digests.Blake2sDigest;
import org.bouncycastle.util.encoders.Hex;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class ImageGenerator {
    public static PatternResult createPatterns(int m, int n, int[] size, boolean border, int id) {
        PatternResult patternResult = new PatternResult();
        int[][] patterns = new int[size[0]][size[1]];
        if (border) {
            fillRectangle(patterns, 0, 0, size[0] / 2, size[1] / 2, 255);
            fillRectangle(patterns, size[0] - size[0] / 2, size[1] - size[1] / 2, size[0] / 2, size[1] / 2, 255);
        }

        int length = Math.min(size[0], size[1]);
        int[] rowUnits = getUnits(m, length, size[0]).stream().mapToInt(i -> i).toArray();
        int[] colUnits = getUnits(n, length, size[1]).stream().mapToInt(i -> i).toArray();

        ArrayList<Integer> code = new ArrayList<>();
        if ((m * n - 4) / 8 > 0) {
            int digestBitSize = ((m * n - 4) / 8) * 8;
            Blake2sDigest digest = new Blake2sDigest(digestBitSize);
            if ((m * n - 4) % 8 != 0) {
                byte[] idBytes = Integer.toString(id % (1 << 8)).getBytes(StandardCharsets.UTF_8);
                digest.update(idBytes, 0, idBytes.length);
                byte[] values = new byte[digest.getDigestSize()];
                digest.doFinal(values, 0);
                String hexValues = Hex.toHexString(values);

                final String hexValuesBinaryString = String.format("%" + digestBitSize + "s", new BigInteger(hexValues, 16).toString(2)).replace(' ', '0');
                final List<Integer> hexValuesBinaryArray = new ArrayList<>(hexValuesBinaryString.codePoints()
                        .mapToObj(c -> Integer.parseInt(String.valueOf((char) c)))
                        .toList());

                // Convert the toFillValue to a binary string representation andPad the binary string with leading zeros to the desired bit length
                final String toFillBinaryString = String.format("%" + (m * n - 4) % 8 + "s", Integer.toBinaryString(id / (1 << 8))).replace(' ', '0');
                List<Integer> toFillBinaryArray = toFillBinaryString.codePoints()
                        .mapToObj(c -> Integer.parseInt(String.valueOf((char) c)))
                        .toList();

                for (int bIdx = 0; bIdx < toFillBinaryArray.size(); bIdx++) {
                    int b = toFillBinaryArray.get(toFillBinaryArray.size() - 1 - bIdx);
                    int insertIndex = hexValuesBinaryArray.size() - 1 - bIdx;
                    hexValuesBinaryArray.add(insertIndex, b);
                }
                code.addAll(hexValuesBinaryArray);
                // Setting Code value
                patternResult.setCode(hexValuesBinaryArray.stream().map(s -> String.valueOf(s)).toList());
            } else {
                byte[] idBytes = Integer.toString(id).getBytes(StandardCharsets.UTF_8);
                digest.update(idBytes, 0, idBytes.length);
                byte[] values = new byte[digest.getDigestSize()];
                digest.doFinal(values, 0);
                for (int i = 0; i < values.length; i++) {
                    for (int j = 0; j < 8; j++) {
                        code.add(i * 8 + j, ((values[i] >> (7 - j)) & 1));
                    }
                }
                // Setting Code value
                patternResult.setCode(Hex.toHexString(values));
            }
        } else {
            final String toFillBinaryString = String.format("%" + (m * n - 4) % 8 + "s", Integer.toBinaryString(id % (1 << 8))).replace(' ', '0');
            code = new ArrayList<>(toFillBinaryString.codePoints()
                    .mapToObj(c -> Integer.parseInt(String.valueOf((char) c)))
                    .toList());
            // Setting Code value
            patternResult.setCode(toFillBinaryString.toCharArray());

        }
        code.add(0, 0);
        code.add(n - 1, 1);
        code.add(code.size() + Math.min(-1, -1 * (n - 2)), 1);
        code.add(0);

        int[][] resultPatterns = patterns;
        for (int rIdx = 0; rIdx < rowUnits.length - 1; rIdx++) {
            for (int cIdx = 0; cIdx < colUnits.length - 1; cIdx++) {
                int value = code.get(n * rIdx + cIdx) * 255;
                for (int i = rowUnits[rIdx]; i < rowUnits[rIdx + 1]; i++) {
                    for (int j = colUnits[cIdx]; j < colUnits[cIdx + 1]; j++) {
                        resultPatterns[i][j] = value;
                    }
                }
            }
        }

        if (border) {
            int[][] patternsToChange = resizeSubArray(resultPatterns, rowUnits[0], rowUnits[rowUnits.length - 1], colUnits[0], colUnits[colUnits.length - 1], length);
            for (int i = 0; i < patternsToChange.length; i++) {
                System.arraycopy(patternsToChange[i], 0, resultPatterns[rowUnits[0] + i], colUnits[0], patternsToChange[i].length);
            }
        } else {
            resultPatterns = resizeSubArray(resultPatterns, rowUnits[0], rowUnits[rowUnits.length - 1], colUnits[0], colUnits[colUnits.length - 1], length);
        }
        patternResult.setPatterns(resultPatterns);
        return patternResult;
    }

 /*   private static int[] getUnits(int length, int units, int size) {
        int[] unitArray = new int[units + 1];
        int step = length / units;
        for (int i = 0; i < units; i++) {
            unitArray[i] = i * step + (size - length) / 2;
        }
        unitArray[units] = length;
        return unitArray;
    }*/

    public static ArrayList<Integer> getUnits(int pattern, int length, int patVal) {
        List<Integer> rowUnits = new ArrayList<>();

        for (int i = 0; i < length - 1; i += length / pattern) {
            rowUnits.add(i );
        }
        if (rowUnits.size() < pattern + 1) {
            rowUnits.add(length);
        }
        ArrayList<Integer> adjustedRowUnits = new ArrayList<>();
        for (int unit : rowUnits) {
            adjustedRowUnits.add(unit + (patVal - length) / 2);
        }
        return adjustedRowUnits;
    }


    private static void fillRectangle(int[][] array, int startRow, int startCol, int rows, int cols, int fillValue) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                array[startRow + i][startCol + j] = fillValue;
            }
        }
    }

    private static int[][] resizeSubArray(int[][] array, int startRow, int endRow, int startCol, int endCol, int length) {
        int[][] subArray = new int[length][length];
        for (int i = startRow; i < endRow; i++) {
            System.arraycopy(array[i], startCol, subArray[i - startRow], 0, endCol - startCol);
        }
        return subArray;
    }

    private static Map<String, Map<String, Object>> imageGenerator(int m, int n, int[] size, boolean border, List<String> idList) {
        Map<String, Map<String, Object>> imgList = new HashMap<>();

        for (String id : idList) {
            PatternResult patternResult = createPatterns(m, n, size, border, Integer.parseInt(id));
            Map<String, Object> imgInfo = new HashMap<>();
            imgInfo.put("code", patternResult.getCode());
            imgInfo.put("patterns", patternResult.getPatterns());
            imgList.put(id, imgInfo);
        }
        return imgList;
    }

    private static void saveImg(Map<String, Map<String, Object>> imgList, String filePath) {
        imgList.forEach((key, value) -> {
            try {
                Map<String, Object> imgInfo = imgList.get(key);
                int[][] patterns = (int[][]) imgInfo.get("patterns");

                BufferedImage image = new BufferedImage(patterns[0].length, patterns.length, BufferedImage.TYPE_BYTE_GRAY);
                for (int i = 0; i < patterns.length; i++) {
                    for (int j = 0; j < patterns[0].length; j++) {
                        image.setRGB(j, i, patterns[i][j] == 255 ? 0xFFFFFF : 0);
                    }
                }
                ImageIO.write(image, "jpg", new File(filePath + key + "_esl_java.jpg"));
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        });
    }

    public static void main(String[] args) {
        try {

            int[] patternSize = new int[]{4, 5};
            int[] bezelSize = new int[]{180, 200};
            boolean border = false;

            String idPath = "C:\\Users\\SolumTravel\\Downloads\\id_list.txt";
            String filePath = "C:\\Users\\SolumTravel\\Desktop\\python_image\\";
            List<String> eslIdList = FileUtils.readLines(new File(idPath), StandardCharsets.UTF_8);
            Map<String, Map<String, Object>> results = imageGenerator(patternSize[0], patternSize[1], bezelSize, border, eslIdList);
            saveImg(results, filePath);
            try (FileWriter file = new FileWriter(filePath + "esl_info_java.json")) {
                file.write(new Gson().toJson(Collections.singletonList(results)));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
