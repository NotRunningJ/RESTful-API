package lvc.cds;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import lvc.cds.briefobjects.BriefProject;
import lvc.cds.briefobjects.BriefTask;

public class User {
    private int id;
    private String name;
    private String selfLink;
    private ArrayList<BriefProject> projects;
    private ArrayList<BriefTask> tasks;
    private HashMap<String, String> userLinks;

    public User() {
        this.id = -1;
        this.name = "";
        this.selfLink = "";
        projects = new ArrayList<>();
        tasks = new ArrayList<>();
        userLinks = new HashMap<>();
    }

    public User(int id, String name, String selfLink, ArrayList<BriefProject> projects, ArrayList<BriefTask> tasks) {
        this.id = id;
        this.name = name;
        this.selfLink = selfLink;
        this.projects = projects;
        this.tasks = tasks;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<BriefTask> getTasks() {
        return tasks;
    }

    public ArrayList<BriefProject> getProjects() {
        return projects;
    }

    public User setProjects(ArrayList<String> projUserAssoc) {
        for (String string : projUserAssoc) {
            String id = string.substring(string.indexOf("=")+1, string.indexOf(":"));
            String name = (String) Database.projects.getJSON(id, "name");
            String uri = (String) Database.projects.getJSON(id, "selflink");
            projects.add(new BriefProject(name, uri));
        }
        return this;
    }

    public User setTasks(ArrayList<String> taskUserAssoc) {
        for (String string : taskUserAssoc) {
            String id = string.substring(string.indexOf("=")+1, string.indexOf(":"));
            String name = (String) Database.tasks.getJSON(id, "name");
            String uri = (String) Database.tasks.getJSON(id, "selflink");
            tasks.add(new BriefTask(name, uri));
        }
        return this;
    }

    public void addProject(BriefProject proj) {
        projects.add(proj);
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public User setId(int id) {
        this.id = id;
        return this;
    }

    public User setSelfLink() {
        selfLink = Main.BASE_URI + "users/" + this.id;
        return this;
    }

    public String getSelfLink() {
        return selfLink;
    }

    // Call this after setting selflink!!
    public User setUserLinks() {
        userLinks.put("getUser", selfLink);
        userLinks.put("deleteUser", selfLink);

        userLinks.put("changeUserName", selfLink + "/name");
        userLinks.put("getUserName", selfLink + "/name");

        userLinks.put("getProjects", selfLink + "/projects");
        userLinks.put("joinProject", selfLink + "/projects");
        userLinks.put("leaveProject", selfLink + "/projects/{projectid}");

        userLinks.put("assignTask", selfLink + "/tasks");
        userLinks.put("deleteTask", selfLink + "/tasks/{taskid}");
        userLinks.put("getTasks", selfLink + "/tasks");
        return this;
    }

    public HashMap<String,String> getLinks() {
        return userLinks;
    }

    public static User fullConversion(String id) {
        ArrayList<String> taskAssocs = Database.associations.getMatchingKeys("task=\\d+:user=" + id);
        ArrayList<String> projAssocs = Database.associations.getMatchingKeys("project=\\d+:user=" + id);
        JSONObject json = Database.users.get(id);
        return new User().setId(Integer.parseInt(id))
                        .setName(json.getString("name"))
                        .setTasks(taskAssocs)
                        .setProjects(projAssocs)
                        .setSelfLink()
                        .setUserLinks();
                         
    }
}
