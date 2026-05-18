package views;

import controllers.AuthController;
import controllers.UniversitySystem;
import controllers.UserController;
import core.builder.CourseBuilder;
import core.factory.EmployeeFactory;
import core.factory.StudentFactory;
import core.factory.TeacherFactory;
import core.factory.UserFactory;
import core.observer.NewsService;
import data.Database;
import data.LogEntry;
import models.academic.Course;
import models.academic.News;
import models.enums.Major;
import models.enums.ManagerType;
import java.time.LocalDate;
import models.enums.StudyYear;
import models.enums.TeacherTitle;
import models.users.Admin;
import models.users.Student;
import models.users.Teacher;
import models.users.User;
import models.academic.RecommendationLetter;
import models.research.Researcher;
import models.research.ResearcherDecorator;
import models.research.TeacherResearcher;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminView — menu for administrators.
 *
 * Use cases (per the diagram):
 *   - Add user (Student / Teacher / Manager) via Factory pattern
 *   - Update user
 *   - Remove user
 *   - View logs
 *   - List all users
 *   - Create course (Builder pattern)
 *   - Publish news (Observer pattern)
 *   - Assign teacher to course
 */
public class AdminView extends BaseView {

    private AdminView() {}

    public static void show(Admin admin) throws IOException {
        while (true) {
            heading("ADMIN — " + admin.getFullName());
            System.out.println("1) List all users");
            System.out.println("2) Find user by id");
            System.out.println("3) Add user");
            System.out.println("4) Update user");
            System.out.println("5) Remove user by id");
            System.out.println("6) Create course");
            System.out.println("7) Assign teacher to course");
            System.out.println("8) Publish news");
            System.out.println("9) View audit logs");
            System.out.println("a) Top cited researcher of school");
            System.out.println("b) Top cited researcher of year");
            System.out.println("c) All research papers");
            System.out.println("d) Change password");
            System.out.println("0) Logout");
            String choice = prompt("> ");

            switch (choice) {
                case "1": listAll();         break;
                case "2": find();            break;
                case "3": addUser(admin);    break;
                case "4": updateUser(admin); break;
                case "5": remove();          break;
                case "6": createCourse();    break;
                case "7": assignTeacher();   break;
                case "8": publishNews(admin); break;
                case "9": logs();                      break;
                case "a": topResearcherBySchool();     break;
                case "b": topResearcherByYear();       break;
                case "c": allPapers();                 break;
                case "d": changePassword();            break;
                case "0": return;
                default:  System.out.println("Unknown option.");
            }
        }
    }

    // -------------------- existing --------------------

