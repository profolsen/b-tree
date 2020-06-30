import java.util.*;

//Normally I would document this kind of thing, but
//it was just an exploratory project to get a better practical understanding of b-trees.
//Plan:
//(1) make BNode type parameterized.
//(2) make BTree extends AbstractSet.
//(3) make a Map class.
public class BTree<E extends Comparable<E>> extends AbstractSet<E>{

    private BNode<E> root = null;
    private int size = 0;
    private boolean debug = false;

    public static void main(String[] args) {
        BTree<Integer> t = new BTree(3, true);
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

    public BTree() {
        this(100, false);
    }

    public BTree(int b, boolean debug) {
        root = new BNode(b);
        this.debug = debug;
    }

    public String toString() {
        if(debug) return "" + root + (root.keys.isEmpty() ? "()" : root.lowKey().string());
        else return super.toString();
    }

    //@Override
    public boolean add(E value) {
        BNode n = search(value);
        int x = n.indexOf(value);
        if(x >= 0) return false;
        n.insert(value);
        root = n.split();
        size++;
        root.verify();
        return true;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private DLNode<E> next = root.lowKey();
            private DLNode<E> current = null;
            boolean used = false;

            @Override
            public boolean hasNext() {
                if(used) {
                    if(next == null) return false;
                    next = next.next;
                    used = false;
                }
                if(next == null) used = true;
                return !used;
            }

            @Override
            public E next() {
                E answer = next.data;
                current = next;
                used = true;
                return answer;
            }

            @Override
            public void remove() {
                //System.out.println("1>" + current.data);
                BTree.this.remove(current.data);
                //System.out.println("2>" + BTree.this);
            }
        };
    }

    public int size() {
        return size;
    }

    @Override
    public boolean remove(Object key) throws ClassCastException{
        if(key instanceof Integer && ((int)(key)) == 16)
            key = key;
        @SuppressWarnings("unchecked")
        E value = (E) key;
        BNode<E> n = search(value);
        int x = n.indexOf(value);
        if(x < 0) return false; //value already deleted.
        size--; //now definitely going to delete.
        if(n.children.isEmpty()) {
            n.delete(value);
            root = n.rebalance();
        } else {
            DLNode<E> scapegoat = n.children.get(x).highKey();
            //System.out.println(scapegoat);
            remove(scapegoat.data);  //technically recursion, but limited to one call.
            BNode<E> valueDestinationNode = search(value);
            int valueDestinationPosition = valueDestinationNode.indexOf(value);
            valueDestinationNode.keys.get(valueDestinationPosition).data = scapegoat.data;
        }
        root.verify();
        return true;
    }

    private BNode<E> search(E value) {
        BNode<E> n = root;
        while(!n.children.isEmpty()) {
            BNode oldValue = n;
            for(int i = 0; i < n.keys.size(); i++) {
                if(n.keys.get(i).data.compareTo(value) == 0) return n;
                if(n.keys.get(i).data.compareTo(value) > 0) {
                    n = n.children.get(i);
                    break;
                }
            }
            if(n == oldValue) n = n.children.get(n.children.size() - 1);
        }
        return n;
    }

    private static class BNode<E extends Comparable<E>> {
        private ArrayList<DLNode<E>> keys;
        private ArrayList<BNode<E>> children;
        private BNode<E> parent;
        private int b;
        private int min;

        public BNode(int b) {
            this.b = b;
            min = (int)(Math.ceil((b+1)/2.0)-1);
            keys = new ArrayList<DLNode<E>>();
            children = new ArrayList<BNode<E>>();
            parent = null;
        }

        public void insert(E key) {
            if(keys.size() == b + 1) throw new IllegalStateException("Number of keys exceeds b (= " + b + ")");
            if(children.size() > 0) throw new IllegalStateException("Insertion into non-leaf node");
            int i = 0;
            for(; i < keys.size(); i++) {
                if(keys.get(i).data.compareTo(key) < 0){//keys.get(i) < key) {
                    continue;
                } else break;
            }
            DLNode<E> toInsert = i >= keys.size() ?
                    new DLNode<E>(keys.isEmpty() ? null : keys.get(i-1)) : keys.get(i).insertBefore();
            toInsert.data = key;
            keys.add(i, toInsert);
        }

        public void delete(E key) {
            if(keys.size() < min) throw new IllegalStateException("Number of keys is less than min (= " + min + ")");
            if(children.size() > 0) throw new IllegalStateException("Insertion into non-leaf node");
            for(int i = 0; i < keys.size(); i++) {
                if(keys.get(i).data.compareTo(key) == 0) {//keys.get(i) == key)
                    keys.get(i).delete();
                    keys.remove(i);
                }
            }
        }

        public DLNode<E> highKey() {
            BNode<E> n = this;
            while(! n.children.isEmpty()) {
                n = n.children.get(n.children.size() - 1);
            }
            return n.keys.get(n.keys.size() - 1);
        }

        public DLNode<E> lowKey() {
            BNode<E> n = this;
            while(! n.children.isEmpty()) {
                n = n.children.get(0);
            }
            return n.keys.get(0);
        }

        public int indexOf(E value) {
            for(int i = 0; i < keys.size(); i++) {
                if(keys.get(i).data.compareTo(value) == 0) return i;
            }
            return -1;
        }

        public int indexOf(BNode<E> child) {
            return children.indexOf(child);
        }

