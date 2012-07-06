/**
 * [JWorld@TW][Effective Java Reloaded] Static Factory的新用法 by ingramchen 
 * http://www.javaworld.com.tw/jute/post/view?bid=44&id=163570&sty=1&tpg=1&age=0
 */
package Utils;
import java.util.*;

public class Create {
	private Create() {}
	 
	public static <T> ArrayList<T> list(T... objs) {
	    ArrayList<T> collection = new ArrayList<T>();
	    Collections.addAll(collection, objs);
	    return collection;
	}
	
	public static <T> ArrayList<T> arrayList(T... objs) {
	    return list(objs);
	}
	 
	public static <T> LinkedList<T> linkedList(T... objs) {
	    LinkedList<T> collection = new LinkedList<T>();
	    Collections.addAll(collection, objs);
	    return collection;
	}
	 
	public static <T> HashSet<T> set(T... objs) {
	    HashSet<T> collection = new HashSet<T>();
	    Collections.addAll(collection, objs);
	    return collection;
	}
	 
	public static <T> HashSet<T> hashSet(T... objs) {
	    return set(objs);
	}
	 
	public static <T> TreeSet<T> treeSet(T... objs) {
	    TreeSet<T> collection = new TreeSet<T>();
	    Collections.addAll(collection, objs);
	    return collection;
	}
	 
	public static <T> LinkedHashSet<T> linkedHashSet(T... objs) {
	    LinkedHashSet<T> collection = new LinkedHashSet<T>();
	    Collections.addAll(collection, objs);
	    return collection;
	}
	 
	public static <K, V> HashMap<K, V> map() {
	    return hashMap();
	}
	 
	public static <K, V> HashMap<K, V> hashMap() {
	    return new HashMap<K, V>();
	}
	 
	public static <K, V> TreeMap<K, V> treeMap() {
	    return new TreeMap<K, V>();
	}
	 
	public static <K, V> LinkedHashMap<K, V> linkedHashMap() {
	    return new LinkedHashMap<K, V>();
	}
}
