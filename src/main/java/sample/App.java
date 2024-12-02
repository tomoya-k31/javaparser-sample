package sample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import sample.ast.JavaAstVisitor;
import sample.ast.node.AstNode;
import sample.ast.node.JavafileAstNode;
import sample.common.GlobalState;
import sample.common.TypeSolvers;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(
        name = "javaparser-sample",
        description = "Perform code syntax analysis using JavaParser and output the results to the specified file."
)
public class App implements Runnable {

    private static final JavaAstVisitor VISITOR = new JavaAstVisitor();

    /**
     * 抽出対象のJavaファイルのパス（複数指定可能）
     * 例: -f=src/main/java/sample/App.java
     */
    @CommandLine.Option(
            names = {"-f", "--file-path"},
            description = "Paths of the Java files to be extracted (multiple paths can be specified)",
            arity = "1..*",  // 1つ以上の引数が必要
            required = true
    )
    private List<File> filePaths = new ArrayList<>();

    /**
     * プロジェクト全体のsrcディレクトリのパス（複数指定可能）
     * 例: -s=src/main/java
     */
    @CommandLine.Option(
            names = {"-s", "--src-dir"},
            description = "Paths of the src directories of the entire project (multiple paths can be specified)",
            arity = "1..*",
            required = true
    )
    private List<File> srcDirs = new ArrayList<>();

    /**
     * プロジェクトが利用しているJARファイルのパス（複数指定可能）
     * 例: -j=lib/library1.jar -j=lib/library2.jar
     */
    @CommandLine.Option(
            names = {"-j", "--jar"},
            description = "Paths of the JAR files used by the project (multiple paths can be specified)"
    )
    private List<File> jarFiles = new ArrayList<>();

    /**
     * プロジェクトが利用しているJARファイルのテキスト（複数指定可能）
     * 例: --jar-txt=lib/library1.txt --jar-txt=lib/library2.txt
     */
    @CommandLine.Option(
            names = {"--jar-txt"},
            description = "Text files listing the JAR files used by the project (multiple files can be specified)"
    )
    private List<File> jarTexts = new ArrayList<>();

    /**
     * 構文解決結果の出力先ディレクトリ
     * 例: -o=output
     */
    @CommandLine.Option(
            names = {"-o", "--output-dir"},
            description = "Output directory for the syntax resolution results",
            defaultValue = "${user.dir}"
    )
    private File outputDir;

    /**
     * Javaのバージョン（com.github.javaparser.ParserConfiguration.LanguageLevelを参照）
     * 例: --java-ver=JAVA_17
     */
    @CommandLine.Option(
            names = {"--java-ver"},
            defaultValue = "JAVA_17"
    )
    private String javaVer;

    @Override
    public void run() {
        validate();

        // Java version
        ParserConfiguration.LanguageLevel languageLevel = ParserConfiguration.LanguageLevel.valueOf(javaVer);

        // initialize TypeSolver
        CombinedTypeSolver combinedSolver = new CombinedTypeSolver();
        combinedSolver.add(new ReflectionTypeSolver());
        TypeSolvers.createTypeSolverFromSrcDir(srcDirs, languageLevel).forEach(combinedSolver::add);
        TypeSolvers.createTypeSolverFromJars(jarFiles, jarTexts).forEach(combinedSolver::add);

        // initialize GlobalState
        List<String> usedLibraries = TypeSolvers.createTypeSolverFromJars(jarFiles, jarTexts).stream()
                .map(JarTypeSolver::getKnownClasses)
                .flatMap(Set::stream)
                .collect(Collectors.toList());
        GlobalState.init(usedLibraries);

        // initialize JavaParser
        final ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setLanguageLevel(languageLevel);
        parserConfiguration.setSymbolResolver(new JavaSymbolSolver(combinedSolver));
        StaticJavaParser.setConfiguration(parserConfiguration);

        filePaths.stream()
                .filter(File::isDirectory)
                .filter(File::exists)
                .forEach(filePath -> {
                    try (Stream<Path> files = Files.walk(Paths.get(filePath.toURI()))) {
                        files
                                .filter(path -> path.toString().endsWith(".java"))
                                .map(this::parseJavaFile)
                                .forEach(astNode -> {
                                    System.out.println("File: " + astNode.getName());
                                });
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

        filePaths.stream()
                .filter(File::isFile)
                .filter(File::exists)
                .map(filePath -> Paths.get(filePath.toURI()))
                .filter(path -> path.toString().endsWith(".java"))
                .map(this::parseJavaFile)
                .forEach(astNode -> {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.enable(SerializationFeature.INDENT_OUTPUT);
                    try {
                        System.out.println(mapper.writeValueAsString(astNode));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });

        System.out.println("output dir: " + outputDir);
    }

    // 入力値のチェック
    private void validate() {
        // filePathsのパスがファイルや存在しないディレクトリの場合はエラー
        filePaths.stream().filter(Predicate.not(File::exists)).map(File::toString).findAny().ifPresent(file -> {
            throw new IllegalArgumentException("filePaths must be files. '" + file + "' is not a file.");
        });

        // srcDirsのパスがファイルや存在しないディレクトリの場合はエラー
        srcDirs.stream().filter(Predicate.not(File::isDirectory)).map(File::toString).findAny().ifPresent(file -> {
            throw new IllegalArgumentException("srcDirs must be directories. '" + file + "' is not a directory.");
        });
    }

    private AstNode parseJavaFile(Path path) {
        // parse Java file
        CompilationUnit cu;
        try {
            cu = StaticJavaParser.parse(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse " + path + ": " + e.getMessage(), e);
        }

        // パッケージ名をディレクトリ構造に変換
        String relatedJavaFilePath = cu.getPackageDeclaration()
                .map(NodeWithName::getNameAsString)
                .map(pkg -> pkg.replace('.', '/'))
                .map(pkg -> pkg + "/")
                .orElse("") + path.getFileName().toString();

        final AstNode node = new JavafileAstNode(relatedJavaFilePath, cu);
        cu.accept(VISITOR, node);
        return node;
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new App());
        try {
            int exitCode = commandLine.execute(args);
            System.exit(exitCode);
        } catch (CommandLine.ParameterException ex) {
            System.err.println("Error: " + ex.getMessage());
            commandLine.usage(System.err);
            System.exit(1);
        }
    }
}
