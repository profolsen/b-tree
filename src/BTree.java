import java.util.ArrayList;

public class BTree {

    private BNode root = null;
    public static void main(String[] args) {
        BTree t = new BTree(3);
        for(int i = 0; i < 15; i++) {
            t.insert(i);
            System.out.println("\\infty>" + t);
        }
    }

    public BTree(int b) {
        root = new BNode(b);
    }

    public String toString() {
        return "" + root;
    }

    public void insert(int value) {
        BNode n = root;
        while(!n.children.isEmpty()) {
            BNode oldValue = n;
            for(int i = 0; i < n.keys.size(); i++) {
                if(n.keys.get(i) > value) {
                    n = n.children.get(i);
                    break;
                }
            }
            if(n == oldValue) n = n.children.get(n.children.size() - 1);
        }
        n.insert(value);
        root = n.split();
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
                }
            }
            keys.add(i, key);
        }

        public void split(int index) {
            BNode toSplit = children.get(index);
            int midPoint = toSplit.keys.size() / 2;
            int mid = toSplit.keys.get(midPoint);
            System.out.println("6>" + mid);
            BNode left = new BNode(b), right = new BNode(b);
            left.keys.addAll(toSplit.keys.subList(0, midPoint));
            left.parent = this;
            right.keys.addAll(toSplit.keys.subList(midPoint + 1, toSplit.keys.size()));
            right.parent = this;
            if(! toSplit.children.isEmpty()) {
                left.children.addAll(toSplit.children.subList(0, midPoint + 1));
                for(BNode b : left.children) b.parent = left;
                right.children.addAll(toSplit.children.subList(midPoint + 1, toSplit.children.size()));
                for(BNode b : right.children) b.parent = right;
            }
            keys.add(index, mid);
            children.remove(index);
            children.add(index, right);
            children.add(index, left);
        }

        public BNode split() {
            BNode n = this;
            System.out.println("1>" + n);
            while(n.parent != null) {
                if(n.keys.size() > b) {
                    System.out.println("1.5>" + n + " " + n.keys.size() + " " + b);
                    int splitIndex = n.parent.children.indexOf(n);
                    n.parent.split(splitIndex);
                }
                System.out.println("2>" + n);
                n = n.parent;
            }
            System.out.println("3>" + n);
            if(n.keys.size() > b) {
                n.parent = new BNode(b);
                n.parent.children.add(n);
                n.parent.split(0);
            }
            System.out.println("4>" + n.parent);
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


}