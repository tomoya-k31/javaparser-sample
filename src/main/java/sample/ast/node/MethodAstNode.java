package sample.ast.node;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import sample.common.GlobalState;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MethodAstNode extends AstNode {

    private final MethodDeclaration md;
    private final ResolvedMethodDeclaration resolved;
    private final String body;
    private final String modifiers;
    private final List<ReferenceNode> references = new ArrayList<>();

    public MethodAstNode(MethodDeclaration md, ResolvedMethodDeclaration resolved) {
        super(resolved.getSignature());
        this.md = md;
        this.resolved = resolved;
        this.body = md.toString();

        // publicだけは場合によって条件あり（interfaceとか）
        Stream<Modifier.Keyword> publicModifiers = md.isPublic() ? Stream.of(Modifier.Keyword.PUBLIC) : Stream.empty();
        this.modifiers = Stream.concat(publicModifiers, md.getModifiers().stream().map(Modifier::getKeyword))
                        .distinct()
                        .map(Modifier.Keyword::asString)
                        .collect(Collectors.joining(" "));

        // ラムダ式内から呼んでいるメソッドを抽出
        md.findAll(LambdaExpr.class).forEach(lambda -> {
                    lambda.findAll(MethodCallExpr.class).forEach(methodCall -> {
                        try {
                            ResolvedMethodDeclaration resolvedMethod = methodCall.resolve();
                            if (!GlobalState.useLibrary(resolvedMethod.getQualifiedSignature(), true)) {
                                references.add(new ReferenceNode(methodCall.toString(), resolvedMethod.getQualifiedSignature()));
                            }
                        } catch (Exception e) {
                            System.err.println("Unresolved: " + methodCall);
                        }
                    });
                });

        // メソッド参照
        md.findAll(MethodReferenceExpr.class).forEach(methodReference -> {
            // コンストラクタ参照の場合
            if (methodReference.getIdentifier().equals("new")) {
                // String::new など
                System.out.println("Constructor reference found: " + methodReference);
            } else {
                ResolvedMethodDeclaration resolvedMethod = methodReference.resolve();
                if (!GlobalState.useLibrary(resolvedMethod.getQualifiedSignature(), true)) {
                    references.add(new ReferenceNode(methodReference.toString(), resolvedMethod.getQualifiedSignature()));
                }
            }
        });


        md.findAll(ObjectCreationExpr.class).forEach(creationExpr -> {
            ResolvedConstructorDeclaration resolvedConstructorDeclaration = creationExpr.resolve();
            System.out.println("      - Instance creation: " + creationExpr.getTypeAsString() + " ----- " + resolvedConstructorDeclaration.getQualifiedSignature());
        });

        // インスタンスメソッドの呼び出しを抽出
        md.findAll(MethodCallExpr.class).forEach(methodCall -> {

            ResolvedMethodDeclaration resolvedMethod = methodCall.resolve();
            methodCall.getScope().ifPresent(scope -> {
                if (!GlobalState.useLibrary(resolvedMethod.getQualifiedSignature(), true)) {
                    references.add(new ReferenceNode(methodCall.toString(), resolvedMethod.getQualifiedSignature()));
                }
            });
        });

        // staticメソッドの呼び出しを抽出
        md.findAll(MethodCallExpr.class).forEach(methodCall -> {
            ResolvedMethodDeclaration resolvedMethod = methodCall.resolve();
            methodCall.getScope().ifPresent(scope -> {
                if (scope instanceof NameExpr) { // クラス名を示す場合、静的メソッド呼び出し

                    if (!GlobalState.useLibrary(resolvedMethod.getQualifiedSignature(), true)) {
                        references.add(new ReferenceNode(methodCall.toString(), resolvedMethod.getQualifiedSignature()));
                    }
                }
            });
        });
    }


    public String getSignature() {
        return this.resolved.getQualifiedSignature();
    }

    public String getBody() {
        return body;
    }

    public String getModifiers() {
        return modifiers;
    }

    public List<ReferenceNode> getReferences() {
        return references;
    }

}
