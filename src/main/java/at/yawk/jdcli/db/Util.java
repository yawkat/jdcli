package at.yawk.jdcli.db;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.bind.DatatypeConverter;
import lombok.SneakyThrows;

/**
 * @author yawkat
 */
public class Util {
    private Util() {}

    public static void extractZip(Path zip, Path directory) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) { continue; }

                Path target = directory.resolve(entry.getName());
                if (!Files.exists(target.getParent())) {
                    Files.createDirectories(target.getParent());
                }

                Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    public static String hashFile(Path f) {
        Hasher hasher = Hashing.sha512().newHasher();
        Util.hashFile(hasher, f);
        return DatatypeConverter.printHexBinary(hasher.hash().asBytes()).toLowerCase();
    }

    @SneakyThrows
    private static void hashFile(Hasher hasher, Path f) {
        if (Files.isDirectory(f)) {
            Files.list(f)
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .forEach(child -> hashFile(hasher, child));
        } else {
            try (InputStream in = Files.newInputStream(f)) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) != -1) {
                    hasher.putBytes(buf, 0, len);
                }
            }
        }
    }
}
