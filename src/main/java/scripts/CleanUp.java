package scripts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class CleanUp {
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanUp.class);

    public static void RemoveFiles(String targetPath) {
        try {
            delete(targetPath);
        }catch(Exception e) {
            e.printStackTrace();
        }

    }
    public static void delete(String path) {
        File filePath = new File(path);
        String[] list = filePath.list();
        for(String file : list) {
            File f = new File(path + File.separator + file);
            if(f.isFile()) {
                f.delete();
                LOGGER.debug("CleanUp.RemoveFiles.delete completed");
            }
        }
    }
}
