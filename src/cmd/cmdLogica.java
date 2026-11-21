package cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class cmdLogica {
    private boolean modoEscritura = false;
    private boolean modoAgregar = false;
    private File archivoEscritura = null;
    private List<String> lineasEscritura = null;
    
    private File actual;
    
    private boolean nombreValido(String nombre) {
        if (nombre.isEmpty()){
            return false;
        }
        if (nombre.contains("..") || nombre.equals(".")){
            return false;
        }
        if (nombre.contains("/") || nombre.contains("\\")) {
            return false;
        }
        if (new File(nombre).isAbsolute()){
            return false;
        }
        return true;
    }
    
    private File devolverRuta(String nombre) {
        return new File(actual, nombre);
    }

    private String Wr(String nombre){
        if(!nombreValido(nombre)){
            return "Error: el nombre ingresado no es valido.";
        }
        File archivo=devolverRuta(nombre);
        if(archivo.isDirectory()){
            return "Error: '"+nombre+"' es una carpeta, no se puede escribir en ella.";
        }
        modoEscritura=true;
        modoAgregar=false;
        archivoEscritura=archivo;
        lineasEscritura=new ArrayList<>();
        return "Escribiendo en '"+nombre+"'. Escriba EXIT para terminar y guardar.";
    }

    private String Ap(String nombre){
        if(!nombreValido(nombre)){
            return "Error: el nombre ingresado no es valido.";
        }
        File archivo=devolverRuta(nombre);
        if(archivo.isDirectory()){
            return "Error: '"+nombre+"' es una carpeta, no se puede escribir en ella.";
        }
        modoEscritura=true;
        modoAgregar=true;
        archivoEscritura=archivo;
        lineasEscritura=new ArrayList<>();
        return "Agregando texto al final de '"+nombre+"'. Escriba EXIT para terminar y guardar.";
    }

    private String escribirLinea(String linea){
        if(linea.equals("EXIT")){
            try(FileWriter fw=new FileWriter(archivoEscritura, modoAgregar)){
                for(String l : lineasEscritura){
                    fw.write(l);
                    fw.write(System.lineSeparator());
                }
            }catch(IOException e){
                modoEscritura=false;
                archivoEscritura=null;
                lineasEscritura=null;
                return "Error al guardar el archivo: "+e.getMessage();
            }
            String nombre=archivoEscritura.getName();
            modoEscritura=false;
            archivoEscritura=null;
            lineasEscritura=null;
            return "Archivo '"+nombre+"' guardado correctamente.";
        }
        lineasEscritura.add(linea);
        return "";
    }

    private String Rd(String nombre){
        if(!nombreValido(nombre)){
            return "Error: el nombre ingresado no es valido.";
        }
        File archivo=devolverRuta(nombre);
        if(!archivo.exists() || archivo.isDirectory()){
            return "Error: el archivo '"+nombre+"' no existe.";
        }
        StringBuilder sb=new StringBuilder();
        try(BufferedReader br=new BufferedReader(new FileReader(archivo))){
            String linea;
            boolean primera=true;
            while((linea=br.readLine())!=null){
                if(!primera){
                    sb.append("\n");
                }
                sb.append(linea);
                primera=false;
            }
        }catch(IOException e){
            return "Error al leer el archivo: "+e.getMessage();
        }
        if(sb.length()==0){
            return "(El archivo esta vacio)";
        }
        return sb.toString();
    }

    private String Copy(String resto){
        int espacio=resto.indexOf(' ');
        if(espacio==-1){
            return "Uso: Copy <origen> <destino>";
        }
        String nombreOrigen=resto.substring(0,espacio);
        String nombreDestino=resto.substring(espacio+1).trim();
        if(!nombreValido(nombreOrigen) || !nombreValido(nombreDestino)){
            return "Error: el nombre ingresado no es valido.";
        }
        File origen=devolverRuta(nombreOrigen);
        File destino=devolverRuta(nombreDestino);
        if(!origen.exists() || origen.isDirectory()){
            return "Error: el archivo origen '"+nombreOrigen+"' no existe.";
        }
        if(destino.exists()){
            return "Error: ya existe un archivo o carpeta llamado '"+nombreDestino+"'.";
        }
        try(FileReader fr=new FileReader(origen); FileWriter fw=new FileWriter(destino)){
            char[] buffer=new char[1024];
            int len;
            while((len=fr.read(buffer))!=-1){
                fw.write(buffer,0,len);
            }
        }catch(IOException e){
            return "Error al copiar el archivo: "+e.getMessage();
        }
        return "'"+nombreOrigen+"' copiado a '"+nombreDestino+"'.";
    }

}
