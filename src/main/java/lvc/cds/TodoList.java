package lvc.cds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.*;

import org.json.JSONObject;

import lvc.cds.briefobjects.BriefTask;
import lvc.cds.briefobjects.BriefUser;

public class TodoList {
    private String id;
    private String description;
    private String name;
    private ArrayList<BriefUser> users;
    private ArrayList<BriefTask> tasks;
    private String project;
    private String selfLink;
    private TreeMap<String, String> links;

    public TodoList(){
        this.id = "-1";
        this.description = "";
        this.name = "";
        this.users = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.project = "";
        this.selfLink = "";
        this.links = new TreeMap<>();
    }
    
    public static TodoList fullConversion(String id){
        ArrayList<String> userIDs = Database.associations.getMatchingKeys("todolist=" + id + ":user=");
        ArrayList<String> taskIDs = Database.associations.getMatchingKeys("todolist=" + id + ":task=");
        String projectID = Database.associations.getMatchingKeys("project=\\d+:todolist=" + id).isEmpty() ? "" : Database.associations.getMatchingKeys("project=\\d+:todolist=" + id).get(0);
        JSONObject json = Database.todolists.get(id);
        return new TodoList().setId(id)
            .setDescription(json.getString("description"))
            .setName(json.getString("name"))
            .setProject(projectID.equals("") ? "" : Main.BASE_URI + "projects/" + projectID.substring(projectID.indexOf("=") + 1, projectID.indexOf(":")))
            .setUsers(userIDs.stream()
                        .map((String s) -> new BriefUser(Database.users.get(s.substring(s.lastIndexOf("=") + 1))))
                        .collect(Collectors.toCollection(ArrayList::new)))
            .setTasks(taskIDs.stream()
                        .map((String s) -> new BriefTask(Database.tasks.get(s.substring(s.lastIndexOf("=") + 1))))
                        .collect(Collectors.toCollection(ArrayList::new)))
            .setSelfLink()
            .setOptionLinks();
    }

    public TodoList setId(String newID){
        if(id.equals("-1")){
            id = newID;
            return this;
        }else{
            return this;
        }
    }

    public String getId(){
        return id;
    }

    public TodoList setDescription(String newDescription){
        description = newDescription;
        return this;
    }

    public String getDescription(){
        return description;
    }

    public TodoList setName(String newName) {
        name = newName;
        return this;
    }

    public String getName() {
        return name;
    }

    public TodoList setUsers(ArrayList<BriefUser> newUsers){
        users = newUsers;
        return this;
    }

    public List<BriefUser> getUsers(){
        return users;
    }

    public TodoList setTasks(ArrayList<BriefTask> newTasks){
        tasks = newTasks;
        return this;
    }

    public List<BriefTask> getTasks(){
        return tasks;
    }

    public TodoList setProject(String projectURI){
        project = projectURI;
        return this;
    }

    public String getProject(){
        return project;
    }

    public TodoList setSelfLink(){
        selfLink = Main.BASE_URI + "todolists/" + id;
        return this;
    }

    public String getSelfLink(){
        return selfLink;
    }

    public String getSelfLink(String parameters){
        return selfLink + parameters;
    }

    public TodoList setOptionLinks() {
        links.clear();
        links.put("getTodolist", getSelfLink());
        links.put("deleteTodolist", getSelfLink());

        links.put("getDecription", getSelfLink("/description"));
        links.put("changeDescription", getSelfLink("/description"));

        links.put("getName", getSelfLink("/name"));
        links.put("changeName", getSelfLink("/name"));

        links.put("getProject", getSelfLink("/project"));
        links.put("changeProject", getSelfLink("/project"));

        links.put("getTasks", getSelfLink("/tasks"));
        links.put("addTask", getSelfLink("/tasks"));

        links.put("getUsers", getSelfLink("/users"));
        links.put("addUser", getSelfLink("/users"));

        return this;
    }

    public Map<String, String> getLinks(){
        return links;
    }

    

    public JSONObject todolistToMiniJSON(){
        return new JSONObject().put("id", id)
                            .put("name", name)
                            .put("description", description)
                            .put("selfLink", selfLink);
                            
    }
}
