package sample.ast;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import sample.ast.node.AstNode;
import sample.ast.node.ClassAstNode;
import sample.ast.node.JavafileAstNode;
import sample.ast.node.MethodAstNode;

public class JavaAstVisitor extends VoidVisitorAdapter<AstNode> {

    @Override
    public void visit(CompilationUnit cu, AstNode arg) {
        // パッケージ名をJavafileNodeに設定
        arg.getAstNode(JavafileAstNode.class)
                .ifPresent(javafileAstNode -> cu.getPackageDeclaration()
                        .map(NodeWithName::getNameAsString)
                        .ifPresent(javafileAstNode::setPackageName));

        super.visit(cu, arg);
    }

    @Override
    public void visit(ClassExpr n, AstNode arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ClassOrInterfaceType n, AstNode arg) {
        super.visit(n, arg);
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration cd, AstNode arg) {
        // Javafile == Classnameの場合（interface/abstract含む）
        arg.getAstNode(JavafileAstNode.class)
                .ifPresent(javafileAstNode -> {
                    cd.getConstructors().forEach(constructor -> {
                        System.out.println("Constructor: " + constructor.getDeclarationAsString());
                    });
                    ClassAstNode classAstNode = new ClassAstNode(
                            cd.getNameAsString(),
                            cd);

                    javafileAstNode.addClassNode(classAstNode);
                    super.visit(cd, classAstNode);
                });

        // InnerClassの場合（interface/abstract含む）
        arg.getAstNode(ClassAstNode.class)
                .ifPresent(classNode -> {
                    cd.getConstructors().forEach(constructor -> {
                        System.out.println("Constructor: " + constructor.getDeclarationAsString());
                    });

                    ClassAstNode innerClassAstNode = new ClassAstNode(
                            cd.getNameAsString(),
                            cd);

                    classNode.addInnerClassNode(innerClassAstNode);
                    super.visit(cd, innerClassAstNode);
                });
    }

    @Override
    public void visit(MethodDeclaration md, AstNode arg) {
        arg.getAstNode(ClassAstNode.class)
                .ifPresent(classNode -> {
                    MethodAstNode methodAstNode = new MethodAstNode(md, md.resolve());
                    classNode.addMethodNode(methodAstNode);
                    super.visit(md, methodAstNode);
                });

        super.visit(md, arg);
    }

}
