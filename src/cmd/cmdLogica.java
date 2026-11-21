package cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class cmdLogica {
    private boolean modoEscritura = false;
    private boolean modoAgregar = false;
    private File archivoEscritura = null;
    private List<String> lineasEscritura = null;
    
    private File actual;
    private File raiz;
    
    public static final String SALIR = " __EXIT__";
    public static final String LIMPIAR = " __CLS__";
    
    public cmdLogica(){
        raiz=new File("ConsolaSimulada");
        if(!raiz.exists()){
            raiz.mkdir();
        }
        actual=raiz;
    }
    
    public String prompt(){
        if(modoEscritura){
            return "";
        }
        return rutaVirtual(actual);
    }
    
    public boolean estaEscribiendo(){
        return modoEscritura;
    }
    
    private String rutaVirtual(File f){
    String base = raiz.getAbsoluteFile().getParent();
    String relativa = f.getAbsolutePath().substring(base.length());
    relativa = relativa.replace("/", "\\");
    if(!relativa.startsWith("\\")){
        relativa = "\\" + relativa;
    }
    return "C:" + relativa;
}
    
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
    
    public String ejecutar(String txtIngresado){
        if(modoEscritura){
            return escribirLinea(txtIngresado);
        }
        String texto=txtIngresado.trim();
        if(texto.isEmpty()){
            return "";
        }
        String comando;
        String resto;
        int espacio=texto.indexOf(' ');
        if(espacio==-1){
            comando=texto;
            resto="";
        }else{
            comando=texto.substring(0,espacio);
            resto=texto.substring(espacio+1).trim();
        }

        switch (comando.toLowerCase()){
            case "mkdir":
                return Mkdir(resto);
            case "mfile":
                return Mfile(resto);
            case "..":
                return subirPadre();
            case "cd":
                return Cd(resto);
            case "rm":
                return Rm(resto);
            case "dir":
                return Dir();
            case "date":
                return Date();
            case "time":
                return Time();
            case "wr":
                return Wr(resto);
            case "rd":
                return Rd(resto);
            case "ap":
                return Ap(resto);
            case "ren":
                return Ren(resto);
            case "copy":
                return Copy(resto);
            case "find":
                return Find(resto);
            case "info":
                return Info(resto);
            case "tree":
                return Tree();
            case "cls":
                return LIMPIAR;
            case "help":
                return Help();
            case "exit":
                return SALIR;
            default:
                return "'"+comando+"' no se reconoce como un comando interno o externo.";
        }}
    
    private String Mkdir(String nombre){
        if(!nombreValido(nombre)){
            return "Error: el nombre ingresado no es valido.";
        }
        File carpeta= devolverRuta(nombre);
        if(carpeta.exists()){
            return "Error: ya existe una carpeta con este nombre";
        }
        boolean fueCreada=carpeta.mkdir();
        return fueCreada?"Carpeta '"+nombre+"' creada correctamente."
                :"Error: no se pudo crear la carpeta '"+nombre+"'.";
    }
    
    private String Mfile(String nombre){
        if(!nombreValido(nombre)){
            return "Error: el nombre ingresado no es valido.";
            
        }
        File archivo=devolverRuta(nombre);
        if(archivo.exists()){
            return "Error: ya existe un archivo con este nombre";
        }
        try{
            boolean fueCreado=archivo.createNewFile();
            return fueCreado?"Archivo '"+nombre+"' creado correctamente."
                    :"Error: no se pudo crear el archivo '"+nombre+"'.";
        }catch(IOException e ){
            return "Error al crear el archivo: "+ e.getMessage();
        }
    }
    
    private String subirPadre(){
        if(actual.equals(raiz)){
            return "";
        }
        actual = actual.getParentFile();
        return "";

    }
    
    private String Cd(String nombre){
       if(nombre.equals("..")){
            return subirPadre();
        }
       if(!nombreValido(nombre)){
            return "Error: el nombre ingresado no es valido o está vacío.";
            
        }
       File destino= devolverRuta(nombre);
       if (!destino.exists() || !destino.isDirectory()) {
            return "Error: la carpeta '" + nombre + "' no existe.";
        }
       actual=destino;
       return "";
    }
    
    private String Rm(String nombre){
        if(!nombreValido(nombre)){
            return "Error: el nombre ingresado no es valido.";
        }
        File objetivo=devolverRuta(nombre);
        if(!objetivo.exists()){
            return "Error: no existe el archivo o carpeta '"+nombre+"'";
        }
        boolean fueEliminado=eliminarRecursivo(objetivo);
        return fueEliminado?"'"+nombre+"' eliminado correctamente."
                :"Error: no se pudo eliminar '"+nombre+"'";
    }
    
    private boolean eliminarRecursivo(File f){
        if(f.isDirectory()){
            File[] hijos=f.listFiles();
            if(hijos!=null){
                for(File h : hijos){
                    if(!eliminarRecursivo(h)){
                        return false;
                    }
                }
            }
        }
        return f.delete();
    }
    
    private String Dir(){
        File[] items=actual.listFiles();
        if(items==null || items.length==0){
            return "La carpeta esta vacia.";
        }
        Arrays.sort(items, (a, b) -> {
            if (a.isDirectory() != b.isDirectory()) {
                return a.isDirectory() ? -1 : 1;
            }
            return a.getName().compareToIgnoreCase(b.getName());
        });
        StringBuilder sb=new StringBuilder();
        sb.append("Contenido de ").append(rutaVirtual(actual)).append(":\n\n");
        int carpetas=0;
        int archivos=0;
        for(File f : items){
            if(f.isDirectory()){
                sb.append(String.format("[DIR]  %s%n", f.getName()));
                carpetas++;
            }else{
                sb.append(String.format("       %-30s (%d bytes)%n", f.getName(), f.length()));
                archivos++;
            }
        }
        sb.append("\n").append(carpetas).append(" carpeta(s), ").append(archivos).append(" archivo(s).");
        return sb.toString();
    }
    
    private String Date(){
        LocalDate d=LocalDate.now();
        return "Fecha actual: "+d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String Time(){
        LocalTime t=LocalTime.now();
        return "Hora actual: "+t.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
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
        if(linea.trim().equalsIgnoreCase("EXIT")){
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
    
    private String Ren(String resto){
        int espacio=resto.indexOf(' ');
        if(espacio==-1){
            return "Uso: Ren <actual> <nuevo>";
        }
        String nombreActual=resto.substring(0,espacio);
        String nombreNuevo=resto.substring(espacio+1).trim();
        if(!nombreValido(nombreActual) || !nombreValido(nombreNuevo)){
            return "Error: el nombre ingresado no es valido.";
        }
        File origen=devolverRuta(nombreActual);
        File destino=devolverRuta(nombreNuevo);
        if(!origen.exists()){
            return "Error: no existe '"+nombreActual+"'.";
        }
        if(destino.exists()){
            return "Error: ya existe un archivo o carpeta llamado '"+nombreNuevo+"'.";
        }
        boolean fueRenombrado=origen.renameTo(destino);
        return fueRenombrado?"'"+nombreActual+"' renombrado a '"+nombreNuevo+"'."
                :"Error: no se pudo renombrar '"+nombreActual+"'.";
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
        if(!origen.exists()){
            return "Error: el archivo origen '"+nombreOrigen+"' no existe.";
        }
        if(origen.isDirectory()){
            return "Error: '"+nombreOrigen+"' es una carpeta, solo se pueden copiar archivos.";
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
    
    private String Find(String nombre){
        if(nombre.isEmpty()){
            return "Uso: Find <nombre>";
        }
        List<String> resultados=new ArrayList<>();
        buscarRecursivo(actual, nombre.toLowerCase(), resultados);
        if(resultados.isEmpty()){
            return "No se encontraron coincidencias para '"+nombre+"'.";
        }
        StringBuilder sb=new StringBuilder();
        sb.append("Resultados para '").append(nombre).append("':\n\n");
        for(String r : resultados){
            sb.append(r).append("\n");
        }
        sb.append("\n").append(resultados.size()).append(" resultado(s) encontrado(s).");
        return sb.toString();
    }
    
    private void buscarRecursivo(File carpeta, String textoBuscado, List<String> resultados){
        File[] items=carpeta.listFiles();
        if(items==null){
            return;
        }
        for(File f : items){
            if(f.getName().toLowerCase().contains(textoBuscado)){
                resultados.add(rutaVirtual(f));
            }
            if(f.isDirectory()){
                buscarRecursivo(f, textoBuscado, resultados);
            }
        }
    }
    
    private String Info(String nombre){
        if(!nombreValido(nombre)){
            return "Error: el nombre ingresado no es valido.";
        }
        File objetivo=devolverRuta(nombre);
        if(!objetivo.exists()){
            return "Error: no existe '"+nombre+"'.";
        }
        String tipo=objetivo.isDirectory()?"Carpeta":"Archivo";
        long tamano=objetivo.isDirectory()?tamanoRecursivo(objetivo):objetivo.length();
        SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String fecha=sdf.format(new Date(objetivo.lastModified()));
        StringBuilder sb=new StringBuilder();
        sb.append("Nombre: ").append(objetivo.getName()).append("\n");
        sb.append("Tipo: ").append(tipo).append("\n");
        sb.append("Ruta: ").append(rutaVirtual(objetivo)).append("\n");
        sb.append("Tamano: ").append(tamano).append(" bytes\n");
        sb.append("Ultima modificacion: ").append(fecha);
        return sb.toString();
    }
    
    private long tamanoRecursivo(File f){
        if(f.isFile()){
            return f.length();
        }
        long total=0;
        File[] hijos=f.listFiles();
        if(hijos!=null){
            for(File h : hijos){
                total+=tamanoRecursivo(h);
            }
        }
        return total;
    }
    
    private String Tree(){
        StringBuilder sb=new StringBuilder();
        sb.append("Estructura de ").append(rutaVirtual(actual)).append(":\n");
        sb.append(actual.getName()).append("\n");
        arbolRecursivo(actual, "", sb);
        return sb.toString();
    }
    
    private void arbolRecursivo(File carpeta, String prefijo, StringBuilder sb){
        File[] items=carpeta.listFiles();
        if(items==null){
            return;
        }
        Arrays.sort(items, (a, b) -> {
            if (a.isDirectory() != b.isDirectory()) {
                return a.isDirectory() ? -1 : 1;
            }
            return a.getName().compareToIgnoreCase(b.getName());
        });
        for(int i=0;i<items.length;i++){
            File f=items[i];
            boolean esUltimo=(i==items.length-1);
            sb.append(prefijo).append(esUltimo?"\\-- ":"|-- ").append(f.getName()).append("\n");
            if(f.isDirectory()){
                arbolRecursivo(f, prefijo+(esUltimo?"    ":"|   "), sb);
            }
        }
    }
    

    private String Help(){
        return String.join("\n",
                "Comandos disponibles:",
                "  Mkdir <nombre>            - Crea una carpeta",
                "  Mfile <nombre.ext>        - Crea un archivo",
                "  Rm <nombre>               - Elimina un archivo o carpeta",
                "  Cd <nombre carpeta>       - Cambia a la carpeta indicada",
                "  ..                        - Regresa a la carpeta anterior",
                "  Dir                       - Lista el contenido de la carpeta actual",
                "  Date                      - Muestra la fecha actual",
                "  Time                      - Muestra la hora actual",
                "  Wr <archivo.ext>          - Escribe texto en un archivo (EXIT para terminar)",
                "  Rd <archivo.ext>          - Lee el contenido de un archivo",
                "  Ap <archivo.ext>          - Agrega texto al final de un archivo (EXIT para terminar)",
                "  Ren <actual> <nuevo>      - Renombra un archivo o carpeta",
                "  Copy <origen> <destino>   - Copia un archivo",
                "  Find <nombre>             - Busca archivos o carpetas por nombre",
                "  Info <nombre>             - Muestra informacion de un archivo o carpeta",
                "  Tree                      - Muestra el arbol de carpetas y archivos",
                "  Cls                       - Limpia la pantalla",
                "  Help                      - Muestra esta ayuda",
                "  Exit                      - Cierra la aplicacion"
        );
    }
}
