package lvc.cds.briefobjects;


import org.json.JSONObject;

public class BriefTask {
    String name;
    String description;
    String status;
    String uri; 

    public BriefTask() {
        setName("");
        setDescription("");
        setStatus("Unassigned");
        setUri("");
    }
    
    public BriefTask(JSONObject json) {
        setName(json.getString("name"));
        setDescription(json.getString("description"));
        setStatus(json.getString("status"));
        setUri(json.getString("selfLink"));
    }
    public BriefTask(String name, String uri) {
        this.name = name;
        this.uri = uri;
    }

    public String getName() {
        return this.name;
    }

    public BriefTask setName(String name) {
        this.name = name;
        return this;
    }
    
    public BriefTask setStatus(String string) {
        status = string;
        return this;
    }

    public String getStatus(){
        return status;
    }

    public BriefTask setDescription(String string) {
        description = string;
        return this;
    }

    public String getUri() {
        return this.uri;
    }

    public BriefTask setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public BriefTask name(String name) {
        setName(name);
        return this;
    }

    public BriefTask uri(String uri) {
        setUri(uri);
        return this;
    }
    
}
