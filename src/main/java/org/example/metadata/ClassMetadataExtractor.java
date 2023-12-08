package org.example.metadata;

import org.example.util.LuisLogger;
import org.example.web.LuisSpringApplication;

import java.lang.annotation.Annotation;
import java.util.List;

public class ClassMetadataExtractor {

    private MethodMetadataExtractor methodExtractor;

    public ClassMetadataExtractor(MethodMetadataExtractor methodExtractor){
        this.methodExtractor = methodExtractor;
    }

    public void extractMetadata(List<String> allClasses) {
        try {
            for (String className : allClasses){
                for (Annotation classAnnotation : Class.forName(className).getAnnotations()){
                    if(classAnnotation.annotationType().getSimpleName().equals("LuisController")){
                        LuisLogger.log(LuisSpringApplication.class, "Found a Controller: " + className);
                        methodExtractor.extractMetadata(className);
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}


