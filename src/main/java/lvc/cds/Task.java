package lvc.cds;

import java.util.ArrayList;
import java.util.HashMap;


import org.json.JSONObject;

import lvc.cds.briefobjects.BriefUser;

public class Task {

    private int id;
    private String name;
    private String description;
    private String status;
    private BriefUser user;      
    private String selfLink;
    private HashMap<String, String> links;

    public Task() {
        this.id = -1;
        this.name = "";
        this.description = "";
        this.status = "Unassigned";
        this.user = new BriefUser();
        this.selfLink = "";
        this.links = new HashMap<String, String>();
    }

    public Task(int id, String name, String description, String status, BriefUser user, String todoList) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.user = user;
        this.selfLink = "";
        this.links = new HashMap<String, String>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public BriefUser getUser() {
        return user;
    }

    public Task setId(int id) {
        this.id = id;
        return this;
    }

    public Task setName(String name) {
        this.name = name;
        return this;
    }

    public Task setDescription(String description) {
        this.description = description;
        return this;
    }

    public Task setStatus(String status) {
        this.status = status;
        return this;
    }

    public Task setUser(BriefUser user) {
        this.user = user;
        return this;
    }

    public Task setSelfLink(){
        selfLink = Main.BASE_URI+"tasks/"+id;
        return this;
    }

    public String getSelfLink(){
        return selfLink;
    }

    // initializes and returns the links available for a task
    public HashMap<String, String> getLinks() {
        return links;
    }

    public Task setLinks(){
        links.clear();
        links.put("getTask", Main.BASE_URI+"tasks/"+id);
        links.put("deleteTask", Main.BASE_URI+"tasks/"+id);

        links.put("getStatus", Main.BASE_URI+"tasks/"+id+"status/");
        links.put("changeStatus", Main.BASE_URI+"tasks/"+id+"status/");

        links.put("getdescription", Main.BASE_URI+"tasks/"+id+"description/");
        links.put("changedescription", Main.BASE_URI+"tasks/"+id+"description/");

        links.put("getName", Main.BASE_URI+"tasks/"+id+"name/");
        links.put("changeName", Main.BASE_URI+"tasks/"+id+"name/");

        links.put("getuser", Main.BASE_URI+"tasks/"+id+"user/");
        links.put("assignUser", Main.BASE_URI+"tasks/"+id+"user/");
        links.put("removeUserFromTask", Main.BASE_URI+"tasks/"+id+"user/");
        return this;

    }


    public static Task fullConversion(String id) {

        JSONObject json = Database.tasks.get(id);
        Task t = new Task().setId(Integer.parseInt(id))
                         .setDescription(json.getString("description"))
                         .setName(json.getString("name"))
                         .setStatus(json.getString("status"))
                         .setSelfLink()
                         .setLinks(); 
        ArrayList<String> user = Database.associations.getMatchingKeys("task=" + id + ":user=");
        if(user.size() != 0) {
            String entireAssoc = user.get(0);
            String userId = entireAssoc.substring(entireAssoc.lastIndexOf("=") + 1);
            var temp = Database.users.get(userId);
            BriefUser u = new BriefUser();
            u.setName(temp.getString("name"));
            u.setUri(Main.BASE_URI+"users/"+userId);
            t.setUser(u);
        }
        return t;
    }


    public JSONObject taskToMiniJSON() {
        return new JSONObject().put("id", id)
                               .put("description", description)
                               .put("name", name)
                               .put("status", status)
                               .put("selflink", selfLink);
    }
    

}
