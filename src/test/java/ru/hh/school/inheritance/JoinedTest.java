package ru.hh.school.inheritance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import ru.hh.school.BaseTest;
import ru.hh.school.TestHelper;
import ru.hh.school.TransactionRule;
import ru.hh.school.inheritance.joined.Applicant;
import ru.hh.school.inheritance.joined.Employer;
import ru.hh.school.inheritance.joined.User;
import java.util.Set;
import java.util.stream.Collectors;

public class JoinedTest extends BaseTest {

  @Rule
  public TransactionRule transactionRule = new TransactionRule(sessionFactory);

  @BeforeClass
  public static void createTable() {
    TestHelper.executeScript(pg.getPostgresDatabase(), "create_user_inheritance_joined.sql");
  }

  @Before
  public void clearTable() {
    TestHelper.execute(pg.getPostgresDatabase(), "delete from user_inheritance_joined");
    TestHelper.execute(pg.getPostgresDatabase(), "delete from employer_inheritance_joined");
    TestHelper.execute(pg.getPostgresDatabase(), "delete from applicant_inheritance_joined");
  }

  /**
   * ToDo создайте нужные таблицы в файле scripts/create_user_inheritance_joined.sql
   *
   * https://docs.jboss.org/hibernate/orm/5.3/userguide/html_single/Hibernate_User_Guide.html#entity-inheritance-joined-table
   */
  @Test
  public void saveDifferentTypes() {
    User user = new User();
    user.setName("John Doe");
    getSession().persist(user);

    Employer employer = new Employer();
    employer.setName("John Employer");
    employer.setCompany("HH");
    getSession().persist(employer);

    Applicant applicant = new Applicant();
    applicant.setName("John Applicant");
    applicant.setPosition("Director");
    getSession().persist(applicant);

    // обратите внимание, что в данном случае выполняется по 2 insert-а для подтипов
    assertEquals(5L, getInsertCount());
  }

  @Test
  public void polymorphicCallsShouldWork() {
    saveDifferentTypes();

    Set<String> userGreetings = getSession().createQuery("from UserJoined", User.class).stream()
      .map(User::generateGreeting)
      .collect(Collectors.toSet());

    // обратите внимание на сгенирированный хибернейтом запрос (импользуются join-ы)
    assertEquals(1L, getSelectCount());
    assertTrue(userGreetings.contains("Hello, John Doe"));
    assertTrue(userGreetings.contains("Hello, John Employer from HH"));
    assertTrue(userGreetings.contains("Hello, John Applicant the Director"));
  }

}
