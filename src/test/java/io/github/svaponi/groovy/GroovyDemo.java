package io.github.svaponi.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.junit.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GroovyDemo {

    public static void main(final String[] args) throws IllegalAccessException, InstantiationException, IOException {

        // Create GroovyClassLoader.
        final GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

        final String script = "" +
                "class Script {\n" +
                "   Object apply(org.springframework.util.MultiValueMap params, Map body) {\n" +
                "       return \"Hey \" + params.getFirst(\"name\");\n" +
                "   }\n" +
                "}";

        System.out.println("Groovy script: \n" + script);

        GroovyObject groovyObj = (GroovyObject) groovyClassLoader.parseClass(script).newInstance();

        final MultiValueMap params = new LinkedMultiValueMap();
        params.add("name", "Sam");

        final Map body = new HashMap<>();
        body.put("id", 0);

        final Object result = groovyObj.invokeMethod("apply", new Object[]{params, body});

        System.out.println("Output: \n" + result);

        Assert.assertEquals("Hey Sam", result);
    }
}