    private static void listAll() {
        Collection<User> users = UserController.listAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users in the system.");
            return;
        }
        System.out.println("Users (" + users.size() + "):");
        for (User u : users) System.out.println("  " + u);
    }

    private static void find() throws IOException {
        String id = prompt("User id: ");
        User u = UserController.findById(id);
        System.out.println(u == null ? "Not found." : u.toString());
    }

    private static void remove() throws IOException {
        String id = prompt("User id to remove: ");
        boolean ok = UserController.removeUser(id);
        System.out.println(ok ? "Removed." : "Failed.");
    }

    private static void logs() {
        List<LogEntry> entries = UserController.viewLogs();
        if (entries.isEmpty()) {
            System.out.println("No log entries.");
            return;
        }
        for (LogEntry e : entries) System.out.println(e);
    }

    // -------------------- add user (Factory pattern) --------------------

    private static void addUser(Admin admin) throws IOException {
        System.out.println("Role:");
        System.out.println("  1) Student");
        System.out.println("  2) Teacher");
        System.out.println("  3) Manager");
        String role = prompt("> ");

        Map<String, Object> data = new HashMap<>();
        data.put("id",        prompt("Id (e.g. S001): "));
        data.put("firstName", prompt("First name: "));
        data.put("lastName",  prompt("Last name: "));
        data.put("email",     prompt("Email: "));
        data.put("password",  prompt("Password: "));
        data.put("phone",     prompt("Phone: "));

        UserFactory factory;

        switch (role) {
            case "1":
                data.put("studentId", prompt("Student id (e.g. 23B0301): "));
                data.put("major",     promptEnum("Major", Major.values()));
                data.put("year",      promptEnum("Year",  StudyYear.values()));
                factory = new StudentFactory();
                break;

            case "2":
                data.put("salary",     prompt("Salary: "));
                data.put("hireDate",   LocalDate.now());
                data.put("department", prompt("Department: "));
                data.put("title",      promptEnum("Title", TeacherTitle.values()));
                factory = new TeacherFactory();
                break;

            case "3":
                data.put("salary",      prompt("Salary: "));
                data.put("hireDate",    LocalDate.now());
                data.put("department",  prompt("Department: "));
                data.put("managerType", promptEnum("Manager type", ManagerType.values()));
                factory = new EmployeeFactory();
                break;

            default:
                System.out.println("Unknown role.");
                return;
        }

        User created = factory.createUser(data);
        if (created == null) {
            System.out.println("Factory returned null — aborting.");
            return;
        }

        admin.addUser(created);
        System.out.println("Added: " + created);

        if (created instanceof Teacher && ((Teacher) created).isProfessor()) {
            TeacherResearcher tr = new TeacherResearcher(created);
            Database.getInstance().addResearcher(created.getId(), tr);
            System.out.println("(Decorator) Professor registered as TeacherResearcher.");
        }
    }

    // -------------------- update user --------------------

    private static void updateUser(Admin admin) throws IOException {
        String id = prompt("User id to update: ");
        User u = UserController.findById(id);
        if (u == null) { System.out.println("Not found."); return; }

        System.out.println("Current: " + u);
        System.out.println("Field to change:");
        System.out.println("  1) password");
        System.out.println("  2) phone");
        if (u instanceof Student) {
            System.out.println("  3) GPA");
            System.out.println("  4) failed courses count");
        }
        String which = prompt("> ");

        switch (which) {
            case "1":
                String pwd = prompt("New password: ");
                u.setPassword(pwd);
                break;
            case "2":
                String phone = prompt("New phone: ");
                u.setPhone(phone);
                break;
            case "3":
                if (u instanceof Student) {
                    double gpa = parseDoubleOr(prompt("New GPA (0.0-4.0): "), 0.0);
                    ((Student) u).setGpa(gpa);
                }
                break;
            case "4":
                if (u instanceof Student) {
                    int count = parseIntOr(prompt("Failed courses count: "), 0);
                    ((Student) u).setFailedCoursesCount(count);
                }
                break;
            default:
                System.out.println("Unknown field."); return;
        }

        admin.updateUser(u);
        System.out.println("Updated: " + u);
    }

    // -------------------- create course (Builder pattern) --------------------

    private static void createCourse() throws IOException {
        String courseId = prompt("Course id (e.g. INFT3134): ");
        String name     = prompt("Course name: ");
        int credits     = parseIntOr(prompt("Credits: "), 3);
        Major major     = (Major)     promptEnum("Major", Major.values());
        StudyYear year  = (StudyYear) promptEnum("Year",  StudyYear.values());

        Course c = new CourseBuilder()
                .setId(courseId)
                .setName(name)
                .setCredits(credits)
                .setMajor(major)
                .setYear(year)
                .build();

        Database.getInstance().addCourse(c);
        System.out.println("Created (Builder): " + c);
    }

    // -------------------- assign teacher to course --------------------

    private static void assignTeacher() throws IOException {
        String courseId = prompt("Course id: ");
        Course c = Database.getInstance().getCourseById(courseId);
        if (c == null) { System.out.println("No such course."); return; }

        String teacherId = prompt("Teacher id: ");
        User u = UserController.findById(teacherId);
        if (!(u instanceof Teacher)) {
            System.out.println("That id is not a Teacher."); return;
        }

        ((Teacher) u).manageCourse(c);
        System.out.println("Assigned " + u.getFullName() + " to " + c);
    }

    // -------------------- publish news (Observer pattern) --------------------

    private static void publishNews(Admin admin) throws IOException {
        String newsId = prompt("News id (e.g. N001): ");
        String title  = prompt("News title: ");
        String body   = prompt("News body: ");

        News n = new News(newsId, title, body, admin);
        NewsService.getInstance().publishNews(n);
        System.out.println("(Observer) News published; subscribers notified.");
    }

    // -------------------- analytics & password --------------------

    private static void topResearcherBySchool() throws IOException {
        String school = prompt("School/department name: ");
        Researcher r = UniversitySystem.getInstance().getTopCitedResearcherOfSchool(school);
        if (r == null) { System.out.println("No researchers found for: " + school); return; }
        String name = r instanceof ResearcherDecorator
                ? ((ResearcherDecorator) r).getWrappedUser().getFullName()
                : r.toString();
        System.out.printf("Top cited: %s (h-index: %d)%n", name, r.getHIndex());
    }

    private static void topResearcherByYear() throws IOException {
        int year = parseIntOr(prompt("Year (e.g. 2024): "), 2024);
        Researcher r = UniversitySystem.getInstance().getTopCitedResearcherOfYear(year);
        if (r == null) { System.out.println("No researchers with papers in " + year); return; }
        String name = r instanceof ResearcherDecorator
                ? ((ResearcherDecorator) r).getWrappedUser().getFullName()
                : r.toString();
        System.out.printf("Top cited (%d): %s (h-index: %d)%n", year, name, r.getHIndex());
    }

    private static void allPapers() {
        UniversitySystem.getInstance().printAllPapers((a, b) -> b.getCitations() - a.getCitations());
    }

    private static void changePassword() throws IOException {
        String oldPwd = prompt("Current password: ");
        String newPwd = prompt("New password: ");
        boolean ok = AuthController.changePassword(oldPwd, newPwd);
        System.out.println(ok ? "Password changed." : "Failed.");
    }

    // -------------------- helpers --------------------

    private static <E extends Enum<E>> Enum<?> promptEnum(String label, E[] values) throws IOException {
        System.out.println(label + " options:");
        for (int i = 0; i < values.length; i++) {
            System.out.println("  " + (i + 1) + ") " + values[i].name());
        }
        int idx = parseIntOr(prompt("> "), 1) - 1;
        if (idx < 0 || idx >= values.length) idx = 0;
        return values[idx];
    }
}