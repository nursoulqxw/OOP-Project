package views;

import controllers.AuthController;
import controllers.MarkController;
import controllers.RequestController;
import data.Database;
import models.academic.Course;
import models.research.ResearchPaper;
import models.research.Researcher;
import models.research.TeacherResearcher;
import models.users.Student;
import models.users.Teacher;
import models.users.User;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * TeacherView — menu for teachers.
 */
public class TeacherView extends BaseView {

    private TeacherView() {}

    public static void show(Teacher teacher) throws IOException {
        while (true) {
            heading("TEACHER — " + teacher.getFullName() + " (" + teacher.getTitle() + ")");
            System.out.println("1) View my courses");
            System.out.println("2) View students of a course");
            System.out.println("3) Put mark");
            System.out.println("4) Take attendance for a course");
            System.out.println("5) Generate mark report");
            System.out.println("6) Write recommendation");
            System.out.println("7) Research");
            System.out.println("8) My profile");
            System.out.println("9) Create request");
            System.out.println("10) Change password");
            System.out.println("0) Logout");
            String choice = prompt("> ");

            switch (choice) {
                case "1": listCourses(teacher);            break;
                case "2": viewStudents(teacher);           break;
                case "3": putMark(teacher);                break;
                case "4": takeAttendance(teacher);         break;
                case "5": markReport(teacher);             break;
                case "6": writeRecommendation(teacher);    break;
                case "7":  research(teacher);          break;
                case "8":  myProfile(teacher);         break;
                case "9":  createRequest();            break;
                case "10": changePassword();           break;
                case "0": return;
                default:  System.out.println("Unknown option.");
            }
        }
    }

    private static void listCourses(Teacher t) {
        if (t.getCoursesTaught().isEmpty()) {
            System.out.println("You don't teach any courses yet.");
            return;
        }
        for (Course c : t.getCoursesTaught()) {
            System.out.println("  " + c);
        }
    }

    private static void viewStudents(Teacher t) throws IOException {
        Course c = pickCourse(t);
        if (c == null) return;
        List<Student> students = t.viewStudents(c);
        if (students.isEmpty()) {
            System.out.println("No students enrolled.");
            return;
        }
        for (Student s : students) {
            System.out.println("  " + s);
        }
    }

    private static void putMark(Teacher t) throws IOException {
        Course c = pickCourse(t);
        if (c == null) return;

        List<Student> students = t.viewStudents(c);
        if (students.isEmpty()) {
            System.out.println("No students enrolled in this course.");
            return;
        }
        System.out.println("Enrolled students:");
        for (Student st : students) {
            System.out.printf("  [%s] %s%n", st.getId(), st.getFullName());
        }

        String sid = prompt("Student id: ");
        User u = Database.getInstance().getUsers().get(sid);
        if (!(u instanceof Student)) {
            System.out.println("Not a student.");
            return;
        }
        Student student = (Student) u;

        System.out.println("Mark component:");
        System.out.println("  1) Attestation 1");
        System.out.println("  2) Attestation 2");
        System.out.println("  3) Final Exam");
        String comp = prompt("> ");

        boolean ok;
        switch (comp) {
            case "1":
                ok = MarkController.putAtt1(student, c, parseDouble(prompt("Attestation 1 (0-100): ")));
                break;
            case "2":
                ok = MarkController.putAtt2(student, c, parseDouble(prompt("Attestation 2 (0-100): ")));
                break;
            case "3":
                ok = MarkController.putFinal(student, c, parseDouble(prompt("Final exam (0-100): ")));
                break;
            default:
                System.out.println("Unknown option.");
                return;
        }
        System.out.println(ok ? "Mark recorded." : "Failed.");
    }

    private static void takeAttendance(Teacher t) throws IOException {
        Course c = pickCourse(t);
        if (c == null) return;
        if (c.getLessons().isEmpty()) {
            System.out.println("No lessons yet for this course.");
            return;
        }
        t.takeAttendance(c);
        System.out.println("Attendance marked for the latest lesson.");
    }

