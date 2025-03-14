package aad.message.app.group;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// TODO: Dummy group class for voice message creation, overwrite with real class.

@Entity
@Table(name = "\"group\"")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String name;

    public String imageUrl;

    @Column(name = "reminder_start")
    public LocalDateTime reminderStart;

    @Column(name = "reminder_frequency")
    public Integer reminderFrequency;

//    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<GroupUser> groupUsers;

}

