/**
 * 执行超时操作
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        long time = 60 * 60 * 1000L;
        Thread.sleep(time);
        System.out.println("执行睡眠一小时的操作");
    }
}
