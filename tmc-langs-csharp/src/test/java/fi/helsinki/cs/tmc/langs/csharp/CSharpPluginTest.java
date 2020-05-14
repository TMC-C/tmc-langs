package fi.helsinki.cs.tmc.langs.csharp;

import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CSharpPluginTest {

    private CSharpPlugin csPlugin;

    @Before
    public void setUp() {
        this.csPlugin = new CSharpPlugin();
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
