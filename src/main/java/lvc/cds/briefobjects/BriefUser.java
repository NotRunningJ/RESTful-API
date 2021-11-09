package lvc.cds.briefobjects;

import org.json.JSONObject;

public class BriefUser {
    String name;
    String uri;


    public BriefUser() {
        setName("");
        setUri("");
    }

    public BriefUser(JSONObject json) {
        setName(json.getString("name"));
        setUri(json.getString("selfLink"));
    }

    public BriefUser(String name, String uri) {
        this.name = name;
        this.uri = uri;
    }

    public String getName() {
        return this.name;
    }

    public BriefUser setName(String name) {
        this.name = name;
        return this;
    }

    public String getUri() {
        return this.uri;
    }

    public BriefUser setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public BriefUser name(String name) {
        setName(name);
        return this;
    }

    public BriefUser uri(String uri) {
        setUri(uri);
        return this;
    }

    @Override
    public String toString() {
        return "{" +
            " name='" + getName() + "'" +
            ", uri='" + getUri() + "'" +
            "}";
    }

    
}
