public class ClasspathDump {
    public static void main(String[] args) {
        System.out.print(System.getProperty("sun.boot.class.path"));
        System.out.print("|");
        System.out.print(System.getProperty("java.ext.dirs"));
        System.out.print("|");
        System.out.print(System.getProperty("java.endorsed.dirs"));
    }
}