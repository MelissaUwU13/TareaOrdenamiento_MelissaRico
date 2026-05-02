package org.example.tareaordenamiento;

import java.io.*;
import java.util.*;

public class Ordenamiento {

    //prueba
    private static final int MAX_CHUNK_SIZE = 100;

    //En esta parte modificaciones nuestro archivo de registros
    //para poder acomodarlo para la particion y ordenamiento
    static class Registro {
        String nombre;
        int id;
        String pais;

        public Registro(String nombre, int id, String pais) {
            this.nombre = nombre;
            this.id = id;
            this.pais = pais;
        }

        public static Registro Partes(String linea) {
            String[] partes = linea.split(",");
            return new Registro(partes[0], Integer.parseInt(partes[1]), partes[2]);
        }

        @Override
        public String toString() {
            return nombre + "," + id + "," + pais;
        }
    }

    //Nodo para la fase 2 de Merge
    static class MergeNode {
        Registro registro;
        int id;

        public MergeNode(Registro registro, int id) {
            this.registro = registro;
            this.id = id;
        }
    }

    //En este metodo creamos los chunks, es decir creamos el archivo y pasamos la informacion al archivo
    private static File crearChunk(List<Registro> buffer, int index) throws IOException {
        buffer.sort(Comparator.comparingInt(r -> r.id));

        File archivoTemp = new File("temp_" + index + ".dat");
        BufferedWriter texto = new BufferedWriter(new FileWriter(archivoTemp));

        //ingresamos la informacion al archivo al nuevo archivoTemporal
        for (Registro r : buffer) {
            texto.write(r.toString());
            texto.newLine();
        }

        //cerramos el archivo y regresamos el archivo temporal ya creado
        texto.close();
        return archivoTemp;
    }


    //Fase 1 - Particion
    public static List<File> Particion_Ordenamiento(File archivo) throws IOException {
        List<File> archivosTemp = new ArrayList<>();
        BufferedReader lectores = new BufferedReader(new FileReader(archivo));
        List<Registro> buffer = new ArrayList<>();

        String linea;
        int tamaño = 0;
        int contador = 0;

        //Estara en ciclo while mientras el archivo tenga contenido dentro
        while ((linea = lectores.readLine()) != null) {
            Registro r = Registro.Partes(linea);
            buffer.add(r);
            tamaño += linea.length();

            //si el tamaño es igual o menor al maximo de chunk entonces se agregan al archivo temporal actual
            if (tamaño >= MAX_CHUNK_SIZE) {
                archivosTemp.add(crearChunk(buffer, contador++));
                buffer.clear();
                tamaño = 0;
            }
        }

        if (!buffer.isEmpty()) {
            archivosTemp.add(crearChunk(buffer, contador++));
        }

        //cerramos el archivo y regresamos el archivo temporal ya creado
        lectores.close();
        return archivosTemp;
    }

    //Fase 2 - Mezcla (Merge)
    public static void mergeFiles(List<File> archivosTemp, File archivoSalida) throws IOException {

        List<BufferedReader> lectores = new ArrayList<>();
        PriorityQueue<MergeNode> minHeap =
                new PriorityQueue<>(Comparator.comparingInt(n -> n.registro.id));

        //Preparamos los archivos, obtenemos los primeros datos y los agregamos a minHeap para despues ejecutar el merge
        for (int i = 0; i < archivosTemp.size(); i++) {
            BufferedReader br = new BufferedReader(new FileReader(archivosTemp.get(i)));
            lectores.add(br);

            String linea = br.readLine();
            if (linea != null) {
                minHeap.add(new MergeNode(Registro.Partes(linea), i));
            }
        }


        BufferedWriter texto = new BufferedWriter(new FileWriter(archivoSalida));

        //Agregamos el texto al archivo de texto de salida hasta que el contenido de la mezcla este vacio
        while (!minHeap.isEmpty()) {
            MergeNode node = minHeap.poll();
            texto.write(node.registro.toString());
            texto.newLine();

            BufferedReader br = lectores.get(node.id);
            String linea = br.readLine();

            if (linea != null) {
                minHeap.add(new MergeNode(Registro.Partes(linea), node.id));
            }
        }

        texto.close();

        //Cierra los archivos
        for (BufferedReader br : lectores) {
            br.close();
        }
    }

    //Main para probar el codigo
    public static void main(String[] args) throws IOException {
        File archivo1 = new File("archivo.dat");
        File archivo2 = new File("archivoOrdenado.dat");

        List<File> archivosTemp = Particion_Ordenamiento(archivo1);
        mergeFiles(archivosTemp, archivo2);

    }
}