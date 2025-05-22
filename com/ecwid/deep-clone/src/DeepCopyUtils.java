
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class DeepCopyUtils {

    public static void main(String[] args) {
        List<String> favoriteBooks = List.of("B1", "B2", "B3");
        Man andrey = new Man("Andrey", 25, favoriteBooks);

        Man newAndrey = deepCopy(andrey);
        System.out.println(andrey);
        System.out.println(newAndrey);

        System.out.println();
        System.out.println("Is it new Andrey? " + (newAndrey != andrey));
        System.out.println("They are equals? " + (newAndrey.equals(andrey)));
        System.out.println(newAndrey.getFavoriteBooks() != andrey.getFavoriteBooks());

    }

    public static <T> T deepCopy(T original) {
        if (original == null) {
            return null;
        }

        try {
            T copy = (T) deepCopyInternal(original);
            return copy;
        } catch (Exception e) {
            throw new RuntimeException("Deep copy failed", e);
        }
    }

    private static Object deepCopyInternal(Object original) throws Exception {
        if (original == null) {
            return null;
        }

        Class<?> clazz = original.getClass();

        if (isImmutable(clazz)) {
            return original;
        }

        if (clazz.isArray()) {
            return copyArray(original);
        }

        if (original instanceof Collection) {
            return copyCollection((Collection<?>) original);
        }

        if (original instanceof Map) {
            return copyMap((Map<?, ?>) original);
        }

        return copyObject(original);
    }

    private static Object copyArray(Object array) throws Exception {
        int length = Array.getLength(array);
        Object copy = Array.newInstance(array.getClass().getComponentType(), length);

        for (int i = 0; i < length; i++) {
            Array.set(copy, i, deepCopyInternal(Array.get(array, i)));
        }

        return copy;
    }

    private static Collection<?> copyCollection(Collection<?> original) throws Exception {
        Collection<Object> copy = createCollectionInstance(original.getClass());

        for (Object item : original) {
            copy.add(deepCopyInternal(item));
        }

        return copy;
    }

    private static Map<?, ?> copyMap(Map<?, ?> original) throws Exception {
        Map<Object, Object> copy = createMapInstance(original.getClass());

        for (Map.Entry<?, ?> entry : original.entrySet()) {
            copy.put(deepCopyInternal(entry.getKey()), deepCopyInternal(entry.getValue()));
        }

        return copy;
    }

    private static Object copyObject(Object original) throws Exception {
        Class<?> clazz = original.getClass();
        Object copy = createInstance(clazz);

        for (Field field : getAllFields(clazz)) {
            field.setAccessible(true);
            Object value = field.get(original);
            field.set(copy, deepCopyInternal(value));
        }

        return copy;
    }

    private static Collection<Object> createCollectionInstance(Class<?> collectionClass) throws Exception {
        if (List.class.isAssignableFrom(collectionClass)) {
            return new ArrayList<>();
        }
        if (Set.class.isAssignableFrom(collectionClass)) {
            return new HashSet<>();
        }
        if (Queue.class.isAssignableFrom(collectionClass)) {
            return new LinkedList<>();
        }
        return (Collection<Object>) collectionClass.getDeclaredConstructor().newInstance();
    }

    private static Map<Object, Object> createMapInstance(Class<?> mapClass) throws Exception {
        if (HashMap.class.isAssignableFrom(mapClass)) {
            return new HashMap<>();
        }
        if (LinkedHashMap.class.isAssignableFrom(mapClass)) {
            return new LinkedHashMap<>();
        }
        if (TreeMap.class.isAssignableFrom(mapClass)) {
            return new TreeMap<>();
        }
        return (Map<Object, Object>) mapClass.getDeclaredConstructor().newInstance();
    }

    private static Object createInstance(Class<?> clazz) throws Exception {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if (constructor.getParameterCount() > 0) {
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    Object[] params = new Object[paramTypes.length];
                    for (int i = 0; i < paramTypes.length; i++) {
                        params[i] = getDefaultValue(paramTypes[i]);
                    }
                    constructor.setAccessible(true);
                    return constructor.newInstance(params);
                }
            }
            throw e;
        }
    }

    private static Object getDefaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0d;
        return null;
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private static boolean isImmutable(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == String.class ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Double.class ||
                clazz == Float.class ||
                clazz == Boolean.class ||
                clazz == Character.class ||
                clazz == Byte.class ||
                clazz == Short.class ||
                clazz.isEnum();
    }
}

