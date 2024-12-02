package sample.ast.node;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.javaparser.ast.CompilationUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 1つの.javaファイルを表すノードクラス
 */
public class JavafileAstNode extends AstNode {

    private final List<ClassAstNode> classAstNodes = new ArrayList<>(1);
    private final List<ImportNode> importNodes;
    private String packageName = "";

    /**
     * Constructor
     * @param relatedJavaFilePath .javaファイルの相対パス
     * @param cu CompilationUnit
     */
    public JavafileAstNode(String relatedJavaFilePath, CompilationUnit cu) {
        super(relatedJavaFilePath);
        this.importNodes = cu.getImports().stream()
                .map(importDecl -> new ImportNode(importDecl.getNameAsString(), importDecl.isStatic(), importDecl.isAsterisk()))
                .toList();
    }

    @JsonProperty("package")
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @JsonProperty("classes")
    public List<ClassAstNode> getClassAstNodes() {
        return classAstNodes;
    }

    public void addClassNode(ClassAstNode classAstNode) {
        classAstNodes.add(classAstNode);
    }

    @JsonProperty("imports")
    public List<String> getImportNodes() {
        return importNodes.stream()
                .map(ImportNode::toString)
                .collect(Collectors.toList());
    }

}
