package org.air.services.test;

import TDS.Proctor.Services.EmbossFileService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
    public void shouldCombineFilePath() {
        String combinedFilePath = embossFileService.getCombinedFilePath("/usr/test/testfile.brf", "_suffix");

        assertTrue(combinedFilePath.equals("/usr/test/testfile_suffix.brf"));
    }

    @Test
    public void shouldCombineFilePathWithMultiplePeriods() {
        String combinedFilePath = embossFileService.getCombinedFilePath("/usr/test/testfile.something.brf", "_suffix");

        assertTrue(combinedFilePath.equals("/usr/test/testfile.something_suffix.brf"));
    }

    @Test
    public void shouldCombineFilePathWithNoExtension() {
        String combinedFilePath = embossFileService.getCombinedFilePath("/usr/test/testfile", "_suffix");

        assertTrue(combinedFilePath.equals("/usr/test/testfile_suffix"));
    }

    @Test
    public void shouldCombineFilePathWithEmptySuffixProvided() {
        String combinedFilePath = embossFileService.getCombinedFilePath("/usr/test/testfile.brf", "");

        assertTrue(combinedFilePath.equals("/usr/test/testfile.brf"));
    }

    @Test
    public void shouldCombineFilePathWithNullSuffixProvided() {
        String combinedFilePath = embossFileService.getCombinedFilePath("/usr/test/testfile.brf", null);

        assertTrue(combinedFilePath.equals("/usr/test/testfile.brf"));
    }

    @Test
    public void shouldCombineBrfFilesWithLineBreaks() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        embossFileService.writeEmbossFile(outputStream, new String[] { sample1FilePath, sample2FilePath });

        // file sizes plus the 4 characters for the line breaks added in between
        int combinedSize = Files.readAllBytes(Paths.get(sample1FilePath)).length + Files.readAllBytes(Paths.get(sample2FilePath)).length + 4;

        assertTrue(outputStream.size() == combinedSize);

    }

    @Test
    public void shouldHandleOneBrfFileInput() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        embossFileService.writeEmbossFile(outputStream, new String[] { sample1FilePath });

        assertTrue(outputStream.size() == Files.readAllBytes(Paths.get(sample1FilePath)).length);

    }

    @Test(expected = IOException.class)
    public void shouldThrowExceptionWhenBadMainFilePath() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        embossFileService.writeEmbossFile(outputStream, new String[] { "/tmp/badfile.brf" });
    }

    @Test(expected = IOException.class)
    public void shouldThrowExceptionWhenBadSecondFileFilePath() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        embossFileService.writeEmbossFile(outputStream, new String[] { sample1FilePath, "/tmp/badfile.brf" });
    }
}
