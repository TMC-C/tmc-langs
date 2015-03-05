package fi.helsinki.cs.tmc.langs.ant;

import com.google.common.base.Throwables;
import fi.helsinki.cs.tmc.langs.*;
import fi.helsinki.cs.tmc.langs.utils.TestResultParser;
import fi.helsinki.cs.tmc.stylerunner.CheckstyleRunner;
import fi.helsinki.cs.tmc.stylerunner.exception.TMCCheckstyleException;
import fi.helsinki.cs.tmc.stylerunner.validation.ValidationResult;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import java.util.Locale;
import java.util.logging.Level;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

public class AntPlugin extends LanguagePluginAbstract {

    private static final Logger log = Logger.getLogger(AntPlugin.class.getName());
    private final String testDir = File.separatorChar + "test";
    private final String resultsFile = File.separatorChar + "results.txt";
    private TestResultParser resultParser = new TestResultParser();

    @Override
    public String getLanguageName() {
        return "apache-ant";
    }

    @Override
    public ExerciseDesc scanExercise(Path path, String exerciseName) {
        if (!isExerciseTypeCorrect(path)) {
            return null;
        }

        List<String> output = startProcess(buildTestScannerArgs(path, null));
        String outputString = "";

        for (String line : output) {
            outputString += line;
        }
        return resultParser.parseScannerOutput(outputString, exerciseName);
    }

    @Override
    protected boolean isExerciseTypeCorrect(Path path) {
        return new File(path.toString() + File.separatorChar + "build.xml").exists();
    }

    @Override
    public ValidationResult checkCodeStyle(Path path) {
        try {
            CheckstyleRunner runner = new CheckstyleRunner(path.toFile(), new Locale("fi"));
            return runner.run();
        } catch (TMCCheckstyleException ex) {
            log.log(Level.SEVERE, "Error running checkstyle:", ex);
            return null;
        }
    }

    @Override
    public RunResult runTests(Path path) {
        buildAntProject(path);
        List<String> runnerArgs = buildTestRunnerArgs(path);
        startProcess(runnerArgs);

        File resultFile = new File(path.toString() + resultsFile);
        return resultParser.parseTestResult(resultFile);
    }

    /**
     * Runs the build.xml file for the the given exercise.
     *
     * @param path The file path of the exercise directory.
     */
    public void buildAntProject(Path path) {
        File buildFile = new File(path.toString() + File.separatorChar + "build.xml");
        Project buildProject = new Project();
        buildProject.setUserProperty("ant.file", buildFile.getAbsolutePath());
        buildProject.init();
        buildProject.setBaseDir(path.toAbsolutePath().toFile());
        
        DefaultLogger logger = new DefaultLogger();
        logger.setErrorPrintStream(System.err);
        logger.setOutputPrintStream(System.out);
        logger.setMessageOutputLevel(Project.MSG_ERR);
        buildProject.addBuildListener(logger);

        try {
            buildProject.fireBuildStarted();
            
            ProjectHelper helper = ProjectHelper.getProjectHelper();
            buildProject.addReference("ant.projectHelper", helper);
            helper.parse(buildProject, buildFile);
            buildProject.executeTarget("compile-test");
            buildProject.fireBuildFinished(null);
        } catch (BuildException e) {
            buildProject.fireBuildFinished(e);
        }
    }

    private List<String> buildTestScannerArgs(Path path, ClassPath classPath, String... args) {
        List<String> scannerArgs = new ArrayList<>();

        scannerArgs.add("java");
        scannerArgs.add("-cp");

        if (classPath == null) {
            scannerArgs.add(generateClassPath(path).toString());
        } else {
            scannerArgs.add(classPath.toString());
        }

        scannerArgs.add("fi.helsinki.cs.tmc.testscanner.TestScanner");
        path = path.toAbsolutePath();
        scannerArgs.add(path.toString() + testDir);

        if (args != null) {
            for (String arg : args) {
                scannerArgs.add(arg);
            }
        }

        return scannerArgs;
    }

    private List<String> buildTestRunnerArgs(Path path) {
        List<String> runnerArgs = new ArrayList<>();
        List<String> testMethods;

        runnerArgs.add("java");
        runnerArgs.add("-Dtmc.test_class_dir=" + path.toString() + testDir);
        runnerArgs.add("-Dtmc.results_file=" + path.toString() + resultsFile);
        //runnerArgs.add("-Dfi.helsinki.cs.tmc.edutestutils.defaultLocale=" + locale);

        if (endorsedLibsExists(path)) {
           runnerArgs.add("-Djava.endorsed.dirs=" + createPath(path, "lib", "endorsed"));
        }

        ClassPath classPath = generateClassPath(path);

        runnerArgs.add("-cp");
        runnerArgs.add(classPath.toString());
        runnerArgs.add("fi.helsinki.cs.tmc.testrunner.Main");

        testMethods = startProcess(buildTestScannerArgs(path, classPath, "--test-runner-format"));

        for (String testMethod : testMethods) {
            runnerArgs.add(testMethod);
        }

        return runnerArgs;
    }

    private ClassPath generateClassPath(Path path) {
        ClassPath classPath = new ClassPath(path.toAbsolutePath());
        classPath.addDirAndContents(createPath(path, "lib"));
        classPath.add(createPath(path, "build", "test", "classes"));
        classPath.add(createPath(path, "build", "classes"));

        return classPath;
    }

    private boolean endorsedLibsExists(Path path) {
        File endorsedDir = createPath(path, "lib", "endorsed").toFile();
        return endorsedDir.exists() && endorsedDir.isDirectory();
    }

    private Path createPath(Path basePath, String... subDirs) {
        String path = basePath.toAbsolutePath().toString();

        for (String subDir : subDirs) {
            path += File.separatorChar + subDir;
        }

        return Paths.get(path);
    }
}
