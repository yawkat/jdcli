package at.yawk.jdcli.db;

import at.yawk.jdcli.doclet.DocletSettings;
import at.yawk.jdcli.doclet.Invoker;
import at.yawk.logging.ansi.Ansi;
import com.google.common.io.ByteStreams;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;

/**
 * @author yawkat
 */
@RequiredArgsConstructor
public class DatabaseManager {
    private static final String LOG_ERRORS_SYSTEM_PROPERTY = "jdcli.javadoc.logErrors";
    private static final boolean LOG_ERRORS = Boolean.getBoolean(LOG_ERRORS_SYSTEM_PROPERTY);

    private final Path storageDirectory;
    private final List<ArtifactProvider<?>> artifactProviders;
    private final DocletSettings docletSettings;

    public void updateAndLink() throws IOException, InterruptedException {
        List<Artifact> allArtifacts = new ArrayList<>();
        for (ArtifactProvider<?> provider : artifactProviders) {
            allArtifacts.addAll(updateArtifacts(provider));
        }
        link(allArtifacts);
    }

    private List<Artifact> updateArtifacts(ArtifactProvider<?> provider)
            throws IOException, InterruptedException {
        List<Artifact> artifacts = provider.listArtifacts();
        for (int i = 0; i < artifacts.size(); i++) {
            Artifact artifact = artifacts.get(i);
            String currentHash = artifact.computeHash();

            Path artifactPath = getArtifactPath(artifact);
            Path hashFile = artifactPath.resolve(".hash");
            Path failureFile = artifactPath.resolve(".failure");

            String progressPrefix =
                    Ansi.cyan().toString() + Ansi.bold() + "[" + (i + 1) + "/" + artifacts.size() + "]" + Ansi.reset();

            if (Files.exists(hashFile)) {
                String oldHash = new String(Files.readAllBytes(hashFile), StandardCharsets.UTF_8);
                if (oldHash.equals(currentHash)) {
                    System.out.println(progressPrefix + " Not updating " + artifact);
                    continue;
                }
            }

            if (Files.exists(failureFile)) {
                String oldHash = new String(Files.readAllBytes(failureFile), StandardCharsets.UTF_8);
                if (oldHash.equals(currentHash)) {
                    System.out.println(progressPrefix + " Not updating " + artifact + " " + Ansi.red() +
                                       "(cached failure)" + Ansi.reset());
                    continue;
                }
                continue;
            }

            System.out.println(progressPrefix + " Updating " + artifact + "...");

            createDirectoryIfAbsent(artifactPath);
            if (update(artifact, artifactPath)) {
                Files.write(hashFile, currentHash.getBytes(StandardCharsets.UTF_8));
            } else {
                Files.write(failureFile, currentHash.getBytes(StandardCharsets.UTF_8));
            }

        }
        return artifacts;
    }

    private boolean update(Artifact artifact, Path artifactPath) throws IOException, InterruptedException {
        String classPath;
        try {
            classPath = artifact.resolveClassPath();
        } catch (Exception e) {
            System.out.println(Ansi.red() + "Failed to resolve dependencies: " + Ansi.reset() + e);
            return false;
        }

        Path tmp = createEmptyTemp();

        Files.createDirectories(tmp);

        Path extracted = artifact.extract(tmp);

        ProcessBuilder processBuilder = Invoker.createDocletProcess(
                extracted,
                artifactPath,
                classPath,
                docletSettings
        );
        System.out.println("Running '" + String.join(" ", processBuilder.command()) + "'...");
        if (LOG_ERRORS) {
            processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);
        }

        Process process = processBuilder.start();
        process.waitFor();
        if (process.exitValue() != 0) {
            if (LOG_ERRORS) {
                ByteStreams.copy(process.getErrorStream(), System.out);
            }
            System.out.println(
                    Ansi.red() + "Failed to generate docs: " + Ansi.reset() + "Exit code " + process.exitValue() +
                    " - run with -D" + LOG_ERRORS_SYSTEM_PROPERTY + "=true to show errors"
            );
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private void link(List<Artifact> artifacts) throws IOException {
        System.out.println(Ansi.cyan() + "Sorting artifacts..." + Ansi.reset());

        Collections.sort(artifacts, (o1, o2) -> {
            ArtifactProvider p1 = o1.getProvider();
            ArtifactProvider p2 = o2.getProvider();
            ArtifactComparator versionComparator = p1.getVersionComparator();
            if (!p1.equals(p2) && !versionComparator.canCompare(o1, o2)) {
                return 0;
            }
            return versionComparator.compare(o1, o2);
        });

        // we can't do this in sort() since that would place artifacts in wrong provider order
        Collections.reverse(artifacts);

        System.out.println(Ansi.cyan() + "Linking docs..." + Ansi.reset());
        AtomicInteger linked = new AtomicInteger();

        Path tmp = createEmptyTemp();
        createDirectoryIfAbsent(tmp);

        Set<String> indexed = new HashSet<>();
        try (BufferedWriter indexWriter = Files.newBufferedWriter(tmp.resolve(".index"))) {
            for (Artifact artifact : artifacts) {
                Path artifactPath = getArtifactPath(artifact);
                Files.walkFileTree(artifactPath, new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (!Files.isHidden(file)) {
                            Path relative = artifactPath.relativize(file);
                            Path target = tmp.resolve(relative);
                            if (!Files.exists(target)) {
                                // create symlink
                                createDirectoryIfAbsent(target.getParent());
                                Files.createSymbolicLink(target, file);

                                // add class and packages to index
                                String name = relative.toString().replace('/', '.');
                                // remove .txt extension
                                name = name.substring(0, name.length() - ".txt".length());
                                while (indexed.add(name)) {
                                    indexWriter.write(name);
                                    indexWriter.write('\n');
                                    int lastDot = name.lastIndexOf('.');
                                    if (lastDot == -1) { break; }
                                    name = name.substring(0, lastDot);
                                }

                                // print progress
                                if (linked.getAndIncrement() % 17 == 0) { // prime so it looks natural
                                    System.out.print("\rLinked " + linked + " files");
                                }
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            System.out.println();
        }

        Path target = storageDirectory.resolve("classes");
        deleteDirectoryIfPresent(target);
        Files.move(tmp, target);

        System.out.println(Ansi.lightGreen() + "Done!");
    }

    private static void createDirectoryIfAbsent(Path target) throws IOException {
        if (!Files.exists(target)) {
            Files.createDirectories(target);
        }
    }

    private Path createEmptyTemp() throws IOException {
        Path tmp = storageDirectory.resolve(".tmp");
        deleteDirectoryIfPresent(tmp);
        return tmp;
    }

    private void deleteDirectoryIfPresent(Path directory) throws IOException {
        if (Files.exists(directory)) {
            // recursive delete
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private Path getArtifactPath(Artifact artifact) {
        return storageDirectory
                .resolve("repo")
                .resolve(artifact.getId().replace(':', '/'))
                .resolve(artifact.getVersion());
    }
}
