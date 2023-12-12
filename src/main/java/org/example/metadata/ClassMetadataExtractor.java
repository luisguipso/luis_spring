package org.example.metadata;

import org.example.annotation.LuisController;
import org.example.datastructures.ServiceImplementationMap;
import org.example.util.LuisLogger;

import java.lang.annotation.Annotation;
import java.util.List;

public class ClassMetadataExtractor {

    private final MethodMetadataExtractor methodExtractor;

    public ClassMetadataExtractor(MethodMetadataExtractor methodExtractor){
        this.methodExtractor = methodExtractor;
    }

    public void extractMetadata(List<String> allClasses) {
        try {
            for (String className : allClasses){
                for (Annotation classAnnotation : Class.forName(className).getAnnotations()){
                    if(classAnnotation instanceof LuisController){
                        LuisLogger.log(ClassMetadataExtractor.class, "Found a Controller: " + className);
                        methodExtractor.extractMetadata(className);
                    } else {
                        LuisLogger.log(ClassMetadataExtractor.class, "Found a Component: " + className);
                        for (Class<?> interfacee : Class.forName(className).getInterfaces()){
                            ServiceImplementationMap.implementations.put(interfacee.getName(), className);
                        }
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}


