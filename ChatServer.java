import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

/**
 * 
 */
public class ChatServer {
    User[] users;
    int maxMessages;
    CircularBuffer b;

    public ChatServer(User[] users, int maxMessages) {
        this.users = users;
        this.maxMessages = maxMessages;
        this.users = new User[users.length + 1];

        this.users[0] = new User("root", "cs180", null);

        for (int i = 0; i < users.length; i++) {
            this.users[i + 1] = users[i];
        } //users in the array have to be added to the server's list of users

        this.maxMessages = maxMessages; //required size of the buffer
        this.b = new CircularBuffer(maxMessages);
    }

    /**
     * This method begins server execution.
     */
    public void run() {
        boolean verbose = false;
        System.out.printf("The VERBOSE option is off.\n\n");
        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.printf("Input Server Request: ");
            String command = in.nextLine();

            // this allows students to manually place "\r\n" at end of command
            // in prompt
            command = replaceEscapeChars(command);

            if (command.startsWith("kill"))
                break;

            if (command.startsWith("verbose")) {
                verbose = !verbose;
                System.out.printf("VERBOSE has been turned %s.\n\n", verbose ? "on" : "off");
                continue;
            }

            String response = null;
            try {
                response = parseRequest(command);
            } catch (Exception ex) {
                response = MessageFactory.makeErrorMessage(MessageFactory.UNKNOWN_ERROR,
                        String.format("An exception of %s occurred.", ex.getMessage()));
            }

            // change the formatting of the server response so it prints well on
            // the terminal (for testing purposes only)
            if (response.startsWith("SUCCESS\t"))
                response = response.replace("\t", "\n");

            // print the server response
            if (verbose)
                System.out.printf("response:\n");
            System.out.printf("\"%s\"\n\n", response);
        }

        in.close();
    }

    /**
     * Replaces "poorly formatted" escape characters with their proper values.
     * For some terminals, when escaped characters are entered, the terminal
     * includes the "\" as a character instead of entering the escape character.
     * This function replaces the incorrectly inputed characters with their
     * proper escaped characters.
     *
     * @param str - the string to be edited
     * @return the properly escaped string
     */
    private static String replaceEscapeChars(String str) {
        str = str.replace("\\r", "\r");
        str = str.replace("\\n", "\n");
        str = str.replace("\\t", "\t");

        return str;
    }

    public SessionCookie sf(String cookie) {
        for (int i = 0; i < users.length; i++) {
            long cookie1 = Long.parseLong(cookie);
            if (users[i] != null && users[i].getCookie() != null && users[i].getCookie().getID() == cookie1) {
                return users[i].getCookie();
            }
        }
        return null;
    }

    /**
     * Determines which client command the request is using and calls the
     * function associated with that command.
     *
     * @param request - the full line of the client request (CRLF included)
     * @return the server response
     */
    public String parseRequest(String request) {
        if (request == null) {
            return MessageFactory.makeErrorMessage(MessageFactory.FORMAT_COMMAND_ERROR);
        }

        if (!request.contains("\t")) {
            return MessageFactory.makeErrorMessage(10);
        }

        if (!request.endsWith("\r\n")) {
            return MessageFactory.makeErrorMessage(10);
        }


        String[] s = request.split("\t");

        s[s.length - 1] = s[s.length - 1].substring(0, s[s.length - 1].length() - 2);


        switch (s[0]) {
            case "ADD-USER":
                try {
                    long cookie = Long.parseLong(s[1]);
                    SessionCookie sc = sf(s[1]);
                    if (s.length != 4) {
                        return MessageFactory.makeErrorMessage(MessageFactory.FORMAT_COMMAND_ERROR);
                    }
                    if (sc == null) {
                        return MessageFactory.makeErrorMessage(MessageFactory.LOGIN_ERROR);
                    }
                    if (sc.hasTimedOut()) {
                        sc = null;
                        return MessageFactory.makeErrorMessage(MessageFactory.COOKIE_TIMEOUT_ERROR);
                    }
                    return addUser(s);
                } catch (NumberFormatException e) {
                    return MessageFactory.makeErrorMessage(24);
                }
            case "USER-LOGIN":
                if (s.length != 3) {
                    return MessageFactory.makeErrorMessage(MessageFactory.FORMAT_COMMAND_ERROR);
                } else
                    return userLogin(s);
            case "POST-MESSAGE":
                try {
                    long cookie = Long.parseLong(s[1]);
                    SessionCookie sc = sf(s[1]);
                    if (s.length != 3) {
                        return MessageFactory.makeErrorMessage(MessageFactory.FORMAT_COMMAND_ERROR);
                    }
                    if (sc == null) {
                        return MessageFactory.makeErrorMessage(MessageFactory.LOGIN_ERROR);
                    }
                    if (sc.hasTimedOut()) {
                        sc = null;
                        return MessageFactory.makeErrorMessage(MessageFactory.COOKIE_TIMEOUT_ERROR);
                    }
                    return postMessage(s, s[1]);


                } catch (NumberFormatException e) {
                    return MessageFactory.makeErrorMessage(24);
                }

            case "GET-MESSAGES":
                try {
                    long cookie = Long.parseLong(s[1]);
                    SessionCookie sc = sf(s[1]);
                    if (s.length != 3) {
                        return MessageFactory.makeErrorMessage(MessageFactory.FORMAT_COMMAND_ERROR);
                    }
                    if (sc == null) {
                        return MessageFactory.makeErrorMessage(23);
                    }

                    if (sc.hasTimedOut()) {
                        for (int i = 0; i < users.length; i++) {
                            if (users[i].getCookie() != null && users[i].getCookie().getID() == cookie) {
                                users[i].setCookie(null);
                            }
                        }
                        return MessageFactory.makeErrorMessage(MessageFactory.COOKIE_TIMEOUT_ERROR);
                    }
                    return getMessages(s);
                } catch (NumberFormatException e) {
                    return MessageFactory.makeErrorMessage((24));
                }
        }
        if (!s[0].equals("USER-LOGIN") || !s[0].equals("ADD-USER") || !s[0].equals("POST-MESSAGES") ||
                !s[0].equals("GET-MESSAGES")) {
            return MessageFactory.makeErrorMessage(11);
        }
        return null;
    }

