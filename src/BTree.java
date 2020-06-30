import java.util.*;

//Normally I would document this kind of thing, but
//it was just an exploratory project to get a better practical understanding of b-trees.
//Plan:
//(1) make BNode type parameterized.
//(2) make BTree extends AbstractSet.
//(3) make a Map class.
public class BTree<E extends Comparable<E>> extends AbstractSet<E>{

    private BNode root = null;
    private int size = 0;
    public static void main(String[] args) {
        BTree<Integer> t = new BTree(3);
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
            System.out.print(i.next() + " ");
        }
    }

    public BTree(int b) {
        root = new BNode(b);
    }

    public String toString() {
        return "" + root;
    }

    //@Override
    public boolean add(E value) {
        BNode n = search(value);
        int x = n.indexOf(value);
        if(x >= 0) return false;
        n.insert(value);
        root = n.split();
        size++;
        return true;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private Stack<BNode<E>> stack;
            private Stack<Integer> index;
            private E current = null;
            private boolean initialized = false;

            private void initialize() {
                stack = new Stack<BNode<E>>(); //clear the stack.
                index = new Stack<Integer>();
                if(current == null) {
                    BNode<E> n = root;
                    while(n != null) {
                        index.push(-1);
                        stack.push(n);
                        n = n.children.size() > 0 ? n.children.get(0) : null;
                    }
                    current = stack.peek().keys.get(0);
                } else {
                    BNode<E> found = search(current);
                    Stack<BNode<E>> ustack = new Stack<BNode<E>>();
                    Stack<Integer> uindex = new Stack<Integer>();
                    ustack.push(found);
                    uindex.push(found.indexOf(current));
                    found = found.parent;
                    while(found != null) {
                        ustack.push(found);
                        uindex.push(found.indexOf(ustack.peek()));
                        found = found.parent;
                    }
                    while(! ustack.isEmpty()) {
                        stack.push(ustack.pop());
                        index.push(uindex.pop());
                    }
                }
                initialized = true;
                System.out.println("\\infty(i)> " + current);
            }

            private void advanceCursor() {
                if(index.isEmpty()) return; //nothing left.
                if(index.peek() > stack.peek().keys.size())
                {
                    System.out.println("1(a)>U");
                    while(index.peek() > stack.peek().keys.size()) {
                        index.pop();
                        stack.pop();
                    }
                    //need to look at key in internal node.
                    int i = index.pop();
                    index.push(i++);
                    current = stack.peek().keys.get(i);
                } else if(stack.peek().children.size() > 0) {  //last thing looked at was internal value.
                    System.out.println("1(a)>D");
                    BNode<E> n = stack.peek();
                    while(n != null) {
                        index.push(-1);
                        stack.push(n);
                        n = n.children.get(0);
                    }
                    index.pop();
                    index.push(0);
                    current = stack.peek().keys.get(0);
                } else {
                    System.out.println("1(a)>R");
                    int i = index.pop();
                    System.out.println("1(a)>R " + i);
                    index.push(++i);
                    System.out.println("1(a)>R " + i);
                    current = stack.peek().keys.get(i);
                }
            }

            @Override
            public boolean hasNext() {
                System.out.println("1(h)> " + current);
                if(!initialized) {
                    initialize();
                } else if(current == null) {
                    advanceCursor();
                }
                System.out.println("\\infty(h)> " + current);
                return current != null;
            }

            @Override
            public E next() {
                System.out.println("1(n)> " + current);
                if(hasNext()) {
                    System.out.println("2(n)> " + current);
                    E answer = current;
                    current = null; //set up hasNext().
                    return answer;
                }
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {

            }
        };
    }

    public int size() {
        return size;
    }

    @Override
    public boolean remove(Object key) throws ClassCastException{
        @SuppressWarnings("unchecked")
        E value = (E) key;
        BNode<E> n = search(value);
        int x = n.indexOf(value);
        if(x < 0) return false; //value already deleted.
        size--; //now definitely going to delete.
        if(n.children.isEmpty()) {
            n.delete(value);
            n.rebalance();
            root = n.condense();
        } else {
            E scapegoat = n.children.get(x).highKey();
            //System.out.println(scapegoat);
            remove(scapegoat);  //technically recursion, but limited to one call.
            BNode<E> valueDestinationNode = search(value);
            int valueDestinationPosition = valueDestinationNode.indexOf(value);
            valueDestinationNode.keys.set(valueDestinationPosition, scapegoat);
        }
        return true;
    }

    private BNode<E> search(E value) {
        BNode<E> n = root;
        while(!n.children.isEmpty()) {
            BNode oldValue = n;
            for(int i = 0; i < n.keys.size(); i++) {
                if(n.keys.get(i).compareTo(value) == 0) return n;
                if(n.keys.get(i).compareTo(value) > 0) {
                    n = n.children.get(i);
                    break;
                }
            }
            if(n == oldValue) n = n.children.get(n.children.size() - 1);
        }
        return n;
    }

    private static class BNode<E extends Comparable<E>> {
        private ArrayList<E> keys;
        private ArrayList<BNode<E>> children;
        private BNode<E> parent;
        private int b;
        private int min;

        public BNode(int b) {
            this.b = b;
            min = (int)(Math.ceil((b+1)/2.0)-1);
            keys = new ArrayList<E>();
            children = new ArrayList<BNode<E>>();
            parent = null;
        }

        public void insert(E key) {
            if(keys.size() == b + 1) throw new IllegalStateException("Number of keys exceeds b (= " + b + ")");
            if(children.size() > 0) throw new IllegalStateException("Insertion into non-leaf node");
            int i = 0;
            for(; i < keys.size(); i++) {
                if(keys.get(i).compareTo(key) < 0){//keys.get(i) < key) {
                    continue;
                } else break;
            }
            keys.add(i, key);
        }

        public void delete(E key) {
            if(keys.size() < min) throw new IllegalStateException("Number of keys is less than min (= " + min + ")");
            if(children.size() > 0) throw new IllegalStateException("Insertion into non-leaf node");
            for(int i = 0; i < keys.size(); i++) {
                if(keys.get(i).compareTo(key) == 0) {//keys.get(i) == key)
                    keys.remove(i);
                }
            }
        }

        public E highKey() {
            BNode<E> n = this;
            while(! n.children.isEmpty()) {
                n = n.children.get(n.children.size() - 1);
            }
            return n.keys.get(n.keys.size() - 1);
        }

        public int indexOf(E value) {
            for(int i = 0; i < keys.size(); i++) {
                if(keys.get(i).compareTo(value) == 0) return i;
            }
            return -1;
        }

        public int indexOf(BNode<E> child) {
            return children.indexOf(child);
        }

        public void split(int index) {
            BNode<E> toSplit = children.get(index);
            int midPoint = toSplit.keys.size() / 2;
            E mid = toSplit.keys.get(midPoint);
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
            BNode<E> right = children.get(leftIndex + 1);
            BNode<E> left = children.get(leftIndex);
            //System.out.println("1>>L,R" + left + " " + right);
            E key = keys.get(leftIndex);
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
                keys.set(leftIndex, right.keys.get(0));
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
            children.remove(leftIndex+1); //merged into left node.
            keys.remove(leftIndex);
        }

        public BNode<E> condense() {
            BNode<E> n = this;
            //System.out.println("1>" + n);
            while(n.parent != null) {
                if(n.keys.size() < min) {
                    //System.out.println("2>" + n);
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
                n = n.parent;
            }
            //parent is null, may need some stuff here too.
            if(n.children.size() == 1) return n.children.get(0);
            return n;
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

        public void rebalance() {
            //code not yet here...
        }
    }


}