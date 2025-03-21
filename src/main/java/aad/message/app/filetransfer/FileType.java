package aad.message.app.filetransfer;

public enum FileType {
    PROFILE_PICTURE("pf"),
    GROUP_PICTURE("gp"),
    MESSAGE_AUDIO("ma");

    private final String shortName;

    FileType(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }
}

