package sample.common;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import sample.domain.JarFile;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** TypeSolverを生成するクラス */
public final class TypeSolvers {
    private TypeSolvers() {
    }

    /** srcDirsからTypeSolverを生成 */
    public static List<TypeSolver> createTypeSolverFromSrcDir(List<File> srcDirs,
                                                              ParserConfiguration.LanguageLevel languageLevel) {
        return srcDirs.stream()
                .map(srcDir -> {
                    final ParserConfiguration parserConfiguration = new ParserConfiguration();
                    parserConfiguration.setLanguageLevel(languageLevel);
                    return new JavaParserTypeSolver(srcDir, parserConfiguration);
                })
                .collect(Collectors.toUnmodifiableList());
    }

    /** jarFilesとjarTextsからTypeSolverを生成 */
    public static List<JarTypeSolver> createTypeSolverFromJars(List<File> jarFiles,
                                                            List<File> jarTexts) {
        Stream<JarTypeSolver> jarFileStream = jarFiles.stream()
                .map(jarFile -> {
                    try {
                        return new JarTypeSolver(jarFile);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

        Stream<JarTypeSolver> jarTextStream = jarTexts.stream()
                .flatMap((File jarTextFile) -> ArgsParsers.parseJarText(jarTextFile).stream())
                .filter(JarFile::exists)
                .map(jarFile -> {
                    try {
                        return new JarTypeSolver(jarFile.getPath());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

        return Stream.concat(jarFileStream, jarTextStream).toList();
    }
}
