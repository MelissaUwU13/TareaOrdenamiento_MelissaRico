package org.example.tareaordenamiento;

import java.io.*;
import java.util.*;

public class Ordenamiento {

    private static final int MAX_CHUNK_SIZE = 100; // pequeño para pruebas

    static class Registro {
        String nombre;
        int id;
        String pais;

        public Registro(String nombre, int id, String pais) {
            this.nombre = nombre;
            this.id = id;
            this.pais = pais;
        }

        public static Registro fromLine(String line) {
            String[] parts = line.split(",");
            return new Registro(parts[0], Integer.parseInt(parts[1]), parts[2]);
        }

        @Override
        public String toString() {
            return nombre + "," + id + "," + pais;
        }
    }

    static class MergeNode {
        Registro registro;
        int fileIndex;

        public MergeNode(Registro registro, int fileIndex) {
            this.registro = registro;
            this.fileIndex = fileIndex;
        }
    }

    public static List<File> partitionAndSort(File inputFile) throws IOException {
        List<File> tempFiles = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));

        List<Registro> buffer = new ArrayList<>();
        String line;
        int currentSize = 0;
        int fileCount = 0;

        while ((line = reader.readLine()) != null) {
            Registro r = Registro.fromLine(line);
            buffer.add(r);
            currentSize += line.length();

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

    private static File writeChunk(List<Registro> buffer, int index) throws IOException {
        buffer.sort(Comparator.comparingInt(r -> r.id));

        File tempFile = new File("temp_" + index + ".dat");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

        for (Registro r : buffer) {
            writer.write(r.toString());
            writer.newLine();
        }

        writer.close();
        return tempFile;
    }

    public static void mergeFiles(List<File> tempFiles, File outputFile) throws IOException {

        List<BufferedReader> readers = new ArrayList<>();
        PriorityQueue<MergeNode> minHeap =
                new PriorityQueue<>(Comparator.comparingInt(n -> n.registro.id));

        for (int i = 0; i < tempFiles.size(); i++) {
            BufferedReader br = new BufferedReader(new FileReader(tempFiles.get(i)));
            readers.add(br);

            String line = br.readLine();
            if (line != null) {
                minHeap.add(new MergeNode(Registro.fromLine(line), i));
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        while (!minHeap.isEmpty()) {
            MergeNode node = minHeap.poll();
            writer.write(node.registro.toString());
            writer.newLine();

            BufferedReader br = readers.get(node.fileIndex);
            String line = br.readLine();

            if (line != null) {
                minHeap.add(new MergeNode(Registro.fromLine(line), node.fileIndex));
            }
        }

        writer.close();

        for (BufferedReader br : readers) {
            br.close();
        }
    }

    public static void main(String[] args) throws IOException {
        File archivo1 = new File("archivo.dat");
        File archivo2 = new File("archivoOrdenado.dat");

        List<File> tempFiles = partitionAndSort(archivo1);
        mergeFiles(tempFiles, archivo2);

    }
}