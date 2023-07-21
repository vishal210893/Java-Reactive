package com.reactive.java.corejava;

import com.google.gson.Gson;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.crypto.digests.Blake2sDigest;
import org.bouncycastle.util.encoders.Hex;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageGenerator {
    public static int[][] createPatterns(int m, int n, int[] size, boolean border, int id) throws NoSuchAlgorithmException {
        int[][] patterns = new int[size[0]][size[1]];
        if (border) {
            fillRectangle(patterns, 0, 0, size[0] / 2, size[1] / 2, 255);
            fillRectangle(patterns, size[0] - size[0] / 2, size[1] - size[1] / 2, size[0] / 2, size[1] / 2, 255);
        }

        int length = Math.min(size[0], size[1]);
        int[] row_units = getUnits(length, m, size[0]);
        int[] col_units = getUnits(length, n, size[1]);

        ArrayList<Integer> code = new ArrayList<>();
        if ((m * n - 4) / 8 > 0) {
            Blake2sDigest digest = new Blake2sDigest(m * n - 4);
            if ((m * n - 4) % 8 != 0) {
                byte[] idBytes = Integer.toString(id % (1 << 8)).getBytes(StandardCharsets.UTF_8);
                digest.update(idBytes, 0, idBytes.length);
                // TODO

            } else {
                byte[] idBytes = Integer.toString(id).getBytes(StandardCharsets.UTF_8);
                digest.update(idBytes, 0, idBytes.length);
                byte[] values = new byte[digest.getDigestSize()];
                digest.doFinal(values, 0);
                String hexValues = Hex.toHexString(values);
                for (int i = 0; i < values.length; i++) {
                    for (int j = 0; j < 8; j++) {
                        code.add(i * 8 + j, ((values[i] >> (7 - j)) & 1));
                    }
                }
            }

        } else {
            //TODO
            int[] values = new int[(m * n - 4) % 8];
            for (int i = 0; i < values.length; i++) {
                values[i] = (id >> i) & 1;
            }
            //code = values;
        }
        code.add(0, 0);
        code.add(n - 1, 1);
        code.add(code.size() + Math.min(-1, -1 * (n - 2)), 1);
        code.add(0);

        int[][] resultPatterns = new int[size[0]][size[1]];
        for (int rIdx = 0; rIdx < row_units.length - 1; rIdx++) {
            for (int cIdx = 0; cIdx < col_units.length - 1; cIdx++) {
                int value = code.get(n * rIdx + cIdx) * 255;
                for (int i = row_units[rIdx]; i < row_units[rIdx + 1]; i++) {
                    for (int j = col_units[cIdx]; j < col_units[cIdx + 1]; j++) {
                        resultPatterns[i][j] = value;
                    }
                }
            }
        }

        if (border) {
            int[][] patternsToChange = resizeSubArray(resultPatterns, row_units[0], row_units[row_units.length - 1], col_units[0], col_units[col_units.length - 1], length);
            for (int i = 0; i < patternsToChange.length; i++) {
                System.arraycopy(patternsToChange[i], 0, resultPatterns[row_units[0] + i], col_units[0], patternsToChange[i].length);
            }
        } else {
            resultPatterns = resizeSubArray(resultPatterns, row_units[0], row_units[row_units.length - 1], col_units[0], col_units[col_units.length - 1], length);
        }

        return resultPatterns;
    }

    private static int[] getUnits(int length, int units, int size) {
        int[] unitArray = new int[units + 1];
        int step = length / units;
        for (int i = 0; i < units; i++) {
            unitArray[i] = i * step + (size - length) / 2;
        }
        unitArray[units] = length;
        return unitArray;
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

    private static Map<String, Map<String, Object>> imageGenerator(int m, int n, int[] size, boolean border, List<String> idList) throws NoSuchAlgorithmException {
        Map<String, Map<String, Object>> imgList = new HashMap<>();

        for (String id : idList) {
            int[][] patterns = createPatterns(m, n, size, border, Integer.parseInt(id));
            String code = getCodeFromPatterns(patterns, m * n - 4);
            Map<String, Object> imgInfo = new HashMap<>();
            imgInfo.put("code", code);
            imgInfo.put("patterns", patterns);
            imgList.put(id, imgInfo);
        }
        return imgList;
    }

    private static String getCodeFromPatterns(int[][] patterns, int numBits) {
        // Calculate code from patterns (similar to Python code)
        // ...

        return "code"; // Return the calculated code
    }

    private static void saveImg(Map<String, Map<String, Object>> imgList, String filePath) {
        for (String key : imgList.keySet()) {
            Map<String, Object> imgInfo = imgList.get(key);
            int[][] patterns = (int[][]) imgInfo.get("patterns");

            BufferedImage image = new BufferedImage(patterns[0].length, patterns.length, BufferedImage.TYPE_BYTE_GRAY);
            for (int i = 0; i < patterns.length; i++) {
                for (int j = 0; j < patterns[0].length; j++) {
                    image.setRGB(j, i, patterns[i][j] == 255 ? 0xFFFFFF : 0);
                }
            }
            File outputFile = new File(filePath + key + "_esl.jpg");
            try {
                ImageIO.write(image, "jpg", outputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        // Create and configure the command-line options
        Options options = new Options();
        options.addOption(Option.builder().longOpt("pattern_size").hasArgs().desc("Pattern size (m, n)").build());
        options.addOption(Option.builder().longOpt("bezel_size").hasArgs().desc("Bezel size (height, width)").build());
        options.addOption(Option.builder().longOpt("border").hasArgs().desc("Include border (0 or 1)").build());
        options.addOption(Option.builder().longOpt("id_path").hasArg().desc("Path to ID file").build());
        options.addOption(Option.builder().longOpt("file_path").hasArg().desc("Output file path").build());

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            int[] patternSize = parseIntegerArray(cmd.getOptionValues("pattern_size"));
            int[] bezelSize = parseIntegerArray(cmd.getOptionValues("bezel_size"));
            boolean border = cmd.hasOption("border") && Integer.parseInt(cmd.getOptionValue("border")) == 1;
            String idPath = cmd.getOptionValue("id_path");
            String filePath = cmd.getOptionValue("file_path");

            List<String> eslIdList = FileUtils.readLines(new File(idPath), "UTF-8");
            Map<String, Map<String, Object>> results = imageGenerator(patternSize[0], patternSize[1], bezelSize, border, eslIdList);
            saveImg(results, filePath);
            try (FileWriter file = new FileWriter(filePath + "esl_info.json")) {
                file.write(new Gson().toJson(results));
            }

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    private static int[] parseIntegerArray(String[] values) {
        int[] result = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = Integer.parseInt(values[i]);
        }
        return result;
    }

}
