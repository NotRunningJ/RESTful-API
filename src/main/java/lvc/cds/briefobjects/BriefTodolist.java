package lvc.cds.briefobjects;

import org.json.JSONObject;

public class BriefTodolist {
    private String description;
    private String uri;
    private String name;

    // public BriefTodolist(String description, String uri) {
    //     this.description = description;
    //     this.uri = uri;
    // }

    public BriefTodolist(){
        setDescription("");
        setUri("");
        setName("");
    }
    
    public BriefTodolist(JSONObject todoJSON){
        setDescription(todoJSON.getString("description"));
        setUri(todoJSON.getString("selfLink"));
        setName(todoJSON.getString("name"));
    }

    public String getDescription() {
        return description;
    }

    public BriefTodolist setDescription(String description) {
        this.description = description;
        return this;
    }
    
    public String getUri() {
        return uri;
    }

    public BriefTodolist setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getName() {
        return name;
    }

    public BriefTodolist setName(String name) {
        this.name = name;
        return this;
    }

    
}