        public void split(int index) {
            BNode<E> toSplit = children.get(index);
            int midPoint = toSplit.keys.size() / 2;
            DLNode<E> mid = toSplit.keys.get(midPoint);
            //System.out.println("6>" + mid);
            BNode<E> left = new BNode(b), right = new BNode(b);
            left.keys.addAll(toSplit.keys.subList(0, midPoint));
            left.parent = this;
            right.keys.addAll(toSplit.keys.subList(midPoint + 1, toSplit.keys.size()));
            right.parent = this;
            if (!toSplit.children.isEmpty()) {
                left.children.addAll(toSplit.children.subList(0, midPoint + 1));
                for (BNode<E> b : left.children) b.parent = left;
                right.children.addAll(toSplit.children.subList(midPoint + 1, toSplit.children.size()));
                for (BNode<E> b : right.children) b.parent = right;
            }
            keys.add(index, mid);
            children.remove(index);
            children.add(index, right);
            children.add(index, left);
        }

        //right = true, left = false.
        public void rotate(int leftIndex, boolean direction) {
            //System.out.println("1(r)>" + this);
            BNode<E> right = children.get(leftIndex + 1);
            BNode<E> left = children.get(leftIndex);
            //System.out.println("1>>L,R" + left + " " + right);
            DLNode<E> key = keys.get(leftIndex);
            if(direction) { //right rotate
                keys.set(leftIndex, left.keys.get(left.keys.size() - 1));
                left.keys.remove(left.keys.size() - 1);
                right.keys.add(0, key);
                if(right.children.size() > 0) {
                    if(left.children.size() > 0) right.children.add(0, left.children.get(left.children.size() - 1));
                    right.children.get(0).parent = this;
                }
                if(left.children.size() > 0) left.children.remove(left.children.size() -1);
            } else { //rotate left
                //System.out.println("2(r)>L" + keys + " " + this);
                keys.set(leftIndex, right.keys.get(0));
                //System.out.println("2(r)>L" + keys + " " + this);
                right.keys.remove(0);
                left.keys.add(key);
                if(left.children.size() > 0) {
                    if(right.children.size() > 0) left.children.add(right.children.get(0));
                    left.children.get(left.children.size() - 1).parent = this;
                }
                if(right.children.size() > 0) right.children.remove(0);
            }
        }

        public void merge(int leftIndex) {
            //System.out.println("1>>" + this);
            BNode<E> replacement = children.get(leftIndex);
            replacement.keys.add(keys.get(leftIndex));
            replacement.keys.addAll(children.get(leftIndex+1).keys);
            replacement.children.addAll(children.get(leftIndex+1).children);
            for(BNode b : children.get(leftIndex+1).children) {
                b.parent = replacement;
            }
            children.remove(leftIndex+1); //merged into left node.
            keys.remove(leftIndex);
        }

        public BNode<E> rebalance() {
            BNode<E> n = this;
            //System.out.println("1>" + n);
            while(n.parent != null) {
                if(n.keys.size() < min) {
                    //System.out.println("2>(" + n.keys.size() + ", " + min + ")" + n);
                    int index = n.parent.children.indexOf(n);
                    if(index > 0 && n.parent.children.get(index - 1).keys.size() > min) { //can rotate right.
                        //System.out.println("3>R " + n.parent);
                        n.parent.rotate(index - 1, true);
                    } else if(index < n.parent.children.size() &&
                            n.parent.children.get(index + 1).keys.size() > min) { //can rotate left.
                        //System.out.println("3>L " + n.parent);
                        n.parent.rotate(index, false);
                    } else {
                        //System.out.println("3>" + n.parent);
                        if(index > 0) index--;
                        n.parent.merge(index);
                    }
                }
                //System.out.println("4>" + n + " to " + n.parent);
                n = n.parent;
            }
            //parent is null, may need some stuff here too.
            if(n.children.size() == 1) return n.children.get(0);
            return n;
        }

        public void verify() {
            if(children.size() == 0) return;
            for(BNode<E> b : children) {
                if(b.parent != this) throw new RuntimeException(b + "<<<>>>" + b.parent + "---" + this);
            }
        }

        public BNode<E> split() {
            BNode<E> n = this;
            //System.out.println("1>" + n);
            while(n.parent != null) {
                if(n.keys.size() > b) {
                    //System.out.println("1.5>" + n + " " + n.keys.size() + " " + b);
                    int splitIndex = n.parent.children.indexOf(n);
                    n.parent.split(splitIndex);
                }
                //System.out.println("2>" + n);
                n = n.parent;
            }
            //System.out.println("3>" + n);
            if(n.keys.size() > b) {
                n.parent = new BNode(b);
                n.parent.children.add(n);
                n.parent.split(0);
            }
            //System.out.println("4>" + n.parent);
            //System.out.println(n.parent);
            if(n.parent != null) return n.parent;
            return n;
        }

        public String toString() {
            String answer = "{";
            for(int i = 0; i < keys.size(); i++) {
                if(children.size() > 0) {
                    answer += children.get(i);
                }
                answer += " " + keys.get(i) + " ";
            }
            if(children.size() > 0) {
                answer += children.get(children.size() - 1);
            }
            return answer + "}";
        }
    }

    static class DLNode<E> {

        DLNode<E> prev = null, next = null;
        E data = null;

        public DLNode() {

        }

        public DLNode(E data) {
            this.data = data;
        }

        public DLNode(DLNode<E> p) {
            this.prev = p;
            if(p != null) p.next = this;
        }

        DLNode<E> insertBefore() {
            DLNode<E> toInsert = new DLNode<E>();
            toInsert.next = this;
            if(prev != null) {
                prev.next = toInsert;
            }
            toInsert.prev = prev;
            prev = toInsert;
            return toInsert;
        }

        void delete() {
            if(prev != null)
                prev.next = next;
            if(next != null)
                next.prev = prev;
        }

        public String toString() {  return "" + data;  }

        public String string() {
            String ans = "(";
            DLNode c = this;
            while(c != null) {
                //System.out.println(c);
                ans += c + " ";
                c = c.next;
            }
            return ans + ")";
        }
    }
}