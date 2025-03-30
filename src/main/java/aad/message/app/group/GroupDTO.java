package aad.message.app.group;


import java.time.LocalDateTime;

public class GroupDTO {
    public Long id;
    public String name;
    public String imageUrl;
    public LocalDateTime reminderStart;
    public Integer reminderFrequency;

    public GroupDTO(Group group) {
        this.id = group.id;
        this.name = group.name;
        this.imageUrl = group.imageUrl;
        this.reminderStart = group.reminderStart;
        this.reminderFrequency = group.reminderFrequency;
    }

    public static GroupDTO fromEntity(Group group) {
        return new GroupDTO(group);
    }
}