    private static void markReport(Teacher t) throws IOException {
        Course c = pickCourse(t);
        if (c == null) return;
        System.out.println(t.generateMarkReport(c).export());
    }

    private static void writeRecommendation(Teacher t) throws IOException {
        String studentId = prompt("Student id: ");
        User u = Database.getInstance().getUsers().get(studentId);
        if (!(u instanceof Student)) {
            System.out.println("Not a student.");
            return;
        }
        String content = prompt("Letter content: ");
        String purpose = prompt("Purpose (e.g. job, exchange): ");
        var letter = t.writeRecommendation((Student) u, content, purpose);
        System.out.println("Letter created:");
        System.out.println(letter.export());
    }

    private static Course pickCourse(Teacher t) throws IOException {
        if (t.getCoursesTaught().isEmpty()) {
            System.out.println("You don't teach any courses yet.");
            return null;
        }
        listCourses(t);
        String id = prompt("Course id: ");
        for (Course c : t.getCoursesTaught()) {
            if (c.getCourseId().equals(id)) return c;
        }
        System.out.println("No such course in your list.");
        return null;
    }

    private static void research(Teacher t) throws IOException {
        Researcher r = Database.getInstance().getResearcherByUserId(t.getId());
        if (r == null) {
            System.out.println("Research access requires PROFESSOR title.");
            return;
        }
        while (true) {
            System.out.println("-- RESEARCH (h-index: " + r.getHIndex() + ") --");
            System.out.println("1) View my papers");
            System.out.println("2) Add paper");
            if (r instanceof TeacherResearcher) {
                System.out.println("3) Apply for grant");
            }
            System.out.println("0) Back");
            String c = prompt("> ");
            switch (c) {
                case "1": listPapers(r);               break;
                case "2": addPaper(r, t.getFullName()); break;
                case "3":
                    if (r instanceof TeacherResearcher) {
                        String grant = prompt("Grant name: ");
                        ((TeacherResearcher) r).applyForGrant(grant);
                    }
                    break;
                case "0": return;
                default: System.out.println("Unknown option.");
            }
        }
    }

    private static void listPapers(Researcher r) {
        if (r.getPapers().isEmpty()) {
            System.out.println("No papers yet.");
            return;
        }
        System.out.println("Papers:");
        for (ResearchPaper p : r.getPapers()) {
            System.out.println("  " + p);
        }
    }

    private static void addPaper(Researcher r, String authorName) throws IOException {
        String doi       = prompt("DOI: ");
        String title     = prompt("Title: ");
        String journal   = prompt("Journal: ");
        int pages        = parseInt(prompt("Pages: "));
        int year         = parseInt(prompt("Year (YYYY): "));
        int citations    = parseInt(prompt("Citations: "));
        ResearchPaper paper = new ResearchPaper(doi, title, journal, pages,
                LocalDate.of(year == 0 ? LocalDate.now().getYear() : year, 1, 1));
        paper.addAuthor(authorName);
        paper.setCitations(citations);
        r.addPaper(paper);
        Database.getInstance().getPapers().put(doi, paper);
        System.out.println("Paper added. h-index now: " + r.getHIndex());
    }

    private static void myProfile(Teacher t) {
        System.out.println("Department : " + t.getDepartment());
        System.out.printf ("Salary     : %.2f%n", t.getSalary());
        System.out.println("Hire date  : " + t.getHireDate());
        System.out.printf ("Rating     : %.2f / 5.0 (%d ratings)%n", t.getRating(), t.getRatingCount());
    }

    private static void createRequest() throws IOException {
        String type = prompt("Request type (e.g. LEAVE/SALARY/OTHER): ");
        String desc = prompt("Description: ");
        var r = RequestController.createRequest(type, desc);
        System.out.println(r != null ? "Request submitted: " + r.getRequestId() : "Failed.");
    }

    private static void changePassword() throws IOException {
        String oldPwd = prompt("Current password: ");
        String newPwd = prompt("New password: ");
        boolean ok = AuthController.changePassword(oldPwd, newPwd);
        System.out.println(ok ? "Password changed." : "Failed.");
    }

    private static double parseDouble(String s) {
        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) { return 0; }
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }
}