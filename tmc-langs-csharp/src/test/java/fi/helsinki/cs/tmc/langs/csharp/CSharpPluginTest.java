package fi.helsinki.cs.tmc.langs.csharp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

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

    }

    @Test
    public void testRunTestsFailing() {
        Path path = TestUtils.getPath(getClass(), "FailingProject");
        RunResult runResult = this.csPlugin.runTests(path);
        assertEquals(RunResult.Status.TESTS_FAILED, runResult.status);
    }
}
