package org.air.services.test;

import TDS.Proctor.Services.EmbossFileService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class EmbossFileServiceTest {
    private EmbossFileService embossFileService;
    private String sample1FilePath;
    private String sample2FilePath;

    @Before
    public void setup() {
        embossFileService = new EmbossFileService();
        sample1FilePath = this.getClass().getResource("/emboss/sample1.brf").getFile();
        sample2FilePath = this.getClass().getResource("/emboss/sample1.brf").getFile();
    }

    @After
    public void tearDown() { }

    @Test
    public void shouldCombineFileName() {
        String combinedFilePath = embossFileService.getCombinedFileName("/usr/test/testfile.brf", "_suffix");

        assertTrue(combinedFilePath.equals("testfile_suffix.brf"));
    }

    @Test
    public void shouldCombineFileNamehWithMultiplePeriods() {
        String combinedFilePath = embossFileService.getCombinedFileName("/usr/test/testfile.something.brf", "_suffix");

        assertTrue(combinedFilePath.equals("testfile.something_suffix.brf"));
    }

    @Test
    public void shouldCombineFileNameWithNoExtension() {
        String combinedFilePath = embossFileService.getCombinedFileName("/usr/test/testfile", "_suffix");

        assertTrue(combinedFilePath.equals("testfile_suffix"));
    }

    @Test
    public void shouldCombineFileNameWithEmptySuffixProvided() {
        String combinedFilePath = embossFileService.getCombinedFileName("/usr/test/testfile.brf", "");

        assertTrue(combinedFilePath.equals("testfile.brf"));
    }

    @Test
    public void shouldCombineFileNameithNullSuffixProvided() {
        String combinedFilePath = embossFileService.getCombinedFileName("/usr/test/testfile.brf", null);

        assertTrue(combinedFilePath.equals("testfile.brf"));
    }

    @Test
    public void shouldCombineBrfFilesWithLineBreaks() throws IOException {
        byte[] contents = embossFileService.combineFiles(new String[] { sample1FilePath, sample2FilePath });

        // file sizes plus the 3 characters for the EmbossFileService.PAGE_BREAK_BYTES added in between
        int combinedSize = Files.readAllBytes(Paths.get(sample1FilePath)).length + Files.readAllBytes(Paths.get(sample2FilePath)).length + 3;

        assertTrue(contents.length == combinedSize);
    }

    @Test
    public void shouldHandleOneBrfFileInput() throws IOException {
        byte[] contents = embossFileService.combineFiles(new String[] { sample1FilePath });

        assertTrue(contents.length == Files.readAllBytes(Paths.get(sample1FilePath)).length);
    }

    @Test(expected = IOException.class)
    public void shouldThrowExceptionWhenBadMainFilePath() throws IOException {
        byte[] contents = embossFileService.combineFiles(new String[] { "/tmp/badfile.brf" });
    }

    @Test(expected = IOException.class)
    public void shouldThrowExceptionWhenBadSecondFileFilePath() throws IOException {
        embossFileService.combineFiles(new String[] { sample1FilePath, "/tmp/badfile.brf" });
    }
}
