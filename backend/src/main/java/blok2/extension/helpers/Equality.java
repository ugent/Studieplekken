package blok2.extension.helpers;

import java.util.HashSet;
import java.util.List;

public class Equality {

    /**
     * Since certain entity objects have List attributes (e.g. the assignedTags of Location),
     * and the TestSharedMethods class populates this attribute with a ArrayList while the JPA
     * entity manager populates this attribute using the PersistentBag class, the equals on this
     * attribute will fail. However, since both classes (ArrayList and PersistentBag) implement
     * the List interface, they can both be used in the constructor of a HashSet on which the
     * equals method can be called, effectively comparing for equal content (without having to
     * worry about the order of the collections).
     *
     * Source: https://stackoverflow.com/a/1075699/9356123
     */
    public static <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2) {
        return new HashSet<>(list1).equals(new HashSet<>(list2));
    }

}
