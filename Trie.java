import java.util.*;

public class Trie {

    private TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    public void insert(String word, PriorityQueue<FileCount> value) {
        TrieNode current = root;

        for (char c: word.toCharArray()) {
            Map<Character, TrieNode> children = current.getChildren();
            // check that character is within our character set
            if (!children.containsKey(c)) {
                children.put(c, new TrieNode());
            }
            current = children.get(c);
        }

        current.setValue(value);
    }

    public PriorityQueue<FileCount> get(String word) throws Exception {
        TrieNode current = root;

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            TrieNode node = current.getChildren().get(c);
            if (node == null) {
                throw new Exception(word + " does not exist.");
            }
            current = node;
        }

        if (current.isEndOfWord()) {
            return current.getValue();
        }
        throw new Exception(word + " is not the end of a word.");
    }

    public boolean delete (String word) {
        return delete(root, word, 0);
    }

    private boolean delete(TrieNode current, String word, int index) {
        Map<Character, TrieNode> children = current.getChildren();
        if (index == word.length()) {
            if (!current.isEndOfWord()) {
                return false;
            }
            current.removeValue();
            return children.isEmpty();
        }
        char c = word.charAt(index);
        TrieNode node = children.get(c);

        if (node == null) {
            return false;
        }

        if (delete(node, word, index + 1) && !node.isEndOfWord()) {
            children.remove(c);
            return children.isEmpty();
        }
        return false;
    }

    public static void main(String[] args) {
//        FileCount f1 = new FileCount("fake", 3);
//        FileCount f2 = new FileCount("fake2", 5);
//        FileCount f3 = new FileCount("fak3e", 2);
//        PriorityQueue<FileCount> p1 = new PriorityQueue<>(new Comparator<FileCount>(){
//            @Override
//            public int compare(FileCount a1, FileCount a2) {
//                return a1.getCount() < a2.getCount() ? 1: -1;
//            }
//        });
//
//        PriorityQueue<FileCount> p2 = new PriorityQueue<>(new Comparator<FileCount>(){
//            @Override
//            public int compare(FileCount a1, FileCount a2) {
//                return a1.getCount() < a2.getCount() ? 1: -1;
//            }
//        });
//        p1.offer(f1);
//        p2.offer(f2);
//        p1.offer(f3);
//
//        try {
//            Trie trie = new Trie();
//            trie.insert("try1", p1);
//            trie.insert("try12345", p2);
//            System.out.println(trie.get("try1").poll());
//            System.out.println(trie.get("try12345").poll());
//            System.out.println(trie.get("tryu"));
//        } catch (Exception e) {
//            System.out.println(e);
//        }
    }
}