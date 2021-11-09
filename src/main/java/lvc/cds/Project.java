package lvc.cds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import lvc.cds.briefobjects.BriefUser;

public class Project {
    private int id;
    private String name;
    private String description;
    private String todoList;
    private String selfLink;
    private List<BriefUser> users;
    private HashMap<String, String> projectLinks;

    public Project() {
        id = -1;
        name = "";
        description = "";
        todoList = "";
        selfLink = Main.BASE_URI + "projects";
        users = new ArrayList<>();
        projectLinks = new HashMap<>();
    }

    public Project(int id, String name, String description, String todoList, String selfLink, List<BriefUser> users) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.todoList = todoList;
        this.users = users;
        this.selfLink = selfLink;
    }

    public Project setId(int id) {
        this.id = id;
        return this;
    }

    public int getId() {
        return this.id;
    }

    public String getTodoList() {
        return this.todoList;
    } 
    
    public Project setTodoList(String todoList) {
        this.todoList = todoList;
        return this;
    }

    public Project setTodoList(ArrayList<String> projTlAssocs) {
        if (projTlAssocs.isEmpty())
            this.todoList = "";
        else {
            String tlid = projTlAssocs.get(0).substring(projTlAssocs.get(0).lastIndexOf("=")+1);
            this.todoList = (String)Database.todolists.getJSON(tlid, "selfLink");
        }
        return this;
    }

    public List<BriefUser> getUsers() {
        return this.users;
    }

    public Project setUsers(List<BriefUser> users) {
        this.users = users;
        return this;
    }

    public Project setUsers(ArrayList<String> projUserAssocs) {
        for (String string : projUserAssocs) {
            String id = string.substring(string.lastIndexOf("=")+1);
            String name = (String)Database.users.getJSON(id, "name");
            String uri = (String)Database.users.getJSON(id, "selfLink");
            users.add(new BriefUser(name, uri));
        }
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Project setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Project setDescription(String description) {
        this.description = description;
        return this;
    }

    public HashMap<String, String> getLinks() {
        return projectLinks;
    }

    public Project setSelfLink() {
        selfLink = Main.BASE_URI+"projects/" + this.id;
        return this;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public Project setProjectLinks() {
        projectLinks.put("getProject", selfLink);
        projectLinks.put("deleteProject", selfLink);

        projectLinks.put("changeName", selfLink + "/name");
        projectLinks.put("changeDescription", selfLink + "/description");

        projectLinks.put("getUsers", selfLink + "/users"); 
        projectLinks.put("addUser", selfLink + "/users"); 
        projectLinks.put("removeUser", selfLink + "/users/{id}");

        projectLinks.put("addTodoList", selfLink + "/todoList");
        projectLinks.put("removeTodoList", selfLink + "/todoList");
        return this;
    }

    public static Project fullConversion(String id) {
        ArrayList<String> userIDs = Database.associations.getMatchingKeys("project=" + id + ":user=");
        ArrayList<String> todoListID = Database.associations.getMatchingKeys("project=" + id + ":todolist=");
        JSONObject json = Database.projects.get(id);
        return new Project().setId(Integer.parseInt(id))
                            .setDescription(json.getString("description"))
                            .setTodoList(todoListID)
                            .setUsers(userIDs)
                            .setName(json.getString("name"))
                            .setSelfLink()
                            .setProjectLinks();                       
    }
}
