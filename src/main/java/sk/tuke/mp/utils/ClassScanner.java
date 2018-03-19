package sk.tuke.mp.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassScanner {

    public static List<Class> scan(Package pack) throws IOException {
        return getClasses(pack.getName());
    }


    public static List<Class> scan(String pack) throws IOException {
        return getClasses(pack);
    }

    // Copy-pasted from:
    //      https://web.archive.org/web/20090227113602/http://snippets.dzone.com:80/posts/show/4831
    private static List<Class> getClasses(String packageName)
            throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }


    // Copy-pasted from:
    //      https://web.archive.org/web/20090227113602/http://snippets.dzone.com:80/posts/show/4831
    // + modified
    private static List<Class> findClasses(File directory, String packageName) {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                try {
                    classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
                } catch(ClassNotFoundException e) {
                    Logger.getGlobal().log(Level.INFO, "File " + file.getName() + " was not found, skipping.");
                }
            }
        }
        return classes;
    }
}
