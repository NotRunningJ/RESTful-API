package lvc.cds.briefobjects;

import org.json.JSONObject;

public class BriefProject {
    String name;
    String uri;

    public BriefProject() {
        setName("");
        setUri("");
    }

    public BriefProject(JSONObject json){
        setName(json.getString("name"));
        setUri(json.getString("selfLink"));
    }

    public BriefProject(String name, String uri) {
        this.name = name;
        this.uri = uri;
    }

    public String getName() {
        return this.name;
    }

    public BriefProject setName(String name) {
        this.name = name;
        return this;
    }

    public String getUri() {
        return this.uri;
    }

    public BriefProject setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public BriefProject name(String name) {
        setName(name);
        return this;
    }

    public BriefProject uri(String uri) {
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
