import java.util.HashMap;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import static spark.Spark.*;
import java.util.ArrayList;
import java.util.List;

public class App {
  public static void main(String[] args) {

    ProcessBuilder process = new ProcessBuilder();
       Integer port;
       if (process.environment().get("PORT") != null) {
           port = Integer.parseInt(process.environment().get("PORT"));
       } else {
           port = 4567;
       }

      setPort(port);

    staticFileLocation("/public");
    String layout = "templates/layout.vtl";

    get("/", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      model.put("categories", Category.all());
      model.put("template", "templates/index.vtl");
      return new ModelAndView(model, layout);
    }, new VelocityTemplateEngine());

    // process form
    post("/", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      String inputtedString = request.queryParams("name");
      Category category = new Category(inputtedString);
      category.save();
      response.redirect("/");
      return null;
    });

    get("/categories/:id", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      Category category = Category.find(Integer.parseInt(request.params(":id")));
      model.put("category", category);
      model.put("template", "templates/category.vtl");
      return new ModelAndView(model, layout);
    }, new VelocityTemplateEngine());

    post("/tasks", (request,response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      Category category = Category.find(Integer.parseInt(request.queryParams("categoryId")));
      String description = request.queryParams("description");
      Task newTask = new Task(description);
      newTask.save();
      category.addTask(newTask);
      model.put("category", category); // calling $category.getName() in category tasks success page
      model.put("template", "templates/category-tasks-success.vtl");
      return new ModelAndView(model, layout);
    }, new VelocityTemplateEngine());

//for update task
    post("/tasks/:id", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      int categoryId = Integer.parseInt(request.queryParams("category_id"));
      Category category = Category.find(categoryId);

      int taskId = Integer.parseInt(request.params(":id"));
      Task task = Task.find(taskId);
      String description = request.queryParams("description");
      task.update(description);
      model.put("category", category);

      String url = String.format("/categories/%d", category.getId());
      response.redirect(url);
      return null;
    });

//for showing the update page in task.vtl
    get("/categories/:category_id/tasks/:task_id", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      Task task = Task.find(Integer.parseInt(request.params(":task_id")));
      Integer categoryId = Integer.parseInt(request.params(":category_id"));
      Category category = Category.find(categoryId);
      model.put("category",category);
      model.put("task", task);
      model.put("template", "templates/task.vtl");
      return new ModelAndView(model, layout);
    }, new VelocityTemplateEngine());

//for deleting the task and reload the root page
    post("categories/:category_id/tasks/:task_id/delete", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      Task task = Task.find(Integer.parseInt(request.params(":task_id")));
      task.delete();
      Integer categoryId = Integer.parseInt(request.params(":category_id"));
      Category category = Category.find(categoryId);
      String url = String.format("/categories/%d", category.getId());
      response.redirect(url);
      return null;
    });

//for deleting the categories anc reload the root page
    post("/categories/:id/delete", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      Category categories = Category.find(Integer.parseInt(request.params(":id")));
      categories.delete();
      response.redirect("/");
      return null;
    });

//for showing the assign another category in assign.vtl
    get("/categories/:category_id/tasks/:task_id/assign", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      Task task = Task.find(Integer.parseInt(request.params(":task_id")));
      Integer categoryId = Integer.parseInt(request.params(":category_id"));
      Category category = Category.find(categoryId);
      model.put("categories", Category.all());
      model.put("template", "templates/assign.vtl");
      return new ModelAndView(model, layout);
    }, new VelocityTemplateEngine());

//for assigning another category to the task
    post("categories/:category_id/tasks/:task_id/assign", (request, response) -> {
      HashMap<String, Object> model = new HashMap<String, Object>();
      Task task = Task.find(Integer.parseInt(request.params(":task_id")));
      Integer categoryId = Integer.parseInt(request.queryParams("categoryId"));
      Category category = Category.find(categoryId);
      category.addTask(task);
      response.redirect("/");
      return null;
    });

  }
}
