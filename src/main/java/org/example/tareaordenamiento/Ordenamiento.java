package org.example.tareaordenamiento;

import eu.hansolo.toolbox.Constants;

import java.io.*;
import java.util.*;

public class Ordenamiento {

    private static final int MAX_CHUNK_SIZE = 512 * 1024 * 1024;

    static class MergeNode {
        int value;
        int fileIndex;

        public MergeNode(int value, int fileIndex) {
            this.value = value;
            this.fileIndex = fileIndex;
        }
    }

    public static List<File> partitionAndSort(File inputFile) throws IOException {
        List<File> tempFiles = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));

        List<Integer> buffer = new ArrayList<>();
        String line;
        int currentSize = 0;
        int fileCount = 0;

        while ((line = reader.readLine()) != null) {
            int num = Integer.parseInt(line);
            buffer.add(num);
            currentSize += 4;

            if (currentSize >= MAX_CHUNK_SIZE) {
                tempFiles.add(writeChunk(buffer, fileCount++));
                buffer.clear();
                currentSize = 0;
            }
        }

        if (!buffer.isEmpty()) {
            tempFiles.add(writeChunk(buffer, fileCount++));
        }

        reader.close();
        return tempFiles;
    }

    private static File writeChunk(List<Integer> buffer, int index) throws IOException {
        Collections.sort(buffer);

        File tempFile = new File("temp_" + index + ".dat");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

        for (int num : buffer) {
            writer.write(num + "\n");
        }

        writer.close();
        return tempFile;
    }

    // 🔹 FASE 2: Merge
    public static void mergeFiles(List<File> tempFiles, File outputFile) throws IOException {

        List<BufferedReader> readers = new ArrayList<>();
        PriorityQueue<MergeNode> minHeap =
                new PriorityQueue<>(Comparator.comparingInt(n -> n.value));

        // abrir archivos
        for (int i = 0; i < tempFiles.size(); i++) {
            BufferedReader br = new BufferedReader(new FileReader(tempFiles.get(i)));
            readers.add(br);

            String line = br.readLine();
            if (line != null) {
                minHeap.add(new MergeNode(Integer.parseInt(line), i));
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        while (!minHeap.isEmpty()) {
            MergeNode node = minHeap.poll();
            writer.write(node.value + "\n");

            BufferedReader br = readers.get(node.fileIndex);
            String line = br.readLine();

            if (line != null) {
                minHeap.add(new MergeNode(Integer.parseInt(line), node.fileIndex));
            }
        }

        writer.close();

        for (BufferedReader br : readers) {
            br.close();
        }
    }

    public static void main(String[] args) throws IOException {
        File input = new File("entrada.dat");
        File output = new File("salida.dat");

        List<File> ArchivosTemp = partitionAndSort(input);
        mergeFiles(ArchivosTemp, output);
    }
}