package fi.helsinki.cs.tmc.langs.csharp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import fi.helsinki.cs.tmc.langs.domain.TestDesc;
import fi.helsinki.cs.tmc.langs.utils.TestUtils;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

public class CSharpExerciseDescParserTest {

    private final Path allPassedSampleDir;
    private final Path someFailedSampleDir;

    public CSharpExerciseDescParserTest() {
        Path desc_samples_dir = TestUtils.getPath(getClass(), "Desc_samples");
        allPassedSampleDir = desc_samples_dir.resolve("all_tests_passed_sample");
        someFailedSampleDir = desc_samples_dir.resolve("some_tests_failed_sample");
    }

    private void testDescAsExpected(TestDesc desc, String name, String[] points) {
        assertEquals(name, desc.name);
        assertArrayEquals(points, desc.points.toArray());
    }

    @Test
    public void testDescParsePointsCorrectly() throws IOException {
        ImmutableList<TestDesc> descs = new CSharpExerciseDescParser(allPassedSampleDir).parse();

        testDescAsExpected(descs.get(0), "PassingSampleTests.ProgramTest.TestGetName", new String[]{"1", "1.1"});
        testDescAsExpected(descs.get(1), "PassingSampleTests.ProgramTest.TestGetYear", new String[]{"1", "1.2"});
    }

    @Test
    public void testThatParseWorksForNoPointsJson() throws IOException {
        ImmutableList<TestDesc> descs = new CSharpExerciseDescParser(someFailedSampleDir).parse();

        testDescAsExpected(descs.get(0), 
                "PartiallyFailingSampleTests.ProgramTest.TestCheckFinePassed", 
                new String[]{"2", "2.2"});
        testDescAsExpected(descs.get(1), 
                "PartiallyFailingSampleTests.ProgramTest.TestCheckFinePassed", new String[]{});
    }
}
