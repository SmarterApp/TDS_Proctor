package TDS.Proctor.Services;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@Scope("prototype")
public class EmbossFileService {
    /**
     * Writes multiple files to the output stream added a new page break between each file
     *
     * @param outputStream output stream to write to
     * @param files one or more file paths to braille files
     * @return the number of bytes written
     * @throws IOException
     */
    public int writeEmbossFile(OutputStream outputStream, String[] files) throws IOException {
        int bytesWritten = 0;
        byte[] bytes = Files.readAllBytes(Paths.get(files[0]));
        outputStream.write(bytes);
        bytesWritten += bytes.length;

        for (int i = 1; i < files.length; i++) {
            // 10 = new line
            // 12 = form feed (new page)
            char[] lineBreak = {(char) 10, (char) 12, (char) 10, (char) 12};
            outputStream.write(new String(lineBreak).getBytes("UTF-8"));

            bytes = Files.readAllBytes(Paths.get(files[i]));
            outputStream.write(bytes);

            bytesWritten += bytes.length + 4;
        }

        return bytesWritten;
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
