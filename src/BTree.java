import java.util.ArrayList;

public class BTree {

    private BNode root = null;
    public static void main(String[] args) {
        BTree t = new BTree(3);
        for(int i = 5; i < 20; i++) {
            t.insert(i);
            System.out.println("\\infty>" + t);
        }
        t.insert(4);
        t.delete(18);
        System.out.println("\\infty>" + t);
        t.delete(7);
        System.out.println("\\infty>" + t);
        t.delete(5);
        System.out.println("\\infty>" + t);
        t.delete(4);
        System.out.println("\\infty>" + t);
        t.delete(6);
        System.out.println("\\infty>" + t);
        t.delete(8);
        System.out.println("\\infty>" + t);
        t.delete(11);
        System.out.println("\\infty>" + t);
        t.delete(10);
        System.out.println("\\infty>" + t);
    }

    public BTree(int b) {
        root = new BNode(b);
    }

    public String toString() {
        return "" + root;
    }

    public void insert(int value) {
        BNode n = search(value);
        int x = n.indexOf(value);
        if(x >= 0) return; //value already inserted.
        n.insert(value);
        root = n.split();
    }

    public void delete(int value) {
        BNode n = search(value);
        int x = n.indexOf(value);
        if(x < 0) return; //value already deleted.
        if(n.children.isEmpty()) {
            n.delete(value);
            n.rebalance();
            root = n.condense();
        } else {
            int scapegoat = n.children.get(x).highKey();
            //System.out.println(scapegoat);
            delete(scapegoat);  //technically recursion, but limited to one call.
            BNode valueDestinationNode = search(value);
            int valueDestinationPosition = valueDestinationNode.indexOf(value);
            valueDestinationNode.keys.set(valueDestinationPosition, scapegoat);
        }
    }

    private BNode search(int value) {
        BNode n = root;
        while(!n.children.isEmpty()) {
            BNode oldValue = n;
            for(int i = 0; i < n.keys.size(); i++) {
                if(n.keys.get(i) == value) return n;
                if(n.keys.get(i) > value) {
                    n = n.children.get(i);
                    break;
                }
            }
            if(n == oldValue) n = n.children.get(n.children.size() - 1);
        }
        return n;
    }

    private static class BNode {
        private ArrayList<Integer> keys;
        private ArrayList<BNode> children;
        private BNode parent;
        private int b;
        private int min;

        public BNode(int b) {
            this.b = b;
            min = (int)(Math.ceil((b+1)/2.0)-1);
            keys = new ArrayList<Integer>();
            children = new ArrayList<BNode>();
            parent = null;
        }

        public void insert(int key) {
            if(keys.size() == b + 1) throw new IllegalStateException("Number of keys exceeds b (= " + b + ")");
            if(children.size() > 0) throw new IllegalStateException("Insertion into non-leaf node");
            int i = 0;
            for(; i < keys.size(); i++) {
                if(keys.get(i) < key) {
                    continue;
                } else break;
            }
            keys.add(i, key);
        }

        public void delete(int key) {
            if(keys.size() < min) throw new IllegalStateException("Number of keys is less than min (= " + min + ")");
            if(children.size() > 0) throw new IllegalStateException("Insertion into non-leaf node");
            for(int i = 0; i < keys.size(); i++) {
                if(keys.get(i) == key) keys.remove(i);
            }
        }

        public int highKey() {
            BNode n = this;
            while(! n.children.isEmpty()) {
                n = n.children.get(n.children.size() - 1);
            }
            return n.keys.get(n.keys.size() - 1);
        }

        public int indexOf(int value) {
            for(int i = 0; i < keys.size(); i++) {
                if(keys.get(i) == value) return i;
            }
            return -1;
        }

        public void split(int index) {
            BNode toSplit = children.get(index);
            int midPoint = toSplit.keys.size() / 2;
            int mid = toSplit.keys.get(midPoint);
            //System.out.println("6>" + mid);
            BNode left = new BNode(b), right = new BNode(b);
            left.keys.addAll(toSplit.keys.subList(0, midPoint));
            left.parent = this;
            right.keys.addAll(toSplit.keys.subList(midPoint + 1, toSplit.keys.size()));
            right.parent = this;
            if (!toSplit.children.isEmpty()) {
                left.children.addAll(toSplit.children.subList(0, midPoint + 1));
                for (BNode b : left.children) b.parent = left;
                right.children.addAll(toSplit.children.subList(midPoint + 1, toSplit.children.size()));
                for (BNode b : right.children) b.parent = right;
            }
            keys.add(index, mid);
            children.remove(index);
            children.add(index, right);
            children.add(index, left);
        }

        //right = true, left = false.
        public void rotate(int leftIndex, boolean direction) {
            BNode right = children.get(leftIndex + 1);
            BNode left = children.get(leftIndex);
            //System.out.println("1>>L,R" + left + " " + right);
            int key = keys.get(leftIndex);
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
            BNode replacement = children.get(leftIndex);
            replacement.keys.add(keys.get(leftIndex));
            replacement.keys.addAll(children.get(leftIndex+1).keys);
            replacement.children.addAll(children.get(leftIndex+1).children);
            children.remove(leftIndex+1); //merged into left node.
            keys.remove(leftIndex);
        }

        public BNode condense() {
            BNode n = this;
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

        public BNode split() {
            BNode n = this;
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