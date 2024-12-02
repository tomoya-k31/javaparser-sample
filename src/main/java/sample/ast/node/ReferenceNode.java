package sample.ast.node;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReferenceNode extends AstNode {

    private final String signature;

    public ReferenceNode(String name, String signature) {
        super(name);
        this.signature = signature;
    }

    @JsonProperty("expr")
    @Override
    public String getName() {
        return super.getName();
    }

    public String getSignature() {
        return signature;
    }
}
