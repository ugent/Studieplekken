package be.ugent.blok2.model;

public class Authority implements Cloneable{
    private int authorityId;
    private String name;
    private String description;

    public Authority(int authorityId, String name, String description) {
        this.authorityId = authorityId;
        this.name = name;
        this.description = description;
    }
    public Authority(){}

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(int authorityId) {
        this.authorityId = authorityId;
    }
}
