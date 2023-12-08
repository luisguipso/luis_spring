package org.example.metadata;

import org.example.explore.ClassExplorer;

import java.util.List;

public class MetadataExtractorImpl implements MetadataExtractor {
    @Override
    public void extractMetadata(Class<?> sourceClass) {
        List<String> allClasses = getAllClasses(sourceClass);
        MethodMetadataExtractor methodExtractor = new MethodMetadataExtractor();
        ClassMetadataExtractor classMetadataExtractor = new ClassMetadataExtractor(methodExtractor);
        classMetadataExtractor.extractMetadata(allClasses);
    }

    private static List<String> getAllClasses(Class<?> sourceClass) {
        return ClassExplorer.retrieveAllClasses(sourceClass);
    }
}
