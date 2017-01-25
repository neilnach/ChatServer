import java.util.Arrays;

/**
 * <b> CS 180 - Project 4 - Chat Server Skeleton </b>
 * <p>
 * <p>
 * This is the skeleton code for the ChatServer Class. This is a private chat
 * server for you and your friends to communicate.
 *
 * @author Neil Nachnani nnachnan@purdue.edu
 * Tejaswi Kotekar tkotekar@purdue.edu
 * @version November 20th, 2015
 * @lab Neil - BR8
 * Tejaswi - LC2
 */

public class CircularBuffer {
    final int size;
    int counter = 0;
    private static String[] buff;
    public int index;
    public int numAvailable;

    public CircularBuffer(int size) {
        this.size = size;
        this.buff = new String[this.size];
        index = 0;
    }

    public void put(String message) {
        if (index == this.size) {
            index = 0;
        }
        String message1 = String.format("%04d) %s", counter, message);

        this.buff[index] = message1;
        counter++;
        index++;


    }

    public String[] getNewest(int numMessages) {
        if (numMessages < 0) {
            return null;
        }

        numAvailable = Math.min(size, counter);

        int length = Math.min(numAvailable, numMessages);
        String[] messages = new String[length];
        int temp = index;
        for (int i = length - 1; i >= 0; i--) {
            if (temp == 0) {
                temp = size;
            }
            temp = (temp - 1) % size;
            messages[i] = buff[temp];
        }
        return messages;
    }


    public static void main(String[] args) {
        CircularBuffer c = new CircularBuffer(5);
        c.put("0");
        c.put("1");
        c.put("2");
        c.put("3");
        c.put("4");
        c.put("5");

        System.out.println(Arrays.toString(buff));
        System.out.println(Arrays.toString(c.getNewest(4)));
    }


}
