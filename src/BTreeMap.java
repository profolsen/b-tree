import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public class BTreeMap<K extends Comparable<K>, V> extends AbstractMap<K, V> {
    private BTreeSet<Pair<K, V>> tree;

    public BTreeMap(int b, boolean debug) {
        tree = new BTreeSet<Pair<K, V>>(b, debug);
    }

    public BTreeMap() {
        tree = new BTreeSet<Pair<K, V>>();
    }

    @Override
    public V get(Object k) {
        K key = (K)k;
        Pair<K, V> x = new Pair<K, V>();
        x.key = key;
        Pair<K, V> answer = tree.lookup(x);
        return answer == null ? null : answer.value;

    }

    @Override
    public boolean containsKey(Object k) {
        K key = (K)k;
        return get(key) != null;
    }

    @Override
    public V remove(Object k) {
        K key = (K)k;
        Pair<K, V> x = new Pair<K, V>();
        x.key = key;
        V answer = tree.lookup(x).value;
        tree.remove(x);
        return answer;
    }

    @Override
    public V put(K key, V value) {
        V old = get(key);
        Pair<K, V> p = new Pair<K, V>();
        p.key = key;
        p.value = value;
        return old;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return (Set)tree;
    }


    private static class Pair<K extends Comparable<K>, V> implements Comparable<Pair<K, V>>, Map.Entry<K, V>{

        private K key;
        private V value;

        @Override
        public int compareTo(Pair<K, V> o) {
            return key.compareTo(o.key);
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V old = value;
            this.value = value;
            return value;
        }
    }
}
