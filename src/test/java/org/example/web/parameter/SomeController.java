package org.example.web.parameter;

import org.example.annotation.LuisBody;
import org.example.annotation.LuisPathVariable;
import org.example.annotation.LuisRequestParam;

public class SomeController {

    public String methodWithBody(@LuisBody SomeData body){
        return "added";
    }

    public String withMultipleParams(@LuisBody SomeData body, @LuisPathVariable("key") String path, @LuisRequestParam("paramName") String param){
        return body.getKey() + path + param;
    }

    public String methodWithPathVariable(@LuisPathVariable("key") String path){
        return path;
    }

    public String methodWithTwoPathVariables(@LuisPathVariable("key") String key, @LuisPathVariable("lock") String lock){
        return "key: " + key + " lock: " + lock;
    }

    public String methodWithRequestParam(@LuisRequestParam("paramName") String param){
        return param;
    }

    public String methodWithTwoRequestParam(
            @LuisRequestParam("paramName") String param,
            @LuisRequestParam("anotherParam") String another
    ){
        return param + another;
    }

    public String methodWithNoParams(){
        return "added";
    }

    public static class SomeData {
        private String key;

        public String getKey() {
            return key;
        }

        public SomeData setKey(String key) {
            this.key = key;
            return this;
        }
    }
}


