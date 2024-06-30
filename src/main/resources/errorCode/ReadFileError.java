

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * 读代码，造成信息泄漏的问题：
 */
public class Main {

    public static void main(String[] args) throws IOException {

        String user_dir = System.getProperty("user.dir");
        String filepath=user_dir + File.separator + "/src/main/resources/application.yml";
        List<String> stringList = Files.readAllLines(Paths.get(filepath));
        System.out.println(String.join("\n",stringList));
    }
}
