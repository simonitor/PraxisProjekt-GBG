package games.BlackJack;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MyLinkedList implements Iterable<Player> {

    Node head = null;
    Node tail = null;

    public Node getHead() {
        return head;
    }

    public void add(Player value) {
        Node newNode = new Node(value);
        if (head == null) {
            head = newNode;
        } else {
            tail.next = newNode;
        }
        tail = newNode;
        tail.next = head;
    }

    public int size() {
        if (head == null) {
            return 0;
        }
        int count = 1;
        Node current = head;
        while (!current.equals(tail)) {
            count++;
            current = current.next;
        }
        return count;
    }

    public Node find(Player value) {
        // TODO: optimize
        if (size() > 0) {
            Node run = head;
            if (run.value.equals(value)) {
                return run;
            }
            while (!run.equals(tail)) {
                if (value.equals(run.value)) {
                    return run;
                }
                run = run.next;
            }
            if (run.value.equals(value)) {
                return run;
            }
        }
        return null;
    }

    public void remove(Player value) {
        // TODO: optimize maybe
        Node current = head;
        if (head != null) {
            if (current.value.equals(value) && head.equals(tail)) {
                head = null;
                tail = null;
            } else if (current.value.equals(value)) {
                head = head.next;
                tail.next = head;
            } else {
                do {
                    Node next = current.next;
                    if (next.value.equals(value)) {
                        if (next.equals(tail)) {
                            tail = current;
                        }
                        current.next = next.next;
                        break;
                    }
                    current = current.next;
                } while (current != head);
            }

        }
    }

    @Override
    public Iterator<Player> iterator() {
        return new myIter(head, tail);
    }

    class myIter implements Iterator<Player> {
        Node current;
        Node tail;
        Node head;
        boolean atStart;

        public myIter(Node head, Node tail) {
            this.current = head;
            this.head = head;
            this.tail = tail;
            this.atStart = true;
        }

        @Override
        public boolean hasNext() {
            if (current == null || current.equals(head) && !atStart) {
                return false;
            }
            return true;
        }

        @Override
        public Player next() {
            atStart = false;
            Node oldCurrent = current;
            current = current.next;
            return oldCurrent.value;
        }

    }

}