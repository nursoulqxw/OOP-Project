package views;

import controllers.AuthController;
import controllers.CourseController;
import controllers.EnrollmentController;
import controllers.MarkController;
import controllers.NewsController;
import data.Database;
import models.academic.Course;
import models.academic.Mark;
import models.academic.News;
import models.academic.RecommendationLetter;
import models.research.ResearchPaper;
import models.research.Researcher;
import models.research.ResearcherDecorator;
import models.research.StudentResearcher;
import models.enums.Major;
import models.enums.StudyYear;
import models.users.Student;
import models.users.Teacher;
import models.users.User;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * StudentView — menu for students.
 */
public class StudentView extends BaseView {

    private StudentView() {}

    public static void show(Student student) throws IOException {
        while (true) {
            heading("STUDENT — " + student.getFullName());
            System.out.println("1) View available courses");
            System.out.println("2) Register for a course");
            System.out.println("3) View my marks");
            System.out.println("4) View transcript");
            System.out.println("5) View news feed");
            System.out.println("6) Subscribe to news");
            System.out.println("7) View my recommendation letters");
            System.out.println("8) Research");
            System.out.println("9) View enrolled courses");
            System.out.println("10) Rate a teacher");
            System.out.println("11) Unsubscribe from news");
            System.out.println("12) Change password");
            System.out.println("13) My profile");
            System.out.println("14) Update profile (major / year)");
            System.out.println("0) Logout");
            String choice = prompt("> ");

            switch (choice) {
                case "1": listCourses();  break;
                case "2": register(); break;
                case "3": viewMarks();   break;
                case "4": viewTranscript(student); break;
                case "5": viewNews(); break;
                case "6": NewsController.subscribe(); break;
                case "7": viewRecommendations(student); break;
                case "8":  research(student); break;
                case "9":  viewEnrolled(student); break;
                case "10": rateTeacher(student); break;
                case "11": NewsController.unsubscribe(); System.out.println("Unsubscribed."); break;
                case "12": changePassword(); break;
                case "13": myProfile(student); break;
                case "14": updateProfile(student); break;
                case "0": return;
                default:  System.out.println("Unknown option.");
            }
        }
    }

    private static void listCourses() {
        Collection<Course> courses = CourseController.listAll();
        if (courses.isEmpty()) {
            System.out.println("No courses available.");
            return;
        }
        for (Course c : courses) {
            System.out.println("  " + c);
        }
    }

    private static void register() throws IOException {
        String id = prompt("Course id: ");
        Course c = CourseController.findById(id);
        if (c == null) {
            System.out.println("No such course.");
            return;
        }
        boolean ok = EnrollmentController.registerForCourse(c);
        System.out.println(ok ? "Registered." : "Registration failed.");
    }

    private static void viewMarks() {
        List<Mark> marks = MarkController.viewMyMarks();
        if (marks.isEmpty()) {
            System.out.println("No marks yet.");
            return;
        }
        for (Mark m : marks) {
            System.out.println("  " + m);
        }
    }

    private static void viewTranscript(Student s) {
        System.out.println(s.getTranscript());
        System.out.println(s.getTranscript().generateReport().export());
    }

    private static void viewNews() {
        List<News> feed = NewsController.getFeed();
        if (feed.isEmpty()) {
            System.out.println("No news yet.");
            return;
        }
        for (News n : feed) {
            System.out.println("  " + n);
        }
    }

    private static void viewRecommendations(Student student) {
        List<RecommendationLetter> letters = student.getRecommendations();
        if (letters.isEmpty()) {
            System.out.println("You have no recommendation letters.");
            return;
        }
        System.out.println("You have " + letters.size() + " letter(s):");
        for (RecommendationLetter letter : letters) {
            System.out.println();
            System.out.println(letter.export());
        }
    }

