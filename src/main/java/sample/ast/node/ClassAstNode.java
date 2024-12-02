package sample.ast.node;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.util.ArrayList;
import java.util.List;

public class ClassAstNode extends AstNode {

    @JsonIgnore
    private final ClassOrInterfaceDeclaration cd;
    private final boolean isInterface;
    private final boolean isAbstract;
    private List<ClassAstNode> innerClassAstNodes = new ArrayList<>();
    private List<MethodAstNode> methodAstNodes = new ArrayList<>();

    public ClassAstNode(String className, ClassOrInterfaceDeclaration cd) {
        super(className);
        this.cd = cd;
        this.isInterface = cd.isInterface();
        this.isAbstract = cd.isAbstract();
    }

    @JsonProperty("class")
    @Override
    public String getName() {
        return super.getName();
    }

    public boolean isInterface() {
        return isInterface;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void addInnerClassNode(ClassAstNode innerClassAstNode) {
        this.innerClassAstNodes.add(innerClassAstNode);
    }

    public void addMethodNode(MethodAstNode methodAstNode) {
        this.methodAstNodes.add(methodAstNode);
    }

    @JsonProperty("inner_classes")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<ClassAstNode> getInnerClassAstNodes() {
        return innerClassAstNodes;
    }

    @JsonProperty("methods")
    public List<MethodAstNode> getMethodAstNodes() {
        return methodAstNodes;
    }
}
