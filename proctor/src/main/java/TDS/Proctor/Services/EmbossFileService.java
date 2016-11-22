package TDS.Proctor.Services;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.util.ByteArrayBuffer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class EmbossFileService {
    // 10 = new line
    // 12 = form feed (new page)
    private static final char[] PAGE_BREAK_CHARS = {(char)10, (char)12, (char)10, (char)12};
    private static byte[] PAGE_BREAK_BYTES = null;

    public EmbossFileService() {
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

        byte[] bytes = Files.readAllBytes(Paths.get(files[0]));
        contents.append(bytes, 0, bytes.length);

        for (int i = 1; i < files.length; i++) {
            // page break
            contents.append(PAGE_BREAK_BYTES, 0, PAGE_BREAK_BYTES.length);

            bytes = Files.readAllBytes(Paths.get(files[i]));
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
