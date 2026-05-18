package views;

import controllers.AuthController;
import controllers.CourseController;
import controllers.NewsController;
import controllers.RequestController;
import controllers.ResearchController;
import core.builder.CourseBuilder;
import core.strategy.SortByCitationsStrategy;
import data.Database;
import models.academic.Course;
import models.academic.News;
import models.academic.Request;
import models.enums.Major;
import models.enums.RequestStatus;
import models.enums.StudyYear;
import models.research.ResearchProject;
import models.users.Manager;
import models.users.Teacher;
import models.users.User;

import java.io.IOException;
import java.util.List;

/**
 * ManagerView — menu for managers.
 */
public class ManagerView extends BaseView {

    private ManagerView() {}

    public static void show(Manager manager) throws IOException {
        while (true) {
            heading("MANAGER — " + manager.getFullName() + " (" + manager.getType() + ")");
            System.out.println("1) Add course (using CourseBuilder)");
            System.out.println("2) List courses");
            System.out.println("3) Assign teacher to course");
            System.out.println("4) Publish news");
            System.out.println("5) Approve / reject requests");
            System.out.println("6) Generate academic report");
            System.out.println("7) Open course for registration (set major/year)");
            System.out.println("8) All research papers (by citations)");
            System.out.println("9) All research projects");
            System.out.println("10) All stored news");
            System.out.println("11) Change password");
            System.out.println("0) Logout");
            String choice = prompt("> ");

            switch (choice) {
                case "1": addCourse();          break;
                case "2": listCourses();        break;
                case "3": assignTeacher();      break;
                case "4": publishNews(manager); break;
                case "5": handleRequests();     break;
                case "6":  System.out.println(manager.generateAcademicReport().export()); break;
                case "7":  openForRegistration(manager); break;
                case "8":  ResearchController.printAllPapersSorted(new SortByCitationsStrategy()); break;
                case "9":  allProjects(); break;
                case "10": storedNews(); break;
                case "11": changePassword(); break;
                case "0": return;
                default:  System.out.println("Unknown option.");
            }
        }
    }

    private static void addCourse() throws IOException {
        String id      = prompt("Course id (e.g. CSCI1207): ");
        String name    = prompt("Course name: ");
        int credits    = parseIntOr(prompt("Credits: "), 3);
        Major major    = pickMajor(prompt("Major (IS/CS/SE/MATH): "));
        StudyYear year = pickYear(prompt("Year (FIRST/SECOND/THIRD/FOURTH): "));

        Course c = new CourseBuilder()
                .setId(id)
                .setName(name)
                .setCredits(credits)
                .setMajor(major)
                .setYear(year)
                .build();
        boolean ok = CourseController.addCourse(c);
        System.out.println(ok ? ("Added: " + c) : "Failed.");
    }

    private static void listCourses() {
        for (Course c : CourseController.listAll()) {
            System.out.println("  " + c);
        }
    }

    private static void assignTeacher() throws IOException {
        String tid = prompt("Teacher id: ");
        User u = Database.getInstance().getUsers().get(tid);
        if (!(u instanceof Teacher)) {
            System.out.println("Not a teacher.");
            return;
        }
        String cid = prompt("Course id: ");
        Course c = CourseController.findById(cid);
        if (c == null) {
            System.out.println("No such course.");
            return;
        }
        boolean ok = CourseController.assignTeacher((Teacher) u, c);
        System.out.println(ok ? "Assigned." : "Failed.");
    }

    private static void publishNews(Manager m) throws IOException {
        String id    = "N-" + System.currentTimeMillis();
        String title = prompt("News title: ");
        String body  = prompt("News content: ");
        News n = new News(id, title, body, m);
        NewsController.publish(n);
    }

    private static void handleRequests() throws IOException {
        List<Request> all = RequestController.listAll();
        if (all.isEmpty()) {
            System.out.println("No requests.");
            return;
        }
        for (Request r : all) {
            System.out.println("  " + r);
        }
        String id = prompt("Request id (or blank to skip): ");
        if (id.isBlank()) return;
        Request target = null;
        for (Request r : all) {
            if (r.getRequestId().equals(id)) {
                target = r;
                break;
            }
        }
        if (target == null) {
            System.out.println("Not found.");
            return;
        }
        if (target.getStatus() != RequestStatus.PENDING) {
            System.out.println("Already handled: " + target.getStatus());
            return;
        }
        String action = prompt("(a)pprove or (r)eject: ");
        if (action.startsWith("a"))      RequestController.approve(target);
        else if (action.startsWith("r")) RequestController.reject(target);
    }

    private static void openForRegistration(Manager m) throws IOException {
        String cid = prompt("Course id: ");
        Course c = CourseController.findById(cid);
        if (c == null) { System.out.println("No such course."); return; }
        Major major    = pickMajor(prompt("Major (IS/CS/SE/MATH): "));
        StudyYear year = pickYear(prompt("Year (FIRST/SECOND/THIRD/FOURTH): "));
        m.addCourseForRegistration(c, major, year);
        System.out.println("Opened for registration: " + c);
    }

    private static void allProjects() {
        List<ResearchProject> projects = ResearchController.listAllProjects();
        if (projects.isEmpty()) { System.out.println("No research projects."); return; }
        for (ResearchProject p : projects) System.out.println("  " + p);
    }

    private static void storedNews() {
        List<News> all = NewsController.listAllStored();
        if (all.isEmpty()) { System.out.println("No stored news."); return; }
        for (News n : all) System.out.println("  " + n);
    }

    private static void changePassword() throws IOException {
        String oldPwd = prompt("Current password: ");
        String newPwd = prompt("New password: ");
        boolean ok = AuthController.changePassword(oldPwd, newPwd);
        System.out.println(ok ? "Password changed." : "Failed.");
    }

    private static Major pickMajor(String s) {
        try { return Major.valueOf(s.toUpperCase()); }
        catch (Exception e) { return Major.IS; }
    }

    private static StudyYear pickYear(String s) {
        try { return StudyYear.valueOf(s.toUpperCase()); }
        catch (Exception e) { return StudyYear.FIRST; }
    }
}