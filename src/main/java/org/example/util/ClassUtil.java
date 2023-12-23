package org.example.util;

public class ClassUtil {
    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Classe: %s não encontrada.", className), e);
        }
    }
}
