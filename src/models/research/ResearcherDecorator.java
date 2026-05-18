package models.research;

import models.users.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Abstract Decorator that adds research capabilities to any User.

 * Per the project rules: 4th-year students need a supervisor whose
 * h-index ≥ 3. PROFESSORs are auto-wrapped by TeacherFactory into
 * TeacherResearcher. Students who do thesis work are wrapped into
 * StudentResearcher. Employees doing research are wrapped into
 * EmployeeResearcher.

 * h-index is recomputed from the citations of the papers list, but
 * we cache it in the {@code hIndex} field so {@link #getHIndex()}
 */
public abstract class ResearcherDecorator implements Researcher, Serializable {

    private static final long serialVersionUID = 1L;

    protected final User wrapped;
    protected int hIndex;
    protected final List<ResearchPaper> papers   = new ArrayList<>();
    protected final List<ResearchProject> projects = new ArrayList<>();

    protected ResearcherDecorator(User wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("Cannot wrap a null User as Researcher");
        }
        this.wrapped = wrapped;
        this.hIndex = 0;
    }

    public User getWrappedUser() {
        return wrapped;
    }

    @Override
    public int getHIndex() {
        return hIndex;
    }

    @Override
    public List<ResearchPaper> getPapers() {
        return papers;
    }

    @Override
    public List<ResearchProject> getProjects() {
        return projects;
    }

    @Override
    public void addPaper(ResearchPaper paper) {
        if (paper == null) return;
        papers.add(paper);
        recomputeHIndex();
    }

    @Override
    public void joinProject(ResearchProject project) {
        if (project == null) return;
        if (!projects.contains(project)) {
            projects.add(project);
        }
        // ResearchProject.addParticipant(this) is called by the controller
        // to keep the link symmetric — we don't double-add here.
    }

    @Override
    public void printPapers(Comparator<ResearchPaper> comparator) {
        List<ResearchPaper> copy = new ArrayList<>(papers);
        if (comparator != null) {
            copy.sort(comparator);
        }
        System.out.println("Papers of " + wrapped.getFullName() + ":");
        for (ResearchPaper p : copy) {
            System.out.println("  - " + p);
        }
    }

    public void refreshHIndex() {
        recomputeHIndex();
    }

    /**
     - Standard h-index: largest h such that at least h papers
     - have at least h citations each.
     */
    protected void recomputeHIndex() {
        List<Integer> citations = new ArrayList<>();
        for (ResearchPaper p : papers) {
            citations.add(p.getCitations());
        }
        citations.sort((a, b) -> b - a); // descending
        int h = 0;
        for (int i = 0; i < citations.size(); i++) {
            if (citations.get(i) >= i + 1) {
                h = i + 1;
            } else {
                break;
            }
        }
        this.hIndex = h;
    }
}