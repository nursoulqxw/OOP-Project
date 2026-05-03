package core.strategy;

import models.users.User;

public interface ReportStrategy {
    String generateReport(User user);
}
