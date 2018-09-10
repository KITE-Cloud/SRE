package de.thm.utililities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Ermöglicht einen einfachen Zugriff auf Dateien die in dem Resource Ordner gespeichert sind.
 */
public class FileUtil {

    public static InputStream getInputStream(String filePath){
        InputStream inputStream = null;
        File f = new File(filePath);

        if(f.exists()){
            try {
                inputStream = new FileInputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }else{
            inputStream = FileUtil.class.getClassLoader().getResourceAsStream(filePath);
        }

        return inputStream;
    }
}
