package aad.message.app.group;

import java.time.LocalDateTime;

public class ReminderUpdateDTO {
    private LocalDateTime reminderStart;
    private Integer reminderFrequency;

    public LocalDateTime getReminderStart() {
        return reminderStart;
    }

    public Integer getReminderFrequency() {
        return reminderFrequency;
    }

}

