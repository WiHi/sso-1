package com.xjbg.sso.core.util;

import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author kesc
 * @since 2018/1/27
 */

public final class CollectionUtil {

    /**
     * Return {@code true} if the supplied Collection is {@code null} or empty.
     * Otherwise, return {@code false}.
     *
     * @param collection the Collection to check
     * @return whether the given Collection is empty
     */
    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * Return {@code true} if the supplied Map is {@code null} or empty.
     * Otherwise, return {@code false}.
     *
     * @param map the Map to check
     * @return whether the given Map is empty
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static <T> boolean isEmpty(T[] array) {
        return (array == null || array.length == 0);
    }

    public static <T> boolean isNotEmpty(T[] array) {
        return !isEmpty(array);
    }

    /**
     * Convert the supplied array into a List. A primitive array gets converted
     * into a List of the appropriate wrapper type.
     * <p><b>NOTE:</b> Generally prefer the standard {@link Arrays#asList} method.
     * This {@code arrayToList} method is just meant to deal with an incoming Object
     * value that might be an {@code Object[]} or a primitive array at runtime.
     * <p>A {@code null} source value will be converted to an empty List.
     *
     * @param source the (potentially primitive) array
     * @return the converted List result
     * @see ObjectUtils#toObjectArray(Object)
     * @see Arrays#asList(Object[])
     */
    @SuppressWarnings("rawtypes")
    public static List arrayToList(Object source) {
        return Arrays.asList(ObjectUtils.toObjectArray(source));
    }

    /**
     * Merge the given Properties instance into the given Map,
     * copying all properties (key-value pairs) over.
     * <p>Uses {@code Properties.propertyNames()} to even catch
     * default properties linked into the original Properties instance.
     *
     * @param props the Properties instance to merge (may be {@code null})
     * @param map   the target Map to merge the properties into
     */
    @SuppressWarnings("unchecked")
    public static <K, V> void mergePropertiesIntoMap(Properties props, Map<K, V> map) {
        if (map == null) {
            throw new IllegalArgumentException("Map must not be null");
        }
        if (props != null) {
            for (Enumeration<?> en = props.propertyNames(); en.hasMoreElements(); ) {
                String key = (String) en.nextElement();
                Object value = props.getProperty(key);
                if (value == null) {
                    // Potentially a non-String value...
                    value = props.get(key);
                }
                map.put((K) key, (V) value);
            }
        }
    }


