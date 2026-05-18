package models.users;

import core.interfaces.Reportable;
import core.strategy.AcademicReportStrategy;
import core.strategy.ReportStrategy;
import models.academic.Course;
import models.academic.Mark;
import models.academic.RecommendationLetter;
import models.academic.Report;
import models.academic.Transcript;
import models.enums.Major;
import models.enums.StudyYear;
import models.exceptions.CourseFailLimitException;
import models.exceptions.CreditLimitExceededException;
import models.exceptions.LowHIndexException;
import models.research.Researcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Student — academic actor with a transcript, courses, and (optionally)
 * a research supervisor.
 *
 * Business rules:
 *   - max 21 credits per semester              -> CreditLimitExceededException
 *   - max 3 failed courses                     -> CourseFailLimitException
 *   - supervisor must have h-index >= 3        -> LowHIndexException
 */
public class Student extends User implements Comparable<Student>, Reportable {

    private static final long serialVersionUID = 1L;

    public static final int MAX_CREDITS_PER_SEMESTER = 21;
    public static final int MAX_FAILED_COURSES       = 3;
    public static final int MIN_SUPERVISOR_HINDEX    = 3;

    private String studentId;
    private Major major;
    private StudyYear year;
    private double gpa;
    private final List<Course> enrolledCourses = new ArrayList<>();
    private final Transcript transcript;
    private int failedCoursesCount;
    private Researcher supervisor;
    private final List<RecommendationLetter> recommendations = new ArrayList<>();

    public Student(String id, String firstName, String lastName,
                   String email, String password, String phone,
                   String studentId, Major major, StudyYear year) {
        super(id, firstName, lastName, email, password, phone);
        this.studentId = studentId;
        this.major = major;
        this.year = year;
        this.gpa = 0.0;
        this.failedCoursesCount = 0;
        this.transcript = new Transcript(this);
    }

    @Override
    public String getRole() {
        return "Student";
    }

    /**
     * Register for a course. Enforces the credit limit and the failed-courses limit.
     */
    public void registerForCourse(Course c) {
        if (c == null) return;

        if (failedCoursesCount >= MAX_FAILED_COURSES) {
            throw new CourseFailLimitException(getFullName(), failedCoursesCount, MAX_FAILED_COURSES);
        }

        int currentCredits = getTotalCredits();
        int newCredits = c.getCredits();
        if (currentCredits + newCredits > MAX_CREDITS_PER_SEMESTER) {
            throw new CreditLimitExceededException(currentCredits, newCredits, MAX_CREDITS_PER_SEMESTER);
        }

        if (!enrolledCourses.contains(c)) {
            enrolledCourses.add(c);
            c.enrollStudent(this);
        }
    }

    public List<Mark> viewMarks() {
        return new ArrayList<>(transcript.getMarks().values());
    }

    public List<Course> viewCourses() {
        return new ArrayList<>(enrolledCourses);
    }

    public Transcript getTranscript() {
        return transcript;
    }

    public void rateTeacher(Teacher t, double rating) {
        if (t == null) return;
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("Rating must be in [0..5]");
        }
        t.receiveRating(rating);
    }

    public int getTotalCredits() {
        int total = 0;
        for (Course c : enrolledCourses) {
            total += c.getCredits();
        }
        return total;
    }

    /**
     * Assign a research supervisor. The candidate must have h-index >= 3.
     */
    public void assignSupervisor(Researcher r) {
        if (r == null) {
            throw new IllegalArgumentException("Supervisor cannot be null");
        }
        if (r.getHIndex() < MIN_SUPERVISOR_HINDEX) {
            String name = (r instanceof models.research.ResearcherDecorator)
                    ? ((models.research.ResearcherDecorator) r).getWrappedUser().getFullName()
                    : "researcher";
            throw new LowHIndexException(name, r.getHIndex(), MIN_SUPERVISOR_HINDEX);
        }
        this.supervisor = r;
    }

    public List<RecommendationLetter> getRecommendations() {
        return recommendations;
    }

    public void addRecommendation(RecommendationLetter letter) {
        if (letter != null) recommendations.add(letter);
    }

    @Override
    public int compareTo(Student o) {
        if (o == null) return -1;
        return Double.compare(o.gpa, this.gpa);
    }

    @Override
    public Report generateReport() {
        ReportStrategy s = new AcademicReportStrategy();
        Map<String, Object> data = new HashMap<>();
        data.put("student", this);
        data.put("transcript", transcript);
        return s.build(data);
    }


    public String getStudentId() {
        return studentId;
    }

    public Major getMajor() {
        return major;
    }

    public StudyYear getYear() {
        return year;
    }

    public double getGpa() {
        return gpa;
    }


    public List<Course> getEnrolledCourses() {
        return enrolledCourses;
    }

    public int getFailedCoursesCount() {
        return failedCoursesCount;
    }

    public Researcher getSupervisor() {
        return supervisor;
    }


    public void setMajor(Major major) {
        this.major = major;
    }

    public void setYear(StudyYear year) {
        this.year = year;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public void setFailedCoursesCount(int count) {
        this.failedCoursesCount = count;
    }

    public void incrementFailedCourses() {
        this.failedCoursesCount++;
    }
}