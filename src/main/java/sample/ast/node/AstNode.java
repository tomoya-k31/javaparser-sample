package sample.ast.node;

import java.util.Optional;

public abstract class AstNode {

    private final String name;

    public AstNode(String name) {
        this.name = name;
    }

    /** Getter */
    public String getName() {
        return name;
    }

    public <T extends AstNode> Optional<T> getAstNode(Class<T> clazz) {
        return Optional.of(this)
                .filter(clazz::isInstance)
                .map(clazz::cast);
    }

}
