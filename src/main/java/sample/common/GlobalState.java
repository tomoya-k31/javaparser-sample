package sample.common;

import java.util.List;

public class GlobalState {

    private static volatile List<String> usedLibraries;

    public static void init(List<String> data) {
        if (usedLibraries == null) {
            synchronized (GlobalState.class) {
                if (usedLibraries == null) {
                    usedLibraries = data; // 初期化
                } else {
                    throw new IllegalStateException("State has already been initialized.");
                }
            }
        }
    }

    public static boolean useLibrary(String library) {
        return useLibrary(library, false);
    }

    public static boolean useLibrary(String library, boolean isUseStandardLibrary) {
        if (usedLibraries == null)
            throw new IllegalStateException("State has not been initialized yet.");

        if (isUseStandardLibrary && (library.startsWith("java.") || library.startsWith("javax.")))
            return true;

        return usedLibraries.stream().anyMatch(library::startsWith);
    }
}