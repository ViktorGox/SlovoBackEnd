package aad.message.app.group;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "\"group\"")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String name;

    @Column(name = "image_url")
    public String imageUrl;

    @Column(name = "reminder_start")
    public LocalDateTime reminderStart;

    @Column(name = "reminder_frequency")
    public Duration reminderFrequency;
}
