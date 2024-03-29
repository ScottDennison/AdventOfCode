package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2022;

import uk.co.scottdennison.java.libs.text.input.LineReader;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;

public class Day07 implements IPuzzle {
    private static final boolean PRETTY_PRINT = false;

    private static final int PART_A_CUTOFF = 100000;
    private static final int PART_B_DISK_SIZE = 70000000;
    private static final int PART_B_UNUSED_SPACE_REQUIRED = 30000000;

    public static abstract class Entry {
        private final Directory parentDirectory;
        private final String name;

        public Entry(Directory parentDirectory, String name) {
            this.parentDirectory = parentDirectory;
            this.name = name;
        }

        public Directory getParentDirectory() {
            return this.parentDirectory;
        }

        public String getName() {
            return this.name;
        }

        public abstract int getSize();
        public abstract void prettyPrint(PrintWriter printWriter, String indent);
    }

    public static final class File extends Entry {
        private final int size;

        public File(Directory parentDirectory, String name, int size) {
            super(parentDirectory, name);
            this.size = size;
        }

        public int getSize() {
            return this.size;
        }

        @Override
        public void prettyPrint(PrintWriter printWriter, String indent) {
            printWriter.format("%s- %s (file, size=%d)%n", indent, this.getName(), this.size);
        }
    }

    public static final class Directory extends Entry {
        private final LinkedHashMap<Entry,Integer> childEntrySizes = new LinkedHashMap<>();
        private final Map<String,Directory> childDirectories = new HashMap<>();
        private final Map<String,File> childFiles = new HashMap<>();
        private int allChildrenSize = 0;

        private Directory(Directory parentDirectory, String name) {
            super(parentDirectory, name);
        }

        public int getSize() {
            return this.allChildrenSize;
        }

        public Directory getChildDirectory(String name) {
            Directory directory = this.childDirectories.get(name);
            if (directory == null) {
                throw new IllegalStateException("No such child directory");
            }
            return directory;
        }

        public Iterator<Directory> iterateChildDirectories() {
            return Collections.unmodifiableCollection(this.childDirectories.values()).iterator();
        }

        public Iterator<File> iterateChildFiles() {
            return Collections.unmodifiableCollection(this.childFiles.values()).iterator();
        }

        public Directory createChildDirectory(String name) {
            Directory directory = new Directory(this, name);
            if (this.childDirectories.put(name, directory) != null) {
                throw new IllegalStateException("Already a directory with this name");
            }
            this.handleNewEntrySize(directory);
            return directory;
        }

        public File createChildFile(String name, int size) {
            File file = new File(this, name, size);
            if (this.childFiles.put(name, file) != null) {
                throw new IllegalStateException("Already a file with this name");
            }
            this.handleNewEntrySize(file);
            return file;
        }

        private void handleNewEntrySize(Entry entry) {
            int entrySize = entry.getSize();
            if (this.childEntrySizes.put(entry,entrySize) != null) {
                throw new IllegalStateException("Size entry already exists");
            }
            this.allChildrenSize += entrySize;
            this.notifyParentSizeChanged();
        }

        public void notifyChildSizeChanged(Entry childEntry) {
            Integer oldEntrySize = this.childEntrySizes.get(childEntry);
            if (oldEntrySize == null) {
                throw new IllegalStateException("Provided child is not known to this directory");
            }
            int newEntrySize = childEntry.getSize();
            this.childEntrySizes.put(childEntry, newEntrySize);
            this.allChildrenSize += (newEntrySize - oldEntrySize);
            this.notifyParentSizeChanged();
        }

        private void notifyParentSizeChanged() {
            Directory parentDirectory = this.getParentDirectory();
            if (parentDirectory != null) {
                parentDirectory.notifyChildSizeChanged(this);
            }
        }

        @Override
        public void prettyPrint(PrintWriter printWriter, String indent) {
            printWriter.format("%s- %s (dir)%n", indent, this.getName());
            String childIndent = indent + "  ";
            for (Entry childEntry : this.childEntrySizes.keySet()) {
                childEntry.prettyPrint(printWriter, childIndent);
            }
        }

        private static Directory createRootDirectory(String name) {
            return new Directory(null, name);
        }
    }

    private int recursePartA(Directory directory) {
        int totalSize = 0;
        int directorySize = directory.getSize();
        if (directorySize <= PART_A_CUTOFF) {
            totalSize += directorySize;
        }
        Iterator<Directory> childDirectoryIterator = directory.iterateChildDirectories();
        while (childDirectoryIterator.hasNext()) {
            totalSize += recursePartA(childDirectoryIterator.next());
        }
        return totalSize;
    }

    private OptionalInt recursePartB(Directory directory, int spaceToFree) {
        int bestSize = directory.getSize();
        if (bestSize < spaceToFree) {
            bestSize = Integer.MAX_VALUE;
        }
        Iterator<Directory> childDirectoryIterator = directory.iterateChildDirectories();
        while (childDirectoryIterator.hasNext()) {
            OptionalInt childResult = recursePartB(childDirectoryIterator.next(),spaceToFree);
            if (childResult.isPresent()) {
                bestSize = Math.min(bestSize, childResult.getAsInt());
            }
        }
        if (bestSize == Integer.MAX_VALUE) {
            return OptionalInt.empty();
        } else {
            return OptionalInt.of(bestSize);
        }
    }

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Directory rootDirectory = Directory.createRootDirectory("/");
        Directory currentDirectory = rootDirectory;
        boolean inLs = false;
        for (String line : LineReader.strings(inputCharacters)) {
            String parts[] = line.split(" ");
            if (parts[0].equals("$")) {
                if (parts.length < 2) {
                    throw new IllegalStateException("Missing command");
                }
                switch (parts[1]) {
                    case "ls":
                        if (parts.length != 2) {
                            throw new IllegalStateException("Expeced 2 parts to an ls command");
                        } else {
                            inLs = true;
                        }
                        break;
                    case "cd":
                        if (parts.length != 3) {
                            throw new IllegalStateException("Expeced 3 parts to an cd command");
                        } else {
                            String directoryName = parts[2];
                            switch (directoryName) {
                                case "/":
                                    currentDirectory = rootDirectory;
                                    break;
                                case "..":
                                    currentDirectory = currentDirectory.getParentDirectory();
                                    break;
                                default:
                                    currentDirectory = currentDirectory.getChildDirectory(directoryName);
                            }
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unexpected command");
                }
            } else if (inLs) {
                if (parts.length != 2) {
                    throw new IllegalStateException("Expeced 2 parts to an ls output line");
                } else if (parts[0].equals("dir")) {
                    currentDirectory.createChildDirectory(parts[1]);
                } else {
                    currentDirectory.createChildFile(parts[1],Integer.parseInt(parts[0]));
                }
            } else {
                throw new IllegalStateException("Not expecting ls output, but did not see a command");
            }
        }
        if (PRETTY_PRINT) {
            rootDirectory.prettyPrint(printWriter, "");
        }
        return new BasicPuzzleResults<>(
            recursePartA(rootDirectory),
            recursePartB(rootDirectory,(PART_B_UNUSED_SPACE_REQUIRED-(PART_B_DISK_SIZE-rootDirectory.getSize()))).orElseThrow(() -> new IllegalStateException("No directory frees enough space"))
        );
    }
}
