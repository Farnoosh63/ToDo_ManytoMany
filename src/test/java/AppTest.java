import org.sql2o.*;
import org.junit.*;
import org.fluentlenium.adapter.FluentTest;
import org.junit.ClassRule;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import static org.assertj.core.api.Assertions.assertThat;
import static org.fluentlenium.core.filter.FilterConstructor.*;
import static org.junit.Assert.*;

public class AppTest extends FluentTest {
  public WebDriver webDriver = new HtmlUnitDriver();

  @Override
  public WebDriver getDefaultDriver() {
    return webDriver;
  }

  @ClassRule
  public static ServerRule server = new ServerRule();

  @Rule
  public DatabaseRule database = new DatabaseRule();

  @Test
  public void rootTest() {
    goTo("http://localhost:4567/");
    assertThat(pageSource()).contains("Todo list!");
  }

  @Test
  public void categoryIsCreatedTest() {
    goTo("http://localhost:4567/");
    fill("#name").with("Household chores");
    submit("#addCategory");
    assertThat(pageSource()).contains("Household chores");
  }

  @Test
  public void categoryAndTaskShowPageDisplays() {
    Category testCategory = new Category("Household chores");
    testCategory.save();
    Task testTask = new Task("Mow the lawn");
    testTask.save();
    testCategory.addTask(testTask);
    String url = String.format("http://localhost:4567/categories/%d", testCategory.getId());
    goTo(url);
    assertThat(pageSource()).contains("Mow the lawn");
  }

  @Test
  public void checkDeleteCategoryButton() {
    Category testCategory = new Category("Household chores");
    testCategory.save();
    submit("#deleteCategory");
    assertThat(pageSource()).doesNotContain("Household chores");
  }

  @Test
  public void checkDeleteTaskButton() {
    Category testCategory = new Category("Household chores");
    testCategory.save();
    Task task = new Task("Mow the lawn");
    task.save();
    testCategory.addTask(task);
    String url = String.format("http://localhost:4567/categories/%d", testCategory.getId());
    goTo(url);
    submit("#deleteTask");
    assertThat(pageSource()).doesNotContain("Mow the lawn");
  }

  @Test
  public void checkDeleteTaskWithMultipleTasks() {
    Category testCategory = new Category("Household chores");
    testCategory.save();
    Task firstTask = new Task("Mow the lawn");
    firstTask.save();
    Task secondTask = new Task("Walk the dog");
    secondTask.save();
    testCategory.addTask(secondTask);
    String url = String.format("http://localhost:4567/categories/%d", testCategory.getId());
    firstTask.delete();
    goTo(url);
    assertThat(pageSource()).doesNotContain("Mow the lawn");
  }
  @Test
  public void checkUpdatedTaskButton() {
    Category testCategory = new Category("Household chores");
    testCategory.save();
    Task testTask = new Task("Mow the lawn");
    testTask.save();
    testCategory.addTask(testTask);
    String url = String.format("http://localhost:4567/categories/%d/tasks/%d", testCategory.getId(), testTask.getId());
    goTo(url);
    fill("#description").with("Do the dishes");
    submit("#updateTask");
    assertThat(pageSource()).contains("Do the dishes");
  }

  @Test
  public void addAnotherCategoryToTask() {
    Category testCategory = new Category("Household chores");
    testCategory.save();
    Category secondCategory = new Category("Mom chores");
    Task testTask = new Task("Do the dishes");
    testTask.save();
    testCategory.addTask(testTask);
    String url = String.format("http://localhost:4567/categories/%d", testCategory.getId());
    goTo(url);
    click("a", withText("Assign Another Category"));
    String urlAssignPage = String.format("http://localhost:4567/categories/%d/tasks/%d/assign", testCategory.getId(), testTask.getId());
    goTo(urlAssignPage);
    assertThat(pageSource()).contains("Assign Another Category to the Task");
    fillSelect("#categoryId").withText("Please select category");
    // click("option", withText("Mom chores"));
    submit("#add_category");
    assertThat(pageSource()).contains("Mom chores");
  }
}
