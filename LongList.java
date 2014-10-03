/**
 * Created by mrx on 03.10.14.
 */
public class LongList {
    private Node head = null;
    private Node current = null;

    public LongList(long data) {
        head = new Node(data);
        current = head;
    }

    public void add(long data) {
        current.next = new Node(data);
        current = current.next;
    }

    public long last() {
        return current.data;
    }

    private class Node {
        public long data;
        public Node next = null;

        public Node(long data) {
            this.data = data;
        }
    }
}