//        if (s[0].equals("ADD-USER") || s[0].equals("POST-MESSAGES") || s[0].equals("GET-MESSAGES")) {
//            if (s[1] == null) {
//                return MessageFactory.makeErrorMessage(00);
//            }
//            if (s[0].equals("USER-LOGIN")) {
//                for (int i = 0; i < users.length; i++) {
//                    if (users[i].getName().equals(s[1])) {
//                        if (users[i].getCookie().hasTimedOut()) {
//                            s[1] = null;
//                            return MessageFactory.makeErrorMessage(05);
//                        }
//                    }
//                }
//            }
//            if (s[0].equals("ADD-USER")) {
//                for (int i = 0; i < users.length; i++) {
//                    if (users[i].getName().equals(s[2])) {
//                        if (this.users[i].getCookie().hasTimedOut()) {
//                            s[2] = null;
//                            return MessageFactory.makeErrorMessage(05);
//                        }
//                    }
//                }
//            }
//            if (!s[0].equals("USER-LOGIN") || !s[0].equals("ADD-USER") || !s[0].equals("POST-MESSAGES") ||
//                    !s[0].equals("GET-MESSAGES")) {
//                return MessageFactory.makeErrorMessage(10);
//            }
//            if (s[0].equals("ADD-USER")) {
//                if (s.length != 4) {
//                    return MessageFactory.makeErrorMessage(10);
//                }
//            }
//            if (!s[0].equals("ADD-USER")) {
//                if (s.length != 3) {
//                    return MessageFactory.makeErrorMessage(10);
//                }
//
//            }
//            if (!s[0].equals("USER-LOGIN") && s[1] == null) {
//                return MessageFactory.makeErrorMessage(21);
//            }
//
//            if (s[0].equals("ADD-USER")) {
//                return addUser(s);
//            }
//            if (s[0].equals("USER-LOGIN")) {
//                return userLogin(s);
//            }
//            if (s[0].equals("POST-MESSAGES")) {
//                return postMessage(s, s[2]);
//            }
//            if (s[0].equals("GET-MESSAGES")) {
//                return getMessages(s);
//            }
//
//        }
////        return "";
//    }


    public String addUser(String[] args) {
        String cookie = args[1];
        String username = args[2];
        String password = args[3];
        if (!isAlphaNumeric(username) || !isAlphaNumeric(password)) {
            return MessageFactory.makeErrorMessage(24);
        }

        long cookie1 = Long.parseLong(cookie);

        for (int i = 0; i < users.length; i++) {
            if (users[i].getName().equals(username)) {
                return MessageFactory.makeErrorMessage(22);
            }
        }

        String[] user = username.split("");
        String[] pass = password.split("");


        //check boundaries of username and password
        if (username.length() > 20 || username.length() < 1) {
            return MessageFactory.makeErrorMessage(24);
        }
        if (password.length() < 4 || password.length() > 40) {
            return MessageFactory.makeErrorMessage(24);
        }


        for (int i = 0; i < users.length; i++) {
            if (username.equals(users[i].getName())) {
                return MessageFactory.makeErrorMessage(22);
            }
        }

        for (int i = 0; i < users.length; i++) {
            if (users[i] != null && users[i].getCookie() != null && cookie1 == users[i].getCookie().getID()) {
                users[i].getCookie().updateTimeOfActivity();
            }
        }

        //splitting into characters to check if each is alphanumeric


        //check if username already exists
        users = Arrays.copyOf(users, users.length + 1);
        users[users.length - 1] = new User(username, password, null);

        return "SUCCESS\r\n";
    }

    //check to see if input is alphanumeric for letters and numbers
    public boolean isAlphaNumeric(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (!Character.isLetterOrDigit(string.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public String userLogin(String[] args) {
        String user = args[1];
        String pass = args[2];
        User u = null;

        for (int i = 0; i < users.length; i++) {
            if (users[i] != null && users[i].getName().equals(user)) {
                u = users[i];
                break;
            }
        }
        if (u == null) {
            return MessageFactory.makeErrorMessage(20);
        }
        if (u.getCookie() != null) {
            return MessageFactory.makeErrorMessage(25);
        }
        if (!u.checkPassword(pass)) {
            return MessageFactory.makeErrorMessage(21);
        }
        u.setCookie(new SessionCookie(generateRandomID()));
        return "SUCCESS\t" + u.getCookie().getID() + "\r\n";
    }

    public long generateRandomID() {
        Random r = new Random();
        boolean flag = true;
        int x;
        do {
            x = r.nextInt(10000);
            for (int i = 0; i < users.length; i++) {
                if (users[i] != null && users[i].getCookie() != null && users[i].getCookie().getID() == x) {
                    flag = false;
                }
            }
        }
        while (flag != true);
        String str = String.format("%04d", x);
        return Long.parseLong(str);
    }

    public String postMessage(String[] args, String name) {
        String cookie = args[1];
        String message = args[2];
        int index = -1;
        long cookie1 = Long.parseLong(cookie);
        for (int i = 0; i < users.length; i++) {
            if (users[i].getCookie() != null && users[i].getCookie().getID() == cookie1) {
                index = i;
            }
        }
        if (index == -1) {
            return MessageFactory.makeErrorMessage(20);
        }
        users[index].getCookie().updateTimeOfActivity();
        //check if message has at least one character after removing all whitespaces 24 error

        String s = message.replace(" ", "");

        if (s.equals("")) {
            return MessageFactory.makeErrorMessage(24);
        }

        String newMessage = name + ": " + message;
        b.put(newMessage);

        return "SUCCESS\r\n";
    }

    public String getMessages(String[] args) {
        String cookieID = args[1];
        String numMessages = args[2];
        String[] messages1;
        if (!numMessages.matches("[0-9]+")) {
            return MessageFactory.makeErrorMessage(24);
        }

        int numberMessages = Integer.parseInt(numMessages);

        if (numberMessages < 1) {
            return MessageFactory.makeErrorMessage(24);
        }
        if (numberMessages > maxMessages) {
            messages1 = b.getNewest(maxMessages);
        } else {
            messages1 = b.getNewest(numberMessages);
        }

        String result = "SUCCESS";

        for (int i = 0; i < messages1.length; i++) {
            result = result + "\t" + messages1[i];
        }

        return result + "\r\n";
    }

    public static void main(String[] args) {
        User[] users = new User[1];
        users[0] = new User("greg", "greg", new SessionCookie(42));
        ChatServer chatServer = new ChatServer(users, 100);


        String student = chatServer.parseRequest("GET-MESSAGES\t42\t1\r\n");


        student = chatServer.parseRequest("GET-MESSAGES\t42\t1\r\n");
        System.out.println(student);

    }
}


//check if the session cookie are numbers
//figure out what user is associated with the cookie id
// check if cookie timeout
//set cookie to null return message


