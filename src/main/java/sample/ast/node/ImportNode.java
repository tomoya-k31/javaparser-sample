package sample.ast.node;

public class ImportNode {

    private final boolean isStatic;
    private final boolean isWildcard;
    private final String name;

    public ImportNode(String name) {
        this(name, false, false);
    }

    public ImportNode(String name, boolean isStatic, boolean isWildcard) {
        this.isStatic = isStatic;
        this.isWildcard = isWildcard;
        this.name = name;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isWildcard() {
        return isWildcard;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        if (isStatic && isWildcard) {
            return "import static " + name + ".*";
        } else if (isStatic) {
            return "import static " + name;
        } else if (isWildcard) {
            return "import " + name + ".*";
        } else {
            return "import " + name;
        }
    }
}
