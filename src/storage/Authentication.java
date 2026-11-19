package storage;

import java.util.ArrayList;

// handles user registration, login and credential validation
public class Authentication {

    // each entry is "username,encodedPassword" (password stored as ascii codes)
    private static ArrayList<String> users = new ArrayList<String>();

    public static ArrayList<String> getUsers() {
        return users;
    }

    // converts a password to ascii codes joined with dashes, e.g. "abcd" -> "97-98-99-100"
    private static String encodePassword(String password) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            if (i > 0) {
                sb.append("-");
            }
            sb.append((int) password.charAt(i));
        }
        return sb.toString();
    }

    // true if the stored password field is already in ascii-code form
    private static boolean isEncoded(String stored) {
        return stored.matches("\\d+(-\\d+)+");
    }

    // converts old plaintext passwords in the list to ascii-code form and saves the file
    public static void migrateOldPasswords() {
        boolean changed = false;
        for (int i = 0; i < users.size(); i++) {
            String[] parts = users.get(i).split(",", 2);
            if (parts.length == 2 && !isEncoded(parts[1])) {
                users.set(i, parts[0] + "," + encodePassword(parts[1]));
                changed = true;
            }
        }
        if (changed) {
            FileManager.saveUsers();
        }
    }

    // returns an error message, or null if registration worked
    public static String register(String username, String password) {
        if (username == null || username.trim().equals("")) {
            return "Username cannot be empty!";
        }
        if (password == null || password.trim().equals("")) {
            return "Password cannot be empty!";
        }
        if (username.contains(",") || password.contains(",")) {
            return "Username and password cannot contain commas!";
        }
        if (password.length() < 4) {
            return "Password must be at least 4 characters!";
        }
        // check if username already exists
        for (int i = 0; i < users.size(); i++) {
            String[] parts = users.get(i).split(",");
            if (parts[0].equals(username)) {
                return "Username already taken!";
            }
        }
        users.add(username + "," + encodePassword(password));
        FileManager.saveUsers();
        return null;
    }

    public static boolean login(String username, String password) {
        for (int i = 0; i < users.size(); i++) {
            String[] parts = users.get(i).split(",");
            if (parts[0].equals(username) && parts[1].equals(encodePassword(password))) {
                return true;
            }
        }
        return false;
    }
}
