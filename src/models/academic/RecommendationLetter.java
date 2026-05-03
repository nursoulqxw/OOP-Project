package models.academic;

import models.users.Student;
import models.users.Teacher;

import java.time.LocalDateTime;

public class RecommendationLetter {
    private String letterId;
    private Teacher author;
    private Student recipient;
    private LocalDateTime issuedAt;
    private String content;

    public RecommendationLetter(String letterId, Teacher author, Student recipient, String content) {
        this.letterId = letterId;
        this.author = author;
        this.recipient = recipient;
        this.content = content;
        this.issuedAt = LocalDateTime.now();
    }

    public String getLetterId() { return letterId; }
    public Teacher getAuthor() { return author; }
    public Student getRecipient() { return recipient; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    // All read-only

    public String getContent() { return content; }
}
