import java.util.*;

public class TrieNode {

    private Map<Character, TrieNode> children;
    private boolean endOfWord;
    private PriorityQueue<FileCount> value;

    public TrieNode() {
        children = new HashMap<Character, TrieNode>();
    }

    public Map<Character, TrieNode> getChildren() {
        return children;
    }

    public boolean isEndOfWord() {
        return endOfWord;
    }

    public PriorityQueue<FileCount> getValue() {
        return value;
    }

    public void setValue(PriorityQueue<FileCount>  value) {
        endOfWord = true;
        this.value = value;
    }

    public void removeValue() {
        this.endOfWord = false;
        value = null;
    }
}