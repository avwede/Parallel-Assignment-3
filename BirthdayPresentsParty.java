// Ashley Voglewede
// COP 4520 - Assignment 3
// Spring 2022

// This program takes "presents" represented by integers from an unordered bag and
// creates a chain using a concurrent linked list. The thank you card scenario 
// is simulated by dedicating 1 thread per servant and assuming that the 
// Minotur received 500,000 presents from his guests.
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class BirthdayPresentsParty extends Thread {
    public static ArrayList<Servant> threads = new ArrayList<>();
    public AtomicInteger thankYouCards = new AtomicInteger();
    public ArrayList<Integer> chainOfGifts = new ArrayList<>();
    public LinkedList chain = new LinkedList();
    public Stack<Integer> gifts = new Stack<>();
    ReentrantLock lock = new ReentrantLock();
    int presents, numThreads;

    BirthdayPresentsParty() {
        this.presents = 500_000;
        this.numThreads = 4;
    }

    void partyTime(BirthdayPresentsParty mainThread) throws InterruptedException {
        // Add threads with assigned values of 1 - n.
        for (int i = 1; i <= this.numThreads; i++)
            threads.add(new Servant(i, mainThread));

        for (int i = 0; i < mainThread.numThreads; i++)
            threads.get(i).start();

        for (int i = 0; i < mainThread.numThreads; i++)
            threads.get(i).join();

        System.out.println("Total thank you cards written: " + thankYouCards);
    }

    public static void main(String args[]) throws InterruptedException {
        BirthdayPresentsParty mainThread = new BirthdayPresentsParty();

        for (int i = 1; i <= mainThread.presents; i++)
            mainThread.gifts.push(i);

        Collections.shuffle(mainThread.gifts);

        final long start = System.currentTimeMillis();
        mainThread.partyTime(mainThread);
        final long end = System.currentTimeMillis();
        final long executionTime = end - start;
        System.out.println("Total Execution Time: " + executionTime + " ms");
    }
}

class Servant extends Thread {
    BirthdayPresentsParty mainThread;
    int threadNumber;

    Servant(int threadNumber, BirthdayPresentsParty mainThread) {
        this.threadNumber = threadNumber;
        this.mainThread = mainThread;
    }

    @Override
    public void run() {
        while (mainThread.thankYouCards.get() < mainThread.presents) {
            int gift, actions = (int) (Math.random() * 3 + 1);

            switch (actions) {
                case 1:
                    // Take a present from the unordered bag and add it to the chain in the correct
                    // location by hooking it to the predecessor’s link. The servant also had to make sure
                    // that the newly added present is also linked with the next present in the chain.
                    mainThread.lock.lock();

                    try {
                        if (this.mainThread.gifts.empty())
                            break;
                        gift = mainThread.gifts.pop();
                        mainThread.chainOfGifts.add(gift);
                    } 
                    finally {
                        mainThread.lock.unlock();
                    }

                    mainThread.chain.add(gift);
                    break;
                case 2:
                    // Write a “Thank you” card to a guest and remove the present from the chain. To do so, 
                    // a servant had to unlink the gift from its predecessor and make sure to connect the 
                    // predecessor’s link with the next gift in the chain.
                    mainThread.lock.lock();

                    try {
                        if (this.mainThread.chainOfGifts.size() == 0)
                            break;

                        int rand = (int) (Math.random() * mainThread.chainOfGifts.size());
                        gift = mainThread.chainOfGifts.get(rand);
                        mainThread.chainOfGifts.remove(rand);
                    } 
                    finally {
                        mainThread.lock.unlock();
                    }

                    mainThread.chain.remove(gift);
                    mainThread.thankYouCards.getAndIncrement();
                    break;
                case 3:
                    // Per the Minotaur’s request, check whether a gift with a particular tag was present
                    // in the chain or not; without adding or removing a new gift, a servant would scan 
                    // through the chain and check whether a gift with a particular tag is already added 
                    // to the ordered chain of gifts or not.
                    int checkGift = (int) (Math.random() * mainThread.presents + 1);
                    mainThread.chain.contains(checkGift);
                    break;
            }
        }
    }
}