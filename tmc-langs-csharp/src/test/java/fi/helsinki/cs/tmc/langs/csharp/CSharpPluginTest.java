package fi.helsinki.cs.tmc.langs.csharp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.abstraction.Strategy;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class CSharpPluginTest {

    private CSharpPlugin csPlugin;

    @Before
    public void setUp() {
        this.csPlugin = new CSharpPlugin();
        System.setProperty("TEST_ENV", "TEST");
    }

    @Test
    public void testGetLanguageName() {
        assertEquals("csharp", this.csPlugin.getPluginName());
    }

    @Test
    public void getStudentFilePolicyReturnsPython3StudentFilePolicy() {
        StudentFilePolicy policy = this.csPlugin.getStudentFilePolicy(Paths.get(""));

        assertTrue(policy instanceof CSharpStudentFilePolicy);
    }

    @Test
    public void testRunTestsPassing() {
        Path path = TestUtils.getPath(getClass(), "PassingProject");
        RunResult runResult = this.csPlugin.runTests(path);
        assertEquals(RunResult.Status.PASSED, runResult.status);

        TestResult testResult = runResult.testResults.get(0);
        assertTrue(testResult.isSuccessful());
        assertEquals("PassingSampleTests.ProgramTest.TestGetYear", testResult.getName());
        assertEquals(2, testResult.points.size());
        assertTrue(testResult.points.contains("1"));
        assertTrue(testResult.points.contains("1.2"));
        assertEquals("", testResult.getMessage());
        assertEquals(0, testResult.getException().size());
    }

    @Test
    public void testRunTestsFailing() {
        Path path = TestUtils.getPath(getClass(), "FailingProject");
        RunResult runResult = this.csPlugin.runTests(path);
        assertEquals(RunResult.Status.TESTS_FAILED, runResult.status);

        TestResult testResult = runResult.testResults.get(0);
        assertTrue(!testResult.isSuccessful());
        assertTrue(!testResult.getMessage().isEmpty());
        assertEquals(0, testResult.points.size());
        assertEquals(3, testResult.getException().size());
    }

    @Test
    public void testScanExercise() {
        Path path = TestUtils.getPath(getClass(), "PassingProject");
        ExerciseDesc testDesc = this.csPlugin.scanExercise(path, "cs-tests").get();
        assertEquals("cs-tests", testDesc.name);
        assertEquals("PassingSampleTests.ProgramTest.TestGetName", testDesc.tests.get(0).name);
        assertEquals(2, testDesc.tests.get(0).points.size());
    }

    @Test()
    public void testCheckCodeStyleStratery() {
        Path path = TestUtils.getPath(getClass(), "PassingProject");
        ValidationResult result = this.csPlugin.checkCodeStyle(path, new Locale("en"));
        assertTrue(result.getStrategy() == Strategy.DISABLED);
    }

}
