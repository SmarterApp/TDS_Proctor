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
    public void writeEmbossFile(OutputStream outputStream, String[] files) throws IOException {
        outputStream.write(Files.readAllBytes(Paths.get(files[0])));

        for (int i = 1; i < files.length; i++) {
            // 10 = new line
            // 12 = form feed (new page)
            char[] lineBreak = {(char)10, (char)12, (char)10, (char)12};
            outputStream.write(new String(lineBreak).getBytes("UTF-8"));

            outputStream.write(Files.readAllBytes(Paths.get(files[i])));
        }
    }

    public String getCombinedFilePath(String templateFilePath, String suffix) {
        String extension = FilenameUtils.getExtension(templateFilePath);

        return  FilenameUtils.getFullPath(templateFilePath) +
                FilenameUtils.getBaseName(templateFilePath) +
                (suffix != null ? suffix : "") +
                (extension != null && extension != "" ? "." + extension : "");

    }
}
