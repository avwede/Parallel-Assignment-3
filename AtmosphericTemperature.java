// Ashley Voglewede
// COP 4520 - Assignment 3
// Spring 2022
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

// This program implements a module responsible for measuring
// atmospheric temperature using 8 threads. It prints out a report
// at the end of every hour, with the 5 highest temps and the 5 lowest
// temps. 
public class AtmosphericTemperature extends Thread {
    public static ArrayList<Sensor> threads = new ArrayList<>();
    public ArrayList<Integer> data = new ArrayList<>();
    public PriorityQueue<Integer> lowFive = new PriorityQueue<>();
    public PriorityQueue<Integer> maxFive = new PriorityQueue<>(Collections.reverseOrder());
    ReentrantLock lock = new ReentrantLock();
    int numOfThreads, convertHours, hours;

    AtmosphericTemperature(int convertHours) {
        this.numOfThreads = 8;
        this.convertHours = convertHours * this.numOfThreads * 60;
        this.hours = convertHours;
    }

    Sensor getThread(int index) {
        return threads.get(index - 1);
    }

    ArrayList<Integer> printTop() {
        ArrayList<Integer> topFive = new ArrayList<>();
        for (int i = 0; i < 5; i++)
        {
            int num = maxFive.poll();
            if (topFive.contains(num))
            {
                i--;
                continue;
            }

            topFive.add(num);
        }

        maxFive.clear();
        return topFive;
    }

    ArrayList<Integer> printLow() {
        ArrayList<Integer> topFive = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            int num = lowFive.poll();
            if (topFive.contains(num)) {
                i--;
                continue;
            }

            topFive.add(num);
        }

        lowFive.clear();
        return topFive;
    }

    int[] findTempDiff() {
        int len = data.size();
        int[] tempDiff = new int[4];

        for (int i = 0, j = 1; i < len; i++) {
            if (data.get(i) >= tempDiff[0])
                tempDiff[0] = data.get(i);
            
            if (data.get(i) <= tempDiff[1])
                tempDiff[1] = data.get(i);

            if (i == j * 80) {
                if ((tempDiff[0] - tempDiff[1]) > tempDiff[2])
                    tempDiff[2] = tempDiff[0] - tempDiff[1];

                tempDiff[3] = j++;
            }
        }

        return tempDiff;
    }

    void printReport(int hour) {
        int[] tempDiff = findTempDiff();
        System.out.println("\nReport For Hour " + hour + ":");
        System.out.println("============================================");
        System.out.println("The Top 5 Temperatures are: " + printTop());
        System.out.println("The Lowest 5 Temperatures are: " + printLow());
        System.out.println("The Largest Temperature Difference is: " + tempDiff[2] + " between minute " + tempDiff[3] * 10 + " to minute " + (tempDiff[3] * 10 + 10));
        this.data.clear();
    }
    
    void partyTime(AtmosphericTemperature mainThread) throws InterruptedException {
        for (int i = 1; i <= this.numOfThreads; i++) {
            if (i == 1)
                threads.add(new Sensor(i, mainThread, true));
            else
                threads.add(new Sensor(i, mainThread, false));
        }

        for (int i = 0; i < mainThread.numOfThreads; i++)
            threads.get(i).start();

        for (int i = 0; i < mainThread.numOfThreads; i++)
            threads.get(i).join();
    }

    public static void main(String[] args) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Please enter the number of hours to simulate: ");

        while (!sc.hasNextInt()) {
            System.out.print("Error: Please input an integer value.");
            sc.nextLine();
        }

        int hours = sc.nextInt();
        sc.close();

        if (hours <= 0 || hours > 45) {
            System.out.println("Error: Please input an integer value that is more than 0 hours.");
            return;
        }

        AtmosphericTemperature mainThread = new AtmosphericTemperature(hours);
        final long start = System.currentTimeMillis();
        mainThread.partyTime(mainThread);
        final long end = System.currentTimeMillis();
        final long executionTime = end - start;
        System.out.println("\nTotal Execution Time: " + executionTime + " ms");
    }
}

class Sensor extends Thread {
    static AtomicInteger iterations = new AtomicInteger();
    static AtomicBoolean reachedHour = new AtomicBoolean();
    AtmosphericTemperature mainThread;
    boolean marked, leader = false;
    int threadNumber, hour = 1;

    Sensor(int threadNumber, AtmosphericTemperature mainThread, boolean leader) {
        this.threadNumber = threadNumber;
        this.mainThread = mainThread;
        this.leader = leader;
        this.marked = false;
    }

    boolean checkThreads() {
        for (int i = 0; i < 8; i++)
            if (!this.mainThread.getThread(this.threadNumber).marked)
                return false;

        return true;
    }

    @Override
    public void run() {
        while (iterations.get() < mainThread.convertHours) {
            while (!checkThreads()) {
                if (!this.marked) {
                    int rand = (int) (Math.floor(Math.random() * (70 - (-100) + 1) + (-100)));
                    this.mainThread.lock.lock();
                    
                    try {
                        this.mainThread.data.add(rand);
                        this.mainThread.maxFive.add(rand);
                        this.mainThread.lowFive.add(rand);
                        this.marked = true;
                    }
                    finally {
                        this.mainThread.lock.unlock();
                    }
                }
            }

            this.marked = false;
            iterations.getAndIncrement();

            // Checks whether or not we have reached an hour.
            if (iterations.get() / this.hour == 480)
                reachedHour.set(true);

            try {
                Thread.sleep(10);
            } 
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            // The leader is created to only allow one thread to create the report for each hour. 
            // Once we have reached an hour, we will print the report.
            if (reachedHour.get() && this.leader) {
                this.mainThread.lock.lock();

                try {
                    this.mainThread.printReport(this.hour++);
                    reachedHour.set(false);
                }
                finally {
                    this.mainThread.lock.unlock();
                }   
            }

            try {
                Thread.sleep(10);
            } 
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}