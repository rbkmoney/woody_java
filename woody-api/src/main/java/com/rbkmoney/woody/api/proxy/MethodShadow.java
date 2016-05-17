package com.rbkmoney.woody.api.proxy;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isPrivate;

/**
 * Created by vpankrashkin on 22.04.16.
 */
public class MethodShadow {

    public static Method[] getShadowedMethods(Class ifaceA, Collection<Class> ifacesB) {
        for (Class ifaceB : ifacesB) {
            Method[] shadowedMethods = getShadowedMethods(ifaceA, ifaceB);
            if (shadowedMethods.length != 0) {
                return shadowedMethods;
            }
        }
        return new Method[0];
    }

    public static Method[] getShadowedMethods(Class ifaceA, Class ifaceB) {
        Stream.of(ifaceA, ifaceB).forEach(iface -> checkInterface(iface, "Referred class is not an interface:"));

        return getOverlappingMethods(ifaceA.getMethods(), ifaceB.getMethods());
    }

    public static Method[] getShadowedMethods(Object object, Class iface) {
        checkInterface(iface, "Referred class is not an interface:");
        Method[] objMethods = Arrays.stream(object.getClass().getMethods()).filter(m -> {
            int mod = m.getModifiers();
            return !(isPrivate(mod));
        }).toArray(Method[]::new);

        return getOverlappingMethods(objMethods, iface.getMethods());

    }

    public static boolean isSameSignature(Method methodA, Method methodB) {
        return METHOD_COMPARATOR.compare(methodA, methodB) == 0;
    }

    public static Method[] getOverlappingMethods(Method[] aMethods, Method[] bMethods) {
        return Arrays.stream(aMethods)
                .filter(tm -> Arrays.stream(bMethods)
                        .filter(sm -> isSameSignature(tm, sm))
                        .findAny().isPresent())
                .toArray(Method[]::new);
    }

    public static Method getSameMethod(Method searchMethod, Class targetClass) {
        try {
            return targetClass.getMethod(searchMethod.getName(), searchMethod.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static void checkInterface(Class cl, String errorMessage) {
        if (!cl.isInterface()) {
            throw new IllegalArgumentException(errorMessage + cl.getName());
        }
    }

    public static final Comparator<Method> METHOD_COMPARATOR = (m1, m2) -> {

        int currResult = m1.getName().compareTo(m2.getName());
        if (currResult != 0) {
            return currResult;
        }
        currResult = m1.getParameterCount() - m2.getParameterCount();
        if (currResult != 0) {
            return currResult;
        }

        Class[] pt1 = m1.getParameterTypes();
        Class[] pt2 = m2.getParameterTypes();

        for (int i = 0; i < pt1.length; i++) {
            if (pt1[i] != pt2[i])
                return pt1[i].hashCode() - pt2[i].hashCode();
        }
        return 0;
    };
}
