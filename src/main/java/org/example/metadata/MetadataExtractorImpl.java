package org.example.metadata;

import java.util.List;

public class MetadataExtractorImpl implements MetadataExtractor {

    List<String> allClasses;
    public MetadataExtractorImpl(List<String> allClasses){
        this.allClasses = allClasses;
    }

    @Override
    public void extractMetadata() {
        MethodMetadataExtractor methodExtractor = new MethodMetadataExtractor();
        ClassMetadataExtractor classMetadataExtractor = new ClassMetadataExtractor(methodExtractor);
        classMetadataExtractor.extractMetadata(allClasses);
    }

}
