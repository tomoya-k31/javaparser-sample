package sample.common;

import sample.domain.JarFile;

import java.io.*;
import java.util.List;

public final class ArgsParsers {

    private ArgsParsers() {
    }

    /**
     * 以下のJARファイルのテキストフォーマットを解析する
     * --------------------------------------------------
     * Library: com.github.javaparser:javaparser-core:3.26.2, Path: /Users/tomoya-k31/.gradle/caches/modules-2/files-2.1/com.github.javaparser/javaparser-core/3.26.2/795eca30f20fdc110d1a02a1f0e7ad207e17e7c1/javaparser-core-3.26.2.jar
     * Library: org.apache.commons:commons-lang3:3.17.0, Path: /Users/tomoya-k31/.gradle/caches/modules-2/files-2.1/org.apache.commons/commons-lang3/3.17.0/b17d2136f0460dcc0d2016ceefca8723bdf4ee70/commons-lang3-3.17.0.jar
     * Library: jakarta.json:jakarta.json-api:2.1.3, Path: /Users/tomoya-k31/.gradle/caches/modules-2/files-2.1/jakarta.json/jakarta.json-api/2.1.3/4febd83e1d9d1561d078af460ecd19532383735c/jakarta.json-api-2.1.3.jar
     * --------------------------------------------------
     *
     * @param jarTextFile JARファイルのテキスト
     */
    public static List<JarFile> parseJarText(File jarTextFile) {
        if (jarTextFile == null || !jarTextFile.exists()) {
            throw new IllegalArgumentException("File not found: " + jarTextFile);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(jarTextFile))) {
            return reader.lines()
                    .map(ArgsParsers::parseLine)
                    .distinct()
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static JarFile parseLine(String line) {
        String[] split = line.split(", ");
        if (split.length != 2) {
            throw new IllegalArgumentException("Invalid line: " + line);
        }
        JarFile jarFile = new JarFile();
        jarFile.setLibrary(split[0].substring("Library: ".length()));
        jarFile.setPath(new File(split[1].substring("Path: ".length())));
        return jarFile;
    }
}
