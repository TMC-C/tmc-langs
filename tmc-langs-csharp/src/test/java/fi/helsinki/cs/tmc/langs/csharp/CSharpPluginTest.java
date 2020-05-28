package fi.helsinki.cs.tmc.langs.csharp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import fi.helsinki.cs.tmc.langs.abstraction.Strategy;
import fi.helsinki.cs.tmc.langs.abstraction.ValidationResult;
import fi.helsinki.cs.tmc.langs.domain.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import fi.helsinki.cs.tmc.langs.domain.TestResult;
import fi.helsinki.cs.tmc.langs.io.StudentFilePolicy;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;

import org.apache.commons.io.FileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class CSharpPluginTest {

    private CSharpPlugin csPlugin;
    
    public CSharpPluginTest() {
        TestUtils.skipIfNotAvailable("dotnet");
    }

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
    public void testCSharpIsRecognizedAsCSharp() {
        Path csharpProjectPath = TestUtils.getPath(getClass(), "PassingProject");
        assertTrue(csPlugin.isExerciseTypeCorrect(csharpProjectPath));
    }

    @Test
    public void testPythonIsNotRecognizedAsCSharp() {
        Path pythonProjectPath = TestUtils.getPath(getClass(), "PythonProject");
        assertFalse(csPlugin.isExerciseTypeCorrect(pythonProjectPath));
    }

    @Test
    public void getStudentFilePolicyReturnsCSharpStudentFilePolicy() {
        StudentFilePolicy policy = this.csPlugin.getStudentFilePolicy(Paths.get(""));
        assertTrue(policy instanceof CSharpStudentFilePolicy);
    }

    @Test
    public void testJarPathExists() {
        Path jarPath = csPlugin.getJarPath();
        assertNotNull(jarPath);
    }

    @Test
    public void testDownloadingRunner() throws IOException {
        Path jarPath = csPlugin.getJarPath();
        Path dirPath = jarPath.resolve(Paths.get("tmc-csharp-runner"));

        if (Files.exists(dirPath)) {
            FileUtils.deleteDirectory(dirPath.toFile());
        }

        Path projectPath = TestUtils.getPath(getClass(), "PassingProject");
        RunResult runResult = this.csPlugin.runTests(projectPath);
        assertNotNull(runResult);
        assertEquals(runResult.toString(), RunResult.Status.PASSED, runResult.status);

        assertTrue(Files.exists(dirPath));
        assertTrue(Files.exists(jarPath.resolve(Paths.get("tmc-csharp-runner", "Bootstrap.dll"))));
    }

    @Test
    public void testRunTestsPassing() {
        Path path = TestUtils.getPath(getClass(), "PassingProject");

        RunResult runResult = this.csPlugin.runTests(path);
        assertNotNull(runResult);
        System.out.println(runResult);
        assertEquals(runResult.toString(), RunResult.Status.PASSED, runResult.status);

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
        assertNotNull(runResult);
        assertEquals(runResult.toString(), RunResult.Status.TESTS_FAILED, runResult.status);

        TestResult testResult = runResult.testResults.get(0);
        assertTrue(!testResult.isSuccessful());
        assertTrue(!testResult.getMessage().isEmpty());
        assertEquals(0, testResult.points.size());
    }

    @Test
    public void testRunTestsNonCompiling() {
        Path path = TestUtils.getPath(getClass(), "NonCompilingProject");
        RunResult runResult = this.csPlugin.runTests(path);
        assertNotNull(runResult);
        assertEquals(runResult.toString(), RunResult.Status.COMPILE_FAILED, runResult.status);
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

    @Test
    public void testCleanRemovesBinAndObj() throws IOException {
        Path projectPath = TestUtils.getPath(getClass(), "PassingProject");
        this.csPlugin.runTests(projectPath);

        assertTrue(Files.exists(projectPath.resolve(Paths.get("src", "PassingSample", "bin"))));
        assertTrue(Files.exists(projectPath.resolve(Paths.get("src", "PassingSample", "obj"))));
        assertTrue(Files.exists(projectPath.resolve(Paths.get("test", "PassingSampleTests", "bin"))));
        assertTrue(Files.exists(projectPath.resolve(Paths.get("test", "PassingSampleTests", "obj"))));

        csPlugin.clean(projectPath);

        assertFalse(Files.exists(projectPath.resolve(Paths.get("src", "PassingSample", "bin"))));
        assertFalse(Files.exists(projectPath.resolve(Paths.get("src", "PassingSample", "obj"))));
        assertFalse(Files.exists(projectPath.resolve(Paths.get("test", "PassingSampleTests", "bin"))));
        assertFalse(Files.exists(projectPath.resolve(Paths.get("test", "PassingSampleTests", "obj"))));
    }
}
