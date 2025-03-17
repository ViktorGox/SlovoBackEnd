package aad.message.app.group;

public class GroupDTO {
    public Long id;
    public String name;
    public String imageUrl;

    public GroupDTO(Group group) {
        this.id = group.id;
        this.name = group.name;
        this.imageUrl = group.imageUrl;
    }

    public static GroupDTO fromEntity(Group group) {
        return new GroupDTO(group);
    }
}