    /**
     * Check whether the given Iterator contains the given element.
     *
     * @param iterator the Iterator to check
     * @param element  the element to look for
     * @return {@code true} if found, {@code false} else
     */
    public static boolean contains(Iterator<?> iterator, Object element) {
        if (iterator != null) {
            while (iterator.hasNext()) {
                Object candidate = iterator.next();
                if (ObjectUtils.nullSafeEquals(candidate, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check whether the given Enumeration contains the given element.
     *
     * @param enumeration the Enumeration to check
     * @param element     the element to look for
     * @return {@code true} if found, {@code false} else
     */
    public static boolean contains(Enumeration<?> enumeration, Object element) {
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                Object candidate = enumeration.nextElement();
                if (ObjectUtils.nullSafeEquals(candidate, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check whether the given Collection contains the given element instance.
     * <p>Enforces the given instance to be present, rather than returning
     * {@code true} for an equal element as well.
     *
     * @param collection the Collection to check
     * @param element    the element to look for
     * @return {@code true} if found, {@code false} else
     */
    public static boolean containsInstance(Collection<?> collection, Object element) {
        if (collection != null) {
            for (Object candidate : collection) {
                if (candidate == element) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return {@code true} if any element in '{@code candidates}' is
     * contained in '{@code source}'; otherwise returns {@code false}.
     *
     * @param source     the source Collection
     * @param candidates the candidates to likeSearch for
     * @return whether any of the candidates has been found
     */
    public static boolean containsAny(Collection<?> source, Collection<?> candidates) {
        if (isEmpty(source) || isEmpty(candidates)) {
            return false;
        }
        for (Object candidate : candidates) {
            if (source.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the first element in '{@code candidates}' that is contained in
     * '{@code source}'. If no element in '{@code candidates}' is present in
     * '{@code source}' returns {@code null}. Iteration order is
     * {@link Collection} implementation specific.
     *
     * @param source     the source Collection
     * @param candidates the candidates to likeSearch for
     * @return the first present object, or {@code null} if not found
     */
    @SuppressWarnings("unchecked")
    public static <E> E findFirstMatch(Collection<?> source, Collection<E> candidates) {
        if (isEmpty(source) || isEmpty(candidates)) {
            return null;
        }
        for (Object candidate : candidates) {
            if (source.contains(candidate)) {
                return (E) candidate;
            }
        }
        return null;
    }

    /**
     * Find a single value of the given type in the given Collection.
     *
     * @param collection the Collection to likeSearch
     * @param type       the type to look for
     * @return a value of the given type found if there is a clear match,
     * or {@code null} if none or more than one such value found
     */
    @SuppressWarnings("unchecked")
    public static <T> T findValueOfType(Collection<?> collection, Class<T> type) {
        if (isEmpty(collection)) {
            return null;
        }
        T value = null;
        for (Object element : collection) {
            if (type == null || type.isInstance(element)) {
                if (value != null) {
                    // More than one value found... no clear single value.
                    return null;
                }
                value = (T) element;
            }
        }
        return value;
    }

    /**
     * Find a single value of one of the given types in the given Collection:
     * searching the Collection for a value of the first type, then
     * searching for a value of the second type, etc.
     *
     * @param collection the collection to likeSearch
     * @param types      the types to look for, in prioritized order
     * @return a value of one of the given types found if there is a clear match,
     * or {@code null} if none or more than one such value found
     */
    public static Object findValueOfType(Collection<?> collection, Class<?>[] types) {
        if (isEmpty(collection) || ObjectUtils.isEmpty(types)) {
            return null;
        }
        for (Class<?> type : types) {
            Object value = findValueOfType(collection, type);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * Determine whether the given Collection only contains a single unique object.
     *
     * @param collection the Collection to check
     * @return {@code true} if the collection contains a single reference or
     * multiple references to the same instance, {@code false} else
     */
    public static boolean hasUniqueObject(Collection<?> collection) {
        if (isEmpty(collection)) {
            return false;
        }
        boolean hasCandidate = false;
        Object candidate = null;
        for (Object elem : collection) {
            if (!hasCandidate) {
                hasCandidate = true;
                candidate = elem;
            } else if (candidate != elem) {
                return false;
            }
        }
        return true;
    }

    /**
     * Find the common element type of the given Collection, if any.
     *
     * @param collection the Collection to check
     * @return the common element type, or {@code null} if no clear
     * common type has been found (or the collection was empty)
     */
    public static Class<?> findCommonElementType(Collection<?> collection) {
        if (isEmpty(collection)) {
            return null;
        }
        Class<?> candidate = null;
        for (Object val : collection) {
            if (val != null) {
                if (candidate == null) {
                    candidate = val.getClass();
                } else if (candidate != val.getClass()) {
                    return null;
                }
            }
        }
        return candidate;
    }

    /**
     * Marshal the elements from the given enumeration into an array of the given type.
     * Enumeration elements must be assignable to the type of the given array. The array
     * returned will be a different instance than the array given.
     */
    public static <A, E extends A> A[] toArray(Enumeration<E> enumeration, A[] array) {
        ArrayList<A> elements = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            elements.add(enumeration.nextElement());
        }
        return elements.toArray(array);
    }

    public static <K, E> Map<K, E> toMap(Collection<E> elements, Function<E, K> keyBuilder) {
        if (isEmpty(elements) || keyBuilder == null) {
            return Collections.emptyMap();
        }
        Map<K, E> result = new HashMap<>(elements.size() * 3 / 2);
        for (E element : elements) {
            result.put(keyBuilder.apply(element), element);
        }
        return result;
    }

    public static <E, K> Map<K, List<E>> toMapList(Collection<E> elements, Function<E, K> keyFunc) {
        return toMapList(elements, keyFunc, Function.identity());
    }

    public static <E, K, V> Map<K, List<V>> toMapList(Collection<E> elements, Function<E, K> keyFunc, Function<E, V> valueFunc) {
        if (isEmpty(elements) || keyFunc == null || valueFunc == null) {
            return Collections.emptyMap();
        }
        Map<K, List<V>> result = new HashMap<>(elements.size() * 3 / 2);
        for (E element : elements) {
            K key = keyFunc.apply(element);
            List<V> values = result.computeIfAbsent(key, k -> new ArrayList<>());
            values.add(valueFunc.apply(element));
        }
        return result;
    }

    public static <E> void forEach(Collection<E> collection, Consumer<? super E> action) {
        if (isEmpty(collection) || action == null) {
            return;
        }
        collection.forEach(action);
    }

    public static <E> List<E> filter(Collection<E> source, Predicate<E> predicate) {
        if (isEmpty(source) || predicate == null) {
            return Collections.emptyList();
        }
        List<E> result = new ArrayList<>();
        for (E element : source) {
            if (predicate.test(element)) {
                result.add(element);
            }
        }
        return result;
    }


    public static <F, T> List<T> selectList(Collection<F> source, Function<F, T> func) {
        return selectList(source, func, true);
    }

    public static <F, T> List<T> selectNotNullList(Collection<F> source, Function<F, T> func) {
        return selectList(source, func, false);
    }

    public static <F, T> List<T> selectList(Collection<F> source, Function<F, T> func, boolean isAllowNullValue) {
        if (isEmpty(source)) {
            return Collections.emptyList();
        }
        List<T> resultList = new ArrayList<>(source.size());
        for (F f : source) {
            T t = func.apply(f);
            if (t != null || isAllowNullValue) {
                resultList.add(t);
            }
        }
        return resultList;
    }

    public static <F, T> List<T> selectListWithIndex(Collection<F> source, BiFunction<F, Integer, T> func) {
        if (isEmpty(source)) {
            return Collections.emptyList();
        }
        List<T> resultList = new ArrayList<>(source.size());
        AtomicInteger index = new AtomicInteger();
        for (F f : source) {
            resultList.add(func.apply(f, index.getAndIncrement()));
        }
        return resultList;
    }
}
