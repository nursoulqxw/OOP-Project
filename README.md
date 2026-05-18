# University System

A console-based university management system built in Java. The goal is to simulate a real university with multiple user roles, each seeing only their own slice of functionality — students register for courses and track grades, teachers grade and do research, managers handle requests and reports, admins control everything. All state is persisted to disk between sessions.

---

## Project Goal

Model a working university back-end using clean OOP and at least six design patterns (Singleton, Factory, Builder, Decorator, Observer, Strategy). Every action a real university actor would perform — enrolling in a course, publishing news, writing a research paper, approving a request — is represented as a menu option tied to a business rule enforced in code.

---

## Architecture (Layered)

```
tests/Main.java          ← entry point, seeds default users
views/                   ← console menus, one per role
controllers/             ← business logic, called by views
core/                    ← patterns infrastructure (factory, builder, observer, strategy, auth)
models/                  ← domain objects (users, academic, research, enums, exceptions)
data/                    ← Database singleton + persistence (db.dat)
```

Each layer only talks to the layer below it. Views call controllers, controllers call models and data, models know nothing about views.

---

## User Roles & What They Can Do

### Admin (`AdminView`)
- Add / update / remove users (Student, Teacher, Manager, Employee)
- Create courses via Builder pattern
- Assign teachers to courses
- Publish news (triggers Observer notifications)
- View audit logs
- View research stats (top-cited researcher by school or year)

### Manager (`ManagerView`)
- View and approve/reject student requests
- Generate academic and teacher reports (Strategy pattern)
- View all courses and enrolled students
- View all papers sorted by citations

### Teacher (`TeacherView`)
- View courses they teach
- Record and update student marks (Attestation 1, Attestation 2, Final Exam)
- Track attendance per lesson
- Write recommendation letters for students
- Research menu (PROFESSOR title only):
  - Add research papers
  - Update paper citations → recalculates h-index
  - Apply for grants

### Student (`StudentView`)
- Register for courses (enforces 21-credit cap and 3-failed-courses cap)
- View enrolled courses and marks
- View transcript and GPA
- Rate teachers (0–5 scale)
- Assign a research supervisor (supervisor must have h-index ≥ 3)
- News feed (subscribed to NewsService)
- Submit requests (leave, salary, other)
- Research menu (opt-in as StudentResearcher):
  - Add research papers
  - Update paper citations → recalculates h-index
  - Submit thesis

---

## How H-Index Works

H-index = largest number **h** such that at least **h** papers each have at least **h** citations.

| Paper citations (sorted ↓) | H-index |
|---|---|
| [3, 3, 3] | 3 |
| [4, 3, 3, 3] | 3 — 4th paper needs ≥4 but only has 3 |
| [4, 4, 4, 4] | 4 |
| [5, 5, 5, 5, 5] | 5 |

To grow h-index beyond its current value: use **"Update paper citations"** in the research menu to raise existing papers' citation counts, then add new papers with matching citation counts.

---

## Design Patterns

| Pattern | Where | Purpose |
|---|---|---|
| **Singleton** | `Database`, `Logger`, `UniversitySystem`, `NewsService` | One instance across the whole app |
| **Factory** | `StudentFactory`, `TeacherFactory`, `EmployeeFactory` | Create correct user type without `new` in views |
| **Builder** | `CourseBuilder`, `TranscriptBuilder` | Construct complex objects step by step |
| **Decorator** | `ResearcherDecorator` → `TeacherResearcher`, `StudentResearcher`, `EmployeeResearcher` | Add research capability to any User at runtime |
| **Observer** | `NewsService` + `Observer`/`Subject` interfaces | Notify subscribed users when news is published |
| **Strategy** | `AcademicReportStrategy`, `TeacherReportStrategy`, `ResearchReportStrategy`; `SortByCitationsStrategy`, `SortByDateStrategy`, `SortByPagesStrategy` | Swap report format or sort order without changing callers |
| **Facade** | `UniversitySystem` | Single entry point hiding Database + AuthService wiring |

---

## Key Business Rules

| Rule | Where enforced |
|---|---|
| Max 21 credits per semester | `Student.registerForCourse()` → `CreditLimitExceededException` |
| Max 3 failed courses | `Student.registerForCourse()` → `CourseFailLimitException` |
| Supervisor must have h-index ≥ 3 | `Student.assignSupervisor()` → `LowHIndexException` |
| Only PROFESSOR title gets research access | `TeacherView.research()` checks `Database.getResearcherByUserId()` |
| Teacher rating must be 0–5 | `Student.rateTeacher()` → `IllegalArgumentException` |

---

## Project Structure

