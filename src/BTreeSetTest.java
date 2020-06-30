import java.util.Iterator;

public class BTreeSetTest {

    public static void main(String[] args) {
        BTreeSet<Integer> t = new BTreeSet(3, true);
        for(int i = 5; i < 20; i++) {
            t.add(i);
            System.out.println("\\infty>" + t);
        }
        t.add(4);
        t.remove(18);
        System.out.println("\\infty>" + t);
        t.remove(7);
        System.out.println("\\infty>" + t);
        t.remove(5);
        System.out.println("\\infty>" + t);
        t.remove(4);
        System.out.println("\\infty>" + t);
        t.remove(6);
        System.out.println("\\infty>" + t);
        t.remove(8);
        System.out.println("\\infty>" + t);
        t.remove(11);
        System.out.println("\\infty>" + t);
        t.remove(10);
        System.out.println("\\infty>" + t);
        Iterator<Integer> i = t.iterator();
        while(i.hasNext()) {
            int x = i.next();
            if(x == 15) i.remove();
            System.out.print(i.next() + " ");
        }
        System.out.println();
        System.out.println("\\infty>" + t);
        t.removeIf(x -> x % 2 == 0);
        System.out.println(t);
    }
}
