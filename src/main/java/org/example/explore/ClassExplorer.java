package org.example.explore;

import org.example.util.LuisLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ClassExplorer {

    public static List<String> retrieveAllClasses(Class<?> sourceClass){
        return packageExplorer(sourceClass.getPackageName());
    }

    private static List<String> packageExplorer(String packageName) {
        List<String> classNames = new ArrayList<>();
        try {
            String packageDirectory = packageName.replace(".", File.separator);
            InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(packageDirectory);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String row;
            while((row = reader.readLine()) != null){
                if(row.endsWith(".class")){
                    String classNameWithoutExtension = packageName + "." + row.substring(0, row.indexOf(".class"));
                    classNames.add(classNameWithoutExtension);
                } else {
                    classNames.addAll(packageExplorer(packageName+"."+row));
                }
            }
            return classNames;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