```
src/
├── tests/
│   └── Main.java                     # Entry point + default user seeding
│
├── controllers/
│   ├── UniversitySystem.java         # Facade: lifecycle, top-level use cases
│   ├── AuthController.java           # Login / logout / password change
│   ├── CourseController.java         # Create, assign teacher, list courses
│   ├── EnrollmentController.java     # Enroll / drop student from course
│   ├── MarkController.java           # Record and fetch marks
│   ├── NewsController.java           # Publish and list news
│   ├── RequestController.java        # Submit and approve/reject requests
│   ├── ResearchController.java       # Add papers, join projects
│   └── UserController.java           # CRUD for users
│
├── views/
│   ├── MainView.java                 # Login router → role-specific view
│   ├── AdminView.java
│   ├── ManagerView.java
│   ├── TeacherView.java
│   ├── StudentView.java
│   ├── LoginView.java
│   ├── RegisterView.java
│   └── BaseView.java                 # Shared prompt/input helpers
│
├── models/
│   ├── users/
│   │   ├── User.java                 # Abstract base: id, name, email, password, phone
│   │   ├── Student.java              # + major, year, GPA, transcript, supervisor
│   │   ├── Teacher.java              # + department, salary, title, rating
│   │   ├── Manager.java              # + manager type
│   │   ├── Admin.java                # + salary, department, experience
│   │   └── Employee.java             # Non-teaching staff
│   │
│   ├── academic/
│   │   ├── Course.java               # courseId, credits, major, year, instructors, students
│   │   ├── Mark.java                 # att1, att2, final; auto-computes letter grade + GPA points
│   │   ├── Transcript.java           # Map<Course, Mark>; computes cumulative GPA
│   │   ├── Lesson.java               # Topic, type (LECTURE/SEMINAR/LAB), date
│   │   ├── AttendanceRecord.java     # Per-lesson presence tracking
│   │   ├── Request.java              # Type, description, status (PENDING/APPROVED/REJECTED)
│   │   ├── News.java                 # Title + body published by admin/manager
│   │   ├── RecommendationLetter.java # Written by teacher for a specific student
│   │   ├── Report.java               # Key-value store produced by ReportStrategy
│   │   └── Transcript.java
│   │
│   ├── research/
│   │   ├── Researcher.java           # Interface: getHIndex, addPaper, joinProject, printPapers
│   │   ├── ResearcherDecorator.java  # Abstract decorator; stores papers list, computes h-index
│   │   ├── TeacherResearcher.java    # Adds grant application
│   │   ├── StudentResearcher.java    # Adds thesis submission
│   │   ├── EmployeeResearcher.java   # Base employee researcher
│   │   ├── ResearchPaper.java        # DOI, title, journal, pages, date, authors, citations
│   │   └── ResearchProject.java      # Project title, participants
│   │
│   ├── enums/
│   │   ├── Major.java                # CS, SE, IT, ...
│   │   ├── StudyYear.java            # FIRST … FOURTH
│   │   ├── TeacherTitle.java         # TUTOR, SENIOR_LECTURER, PROFESSOR, ...
│   │   ├── ManagerType.java          # OR_DEPARTMENT, DEAN, etc.
│   │   ├── Semester.java             # FALL, SPRING
│   │   ├── LessonType.java           # LECTURE, SEMINAR, LAB
│   │   └── RequestStatus.java        # PENDING, APPROVED, REJECTED
│   │
│   └── exceptions/
│       ├── AuthenticationException.java
│       ├── CourseFailLimitException.java
│       ├── CreditLimitExceededException.java
│       ├── LowHIndexException.java
│       └── NotResearcherException.java
│
├── core/
│   ├── AuthService.java              # Password check, session state
│   ├── Logger.java                   # Singleton; writes LogEntry list in Database
│   ├── factory/
│   │   ├── UserFactory.java          # Abstract factory interface
│   │   ├── StudentFactory.java
│   │   ├── TeacherFactory.java       # Auto-wraps PROFESSOR into TeacherResearcher
│   │   └── EmployeeFactory.java
│   ├── builder/
│   │   ├── CourseBuilder.java        # Fluent builder for Course
│   │   └── TranscriptBuilder.java
│   ├── observer/
│   │   ├── Observer.java             # interface: update(NewsEvent)
│   │   ├── Subject.java              # interface: subscribe/unsubscribe/notify
│   │   ├── NewsService.java          # Singleton Subject; User implements Observer
│   │   └── NewsEvent.java            # Wraps a News object
│   ├── strategy/
│   │   ├── ReportStrategy.java       # interface: build(Map) → Report
│   │   ├── AcademicReportStrategy.java
│   │   ├── TeacherReportStrategy.java
│   │   ├── ResearchReportStrategy.java
│   │   ├── SortStrategy.java         # interface: getComparator() → Comparator<ResearchPaper>
│   │   ├── SortByCitationsStrategy.java
│   │   ├── SortByDateStrategy.java
│   │   └── SortByPagesStrategy.java
│   └── interfaces/
│       └── Reportable.java           # generateReport() — implemented by Student, Teacher
│
└── data/
    ├── Database.java                 # Singleton; holds all maps; saves/loads db.dat via Java serialization
    └── LogEntry.java                 # Timestamp + message stored in Database.logs
```

---

## Getting Started

### Prerequisites
- Java 11+
- IntelliJ IDEA (`.idea/` and `.iml` config included) or any IDE

### Run

1. Open the project in IntelliJ and run `tests.Main`.
2. First launch seeds three default accounts:

```
Admin   — admin@kbtu.kz   / admin
Student — student@kbtu.kz / student
Teacher — teacher@kbtu.kz / teacher
```

3. All changes are saved to `db.dat` on exit (normal or Ctrl+C).

### Compile manually

```bash
find src -name "*.java" > sources.txt
javac -d out @sources.txt
java -cp out tests.Main
```

### Reset state

Delete `db.dat` — next launch re-seeds the default accounts.