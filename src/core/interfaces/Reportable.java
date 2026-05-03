package core.interfaces;

import java.time.LocalDateTime;
import java.util.Map;

public interface Reportable {
    String getReportId();

    String getTitle();
    void setTitle(String title);

    Map<String, Object> getData();


    LocalDateTime getGeneratedAt();
}
