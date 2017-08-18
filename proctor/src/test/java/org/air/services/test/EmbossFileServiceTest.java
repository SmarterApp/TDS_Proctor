/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

package org.air.services.test;

import TDS.Proctor.Services.EmbossFileService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import tds.itemrenderer.repository.ContentRepository;

@RunWith(MockitoJUnitRunner.class)
public class EmbossFileServiceTest {
    private EmbossFileService embossFileService;
    private String sample1FilePath;
    private String sample2FilePath;

    @Mock
    private ContentRepository mockContentRepository;

    @Before
    public void setup() {
        embossFileService = new EmbossFileService(mockContentRepository);
        sample1FilePath = this.getClass().getResource("/emboss/sample1.brf").getFile();
        sample2FilePath = this.getClass().getResource("/emboss/sample2.brf").getFile();
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
        when(mockContentRepository.findResource(sample1FilePath)).thenReturn(new ByteArrayInputStream(new byte[949]));
        when(mockContentRepository.findResource(sample2FilePath)).thenReturn(new ByteArrayInputStream(new byte[682]));
        byte[] contents = embossFileService.combineFiles(new String[] { sample1FilePath, sample2FilePath });

        // file sizes plus the 3 characters for the EmbossFileService.PAGE_BREAK_BYTES added in between
        int combinedSize = Files.readAllBytes(Paths.get(sample1FilePath)).length + Files.readAllBytes(Paths.get(sample2FilePath)).length + 3;

        assertThat(contents.length).isEqualTo(combinedSize);
    }

    @Test
    public void shouldHandleOneBrfFileInput() throws IOException {
        when(mockContentRepository.findResource(sample1FilePath)).thenReturn(new ByteArrayInputStream(new byte[677]));
        byte[] contents = embossFileService.combineFiles(new String[] { sample1FilePath });

        assertThat(contents.length).isEqualTo(Files.readAllBytes(Paths.get(sample1FilePath)).length);
    }

    @Test(expected = IOException.class)
    public void shouldThrowExceptionWhenBadMainFilePath() throws IOException {
        when(mockContentRepository.findResource("/tmp/badfile.brf")).thenThrow(IOException.class);
        byte[] contents = embossFileService.combineFiles(new String[] { "/tmp/badfile.brf" });
    }

    @Test(expected = IOException.class)
    public void shouldThrowExceptionWhenBadSecondFileFilePath() throws IOException {
        when(mockContentRepository.findResource(isA(String.class))).thenThrow(IOException.class);
        embossFileService.combineFiles(new String[] { sample1FilePath, "/tmp/badfile.brf" });
    }
}
