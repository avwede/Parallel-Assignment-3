// Ashley Voglewede
// COP 4520 - Assignment 3
// Spring 2022

// This implementation of a lock free linked list is adapted from the book, 
// The Art of Multiprocessor Programming, on page 216.

import java.util.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class LinkedList{
    private Node head;

    LinkedList() {
        this.head = new Node(Integer.MIN_VALUE);
        this.head.next = new AtomicMarkableReference<Node>(new Node(Integer.MAX_VALUE), false);
    }
 
    public Window find(Node head, int key) {
        Node pred = null, curr = null, succ = null;
        boolean[] marked = { false };
        boolean snip;

        retry: while (true) {
            pred = head;
            curr = pred.next.getReference();

            while (true) {
                succ = curr.next.get(marked);
                while (marked[0]) {
                    snip = pred.next.compareAndSet(curr, succ, false, false);
                    if (!snip)
                        continue retry;

                    curr = succ;
                    succ = curr.next.get(marked);
                }
                if (curr.key >= key)
                    return new Window(pred, curr);

                pred = curr;
                curr = succ;
            }
        }
    }

    public boolean add(int key) {
        while (true) {
            Window window = find(head, key);
            Node pred = window.pred, curr = window.curr;

            if (curr.key == key) {
                return false;
            } 
            else {
                Node node = new Node(key);
                node.next = new AtomicMarkableReference<Node>(curr, false);

                if (pred.next.compareAndSet(curr, node, false, false))
                    return true;
            }
        }
    }

    public boolean remove(int key) {
        boolean snip;
        while (true) {
            Window window = find(head, key);
            Node pred = window.pred, curr = window.curr;

            if (curr.key != key) {
                return false;
            } 
            else {
                Node succ = curr.next.getReference();
                snip = curr.next.compareAndSet(succ, succ, false, true);

                if (!snip)
                    continue;

                pred.next.compareAndSet(curr, succ, false, false);
                return true;
            }
        }
    }

    public boolean contains(int key) {
        boolean[] marked = { false };
        Node curr = head;

        while (curr.key < key) {
            curr = curr.next.getReference();
            Node succ = curr.next.get(marked);
        }

        return (curr.key == key && !marked[0]);
    }
}

class Window {
    public Node pred, curr;

    Window(Node myPred, Node myCurr) {
        pred = myPred; curr = myCurr;
    }
}

class Node {
    int key;
    AtomicMarkableReference<Node> next;

    Node(int key)
    {
        this.key = key;
        this.next = new AtomicMarkableReference<Node>(null, false);
    }
}