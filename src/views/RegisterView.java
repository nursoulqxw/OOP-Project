package views;

import controllers.AuthController;
import core.factory.StudentFactory;
import core.factory.TeacherFactory;
import data.Database;
import models.enums.Major;
import models.enums.StudyYear;
import models.enums.TeacherTitle;
import models.research.TeacherResearcher;
import models.users.Teacher;
import models.users.User;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class RegisterView extends BaseView {

    private RegisterView() {}

    /**
     * Collect details, create account, and return the new User
     * so MainView can dispatch directly to their view. Returns null if cancelled.
     */
    public static User show() throws IOException {
        heading("REGISTER");
        System.out.println("1) Student");
        System.out.println("2) Teacher");
        System.out.println("0) Back");
        String choice = prompt("> ");

        switch (choice) {
            case "1": return registerStudent();
            case "2": return registerTeacher();
            case "0": return null;
            default:
                System.out.println("Unknown option.");
                return null;
        }
    }

    private static User registerStudent() throws IOException {
        String email = prompt("Email: ");
        if (emailTaken(email)) { System.out.println("Email already registered."); return null; }

        Map<String, Object> data = new HashMap<>();
        data.put("id",        generateId("S"));
        data.put("firstName", prompt("First name: "));
        data.put("lastName",  prompt("Last name: "));
        data.put("email",     email);
        data.put("password",  prompt("Password: "));
        data.put("phone",     prompt("Phone: "));
        data.put("studentId", prompt("Student number (e.g. 23B0301): "));
        data.put("major",     promptEnum("Major", Major.values()));
        data.put("year",      promptEnum("Study year", StudyYear.values()));

        User u = new StudentFactory().createUser(data);
        Database.getInstance().getUsers().put(u.getId(), u);
        AuthController.login(email, (String) data.get("password"));
        System.out.println("Account created! Welcome, " + u.getFullName()
                + " (id=" + u.getId() + ")");
        return u;
    }

    private static User registerTeacher() throws IOException {
        String email = prompt("Email: ");
        if (emailTaken(email)) { System.out.println("Email already registered."); return null; }

        Map<String, Object> data = new HashMap<>();
        data.put("id",         generateId("T"));
        data.put("firstName",  prompt("First name: "));
        data.put("lastName",   prompt("Last name: "));
        data.put("email",      email);
        data.put("password",   prompt("Password: "));
        data.put("phone",      prompt("Phone: "));
        data.put("department", prompt("Department: "));
        data.put("title",      promptEnum("Title", TeacherTitle.values()));
        data.put("salary",     0.0);
        data.put("hireDate",   LocalDate.now());

        User u = new TeacherFactory().createUser(data);
        Database.getInstance().getUsers().put(u.getId(), u);
        AuthController.login(email, (String) data.get("password"));

        if (u instanceof Teacher && ((Teacher) u).isProfessor()) {
            Database.getInstance().addResearcher(u.getId(), new TeacherResearcher(u));
            System.out.println("(Professor) Research profile created.");
        }
        System.out.println("Account created! Welcome, " + u.getFullName()
                + " (id=" + u.getId() + ")");
        return u;
    }

    private static boolean emailTaken(String email) {
        for (User u : Database.getInstance().getUsers().values()) {
            if (u.getEmail().equalsIgnoreCase(email)) return true;
        }
        return false;
    }

    private static String generateId(String prefix) {
        int max = 0;
        for (String key : Database.getInstance().getUsers().keySet()) {
            if (key.startsWith(prefix)) {
                try {
                    int n = Integer.parseInt(key.substring(prefix.length()));
                    if (n > max) max = n;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("%s%03d", prefix, max + 1);
    }

    private static <E extends Enum<E>> Enum<?> promptEnum(String label, E[] values)
            throws IOException {
        System.out.println(label + ":");
        for (int i = 0; i < values.length; i++) {
            System.out.println("  " + (i + 1) + ") " + values[i].name());
        }
        int idx = parseIntOr(prompt("> "), 1) - 1;
        if (idx < 0 || idx >= values.length) idx = 0;
        return values[idx];
    }
}