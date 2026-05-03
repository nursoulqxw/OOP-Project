package models.academic;

import models.users.Student;

public class Mark {
    private double firstAttestation;
    private double secondAttestation;
    private double finalExam;
    private Course course;
    private Student student;

    public Mark(Course course, Student student) {
        this.course = course;
        this.student = student;
    }

    public double getFirstAttestation() { return firstAttestation; }
    public void setFirstAttestation(double firstAttestation) {
        this.firstAttestation = firstAttestation;
    }

    public double getSecondAttestation() { return secondAttestation; }
    public void setSecondAttestation(double secondAttestation) {
        this.secondAttestation = secondAttestation;
    }

    public double getFinalExam() { return finalExam; }
    public void setFinalExam(double finalExam) {
        this.finalExam = finalExam;
    }

    public Course getCourse() { return course; }
    public Student getStudent() { return student; }
    // course and student are usually fixed at creation
}
