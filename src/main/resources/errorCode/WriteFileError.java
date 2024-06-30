import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException {
        //获取当前的工作目录
        String user_dir = System.getProperty("user.dir");
        String filepath=user_dir + File.separator+ "/src/main/resources/木马.bat";
        String errorProgram="java -version 2>&1";
        Files.write(Paths.get(filepath), Arrays.asList(errorProgram));
        System.out.println("你被写入木马了，哈哈哈");
    }
}