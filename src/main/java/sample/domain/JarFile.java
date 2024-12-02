package sample.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.File;

public class JarFile {

    private String library;
    private File path;

    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public File getPath() {
        return path;
    }

    public void setPath(File path) {
        this.path = path;
    }

    public boolean exists() {
        return path.exists() && path.isFile();
    }

    public boolean isOtherProject() {
        // :<library>:で囲まれている場合はfalseを返す
        if (library.startsWith(":") && library.endsWith(":"))
            return false;

        // <root>:<library>:の場合はfalseを返す
        if (library.contains(":") && library.endsWith(":"))
            return true;

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JarFile jarFile = (JarFile) o;
        return new EqualsBuilder().append(library, jarFile.library).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(library).toHashCode();
    }
}
