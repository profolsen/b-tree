import java.util.TreeMap;

public class BTreeMapTest {
    public static void main(String[] args) {
        BTreeMap<Integer, Integer> m = new BTreeMap<Integer, Integer>();
        for(int i = 0; i < 100; i++) {
            m.put((int)(Math.random()*1000000), i);
        }
        System.out.println(m);
    }
}
