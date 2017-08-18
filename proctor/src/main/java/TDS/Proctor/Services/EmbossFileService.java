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

package TDS.Proctor.Services;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.util.ByteArrayBuffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import tds.itemrenderer.repository.ContentRepository;

@Component
public class EmbossFileService {


    private final ContentRepository contentRepository;

    // 13 = carriage return
    // 10 = new line
    // 12 = form feed (new page)
    private static final char[] PAGE_BREAK_CHARS = {(char)13, (char)10, (char)12};
    static byte[] PAGE_BREAK_BYTES = null;

    @Autowired
    public EmbossFileService(final ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
        String pageBreak = new String(PAGE_BREAK_CHARS);

        try {
            PAGE_BREAK_BYTES = pageBreak.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // if UTF-8 is not supported, use the default OS encoding
            PAGE_BREAK_BYTES = pageBreak.getBytes();
        }
    }

    /**
     * Combine one or more files into a new file with page breaks.
     *
     * @param files one or more file paths to braille files that will be combined with page breaks
     * @return the combined file contents as a byte array
     * @throws IOException
     */
    public byte[] combineFiles(String[] files) throws IOException {
        ByteArrayBuffer contents = new ByteArrayBuffer(0);

        byte[] bytes = IOUtils.toByteArray(contentRepository.findResource(files[0]));
        contents.append(bytes, 0, bytes.length);

        for (int i = 1; i < files.length; i++) {
            // page break
            contents.append(PAGE_BREAK_BYTES, 0, PAGE_BREAK_BYTES.length);

            bytes = IOUtils.toByteArray(contentRepository.findResource(files[i]));
            contents.append(bytes, 0, bytes.length);
        }

        return contents.toByteArray();
    }


    /**
     * Generates a combined filename by adding the appopriate suffix before the extension.  If a full path is given, only the filename part is used.
     *
     * @param templateFilePath filename to use as the template
     * @param suffix suffix added to the end of the filename
     * @return the new combined filename
     */
    public String getCombinedFileName(String templateFilePath, String suffix) {
        String extension = FilenameUtils.getExtension(templateFilePath);

        return FilenameUtils.getBaseName(templateFilePath) +
                (suffix != null ? suffix : "") +
                (extension != null && extension != "" ? "." + extension : "");
    }
}