    private static void research(Student s) throws IOException {
        Researcher r = Database.getInstance().getResearcherByUserId(s.getId());
        if (r == null) {
            System.out.println("You are not registered as a researcher.");
            String yn = prompt("Become a student researcher? (y/n): ");
            if (!"y".equalsIgnoreCase(yn.trim())) return;
            String topic = prompt("Thesis topic: ");
            StudentResearcher sr = new StudentResearcher(s, topic);
            Database.getInstance().addResearcher(s.getId(), sr);
            r = sr;
            System.out.println("Registered as student researcher.");
        }
        while (true) {
            System.out.println("-- RESEARCH (h-index: " + r.getHIndex() + ") --");
            System.out.println("1) View my papers");
            System.out.println("2) Add paper");
            System.out.println("3) Update paper citations");
            if (r instanceof StudentResearcher) {
                System.out.println("4) Submit thesis");
            }
            System.out.println("0) Back");
            String c = prompt("> ");
            switch (c) {
                case "1": listPapers(r);                    break;
                case "2": addPaper(r, s.getFullName());     break;
                case "3": updateCitations(r);               break;
                case "4":
                    if (r instanceof StudentResearcher) {
                        ((StudentResearcher) r).submitThesis();
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

    private static void updateCitations(Researcher r) throws IOException {
        List<ResearchPaper> papers = r.getPapers();
        if (papers.isEmpty()) {
            System.out.println("No papers to update.");
            return;
        }
        for (int i = 0; i < papers.size(); i++) {
            System.out.printf("%d) %s  [citations: %d]%n",
                    i + 1, papers.get(i).getTitle(), papers.get(i).getCitations());
        }
        int idx = parseInt(prompt("Paper number: ")) - 1;
        if (idx < 0 || idx >= papers.size()) {
            System.out.println("Invalid selection.");
            return;
        }
        int newCitations = parseInt(prompt("New citations count: "));
        papers.get(idx).setCitations(newCitations);
        if (r instanceof ResearcherDecorator) {
            ((ResearcherDecorator) r).refreshHIndex();
        }
        System.out.println("Updated. h-index now: " + r.getHIndex());
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

    private static void viewEnrolled(Student s) {
        List<Course> courses = s.viewCourses();
        if (courses.isEmpty()) {
            System.out.println("You are not enrolled in any courses.");
            return;
        }
        for (Course c : courses) {
            System.out.println("  " + c);
        }
    }

    private static void rateTeacher(Student s) throws IOException {
        String tid = prompt("Teacher id: ");
        User u = Database.getInstance().getUsers().get(tid);
        if (!(u instanceof Teacher)) {
            System.out.println("Not a teacher.");
            return;
        }
        double rating = parseDouble(prompt("Rating (0-5): "));
        s.rateTeacher((Teacher) u, rating);
        System.out.println("Rated.");
    }

    private static void myProfile(Student s) {
        System.out.println("Student id    : " + s.getStudentId());
        System.out.println("Major         : " + s.getMajor());
        System.out.println("Year          : " + s.getYear());
        System.out.printf ("GPA           : %.2f%n", s.getGpa());
        System.out.println("Failed courses: " + s.getFailedCoursesCount());
        var supervisor = s.getSupervisor();
        if (supervisor != null) {
            System.out.println("Supervisor    : h-index " + supervisor.getHIndex());
        } else {
            System.out.println("Supervisor    : not assigned");
        }
    }

    private static void updateProfile(Student s) throws IOException {
        System.out.println("1) Change major");
        System.out.println("2) Change study year");
        String choice = prompt("> ");
        switch (choice) {
            case "1":
                System.out.println("Major options:");
                Major[] majors = Major.values();
                for (int i = 0; i < majors.length; i++)
                    System.out.println("  " + (i + 1) + ") " + majors[i].name());
                int mi = parseInt(prompt("> ")) - 1;
                if (mi >= 0 && mi < majors.length) {
                    s.setMajor(majors[mi]);
                    System.out.println("Major updated to " + s.getMajor());
                }
                break;
            case "2":
                System.out.println("Year options:");
                StudyYear[] years = StudyYear.values();
                for (int i = 0; i < years.length; i++)
                    System.out.println("  " + (i + 1) + ") " + years[i].name());
                int yi = parseInt(prompt("> ")) - 1;
                if (yi >= 0 && yi < years.length) {
                    s.setYear(years[yi]);
                    System.out.println("Year updated to " + s.getYear());
                }
                break;
            default:
                System.out.println("Unknown option.");
        }
    }

    private static void changePassword() throws IOException {
        String oldPwd = prompt("Current password: ");
        String newPwd = prompt("New password: ");
        boolean ok = AuthController.changePassword(oldPwd, newPwd);
        System.out.println(ok ? "Password changed." : "Failed.");
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private static double parseDouble(String s) {
        try { return Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

}