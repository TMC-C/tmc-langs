package fi.helsinki.cs.tmc.langs.testscanner;

import com.google.common.base.Optional;

import fi.helsinki.cs.tmc.langs.ClassPath;
import fi.helsinki.cs.tmc.langs.ExerciseDesc;
import fi.helsinki.cs.tmc.langs.utils.SourceFiles;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestScanner {

    private JavaCompiler compiler;
    private StandardJavaFileManager fileManager;

    public TestScanner() {
        compiler = ToolProvider.getSystemJavaCompiler();
        fileManager = compiler.getStandardFileManager(null, null, null);
    }

    public Optional<ExerciseDesc> findTests(ClassPath classPath, SourceFiles sourceFiles, String exerciseName) {
        if (sourceFiles.isEmpty()) {
            return Optional.absent();
        }

        List<String> options = new ArrayList<>();
        options.add("-classpath");
        options.add(classPath.toString());
        options.add("-proc:only");

        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                null,
                null,
                options,
                null,
                fileManager.getJavaFileObjectsFromFiles(sourceFiles.getSources()));

        TestMethodAnnotationProcessor processor = new TestMethodAnnotationProcessor();
        task.setProcessors(Arrays.asList(processor));
        if (!task.call()) {
            throw new RuntimeException("Compilation failed");
        }

        return Optional.of(new ExerciseDesc(exerciseName, processor.getTestDescsSortedByName()));
    }
}
