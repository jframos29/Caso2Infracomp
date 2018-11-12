package Caso2;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import java.security.cert.CertificateFactory;
import java.util.Scanner;

import java.security.cert.X509Certificate;

import org.bouncycastle.util.encoders.Hex;
import uniandes.gload.core.Task;


public class Cliente extends Task {


    public static final String HOLA = "HOLA";
    public static final String OK = "OK";
    public static final String AlGORITMOS = "ALGORITMOS";
    public static final String ERROR = "ERROR";
    public static final String SEPARADOR = ":";


    public static final String[] ALGS_SIMETRICOS = {"AES", "Blowfish"};
    public static final String[] ALGS_ASIMETRICOS = {"RSA"};
    public static final String[] ALGS_HMAC = {"HMACMD5", "HMACSHA1", "HMACSHA256"};


    private Socket socketCliente;


    private Scanner sc;

    private BufferedReader reader;

    private PrintWriter writer;

    private Seguridad seguridad;

    public static Long tiempoVerificacion = 0L;

    public static Long tiempoRespuesta = 0L;

    public static int numPerdidas = 0;

    public static double cpu = 0;

    public static Long t1, t2, t3, t4;

    public Cliente() {
        try {
            System.out.println("----------------Caso 2 - Infraestructura Computacional----------------");
            System.out.println("Integrantes:\nSergio C�rdenas 201613444, Juan Felipe Ramos 201616932, Maria Alejandra Abril 201530720");
            sc = new Scanner(System.in);
            seguridad = new Seguridad();

            System.out.println("Ingrese el puerto al que se quiere conectar:\n ");
            //int puerto = sc.nextInt();
            int puerto = 10234;
            //System.out.println("\nListado de algoritmos disponibles:\n ");

            //System.out.println("Algoritmos Sim�tricos:");
            //for(int i = 0 ; i< ALGS_SIMETRICOS.length; i++){
            //System.out.println(ALGS_SIMETRICOS[i]);
            //}
            //System.out.println("\nAlgoritmos Asim�tricos");
            //for(int i = 0 ; i< ALGS_ASIMETRICOS.length; i++){
            //System.out.println(ALGS_ASIMETRICOS[i]);
            //}
            //System.out.println("\nAlgoritmos de Hash");
            //for(int i = 0; i<ALGS_HMAC.length;i++){
            //System.out.println(ALGS_HMAC[i]);
            //}
            //System.out.println("\nIngrese los algoritmos que desea usar separados por comas:");
            //String algos = sc.next();
            //String[] algoritmos = algos.split(",");
            String[] algoritmos = {"AES", "RSA", "HMACMD5"};
            seguridad.SetAlgs(algoritmos);
            socketCliente = new Socket("34.219.170.87", puerto);
            socketCliente.setKeepAlive(true);
            writer = new PrintWriter(socketCliente.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));

            procesar();
            //System.out.println("Tiempo verificación en millis: "+tiempoVerificacion);
            //System.out.println("Tiempo respuesta consulta en millis: "+tiempoRespuesta);
        } catch (Exception e) {
            numPerdidas++;
            //System.out.println("Cantidad de perdidas actual: "+numPerdidas);
            e.printStackTrace();
        }
        try {
            reader.close();
            socketCliente.close();
            writer.close();
            //sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static String convertByteArrayHexa(byte[] byteArray) {
        String out = "";
        for (int i = 0; i < byteArray.length; i++) {
            if ((byteArray[i] & 0xff) <= 0xf) {
                out += "0";
            }
            out += Integer.toHexString(byteArray[i] & 0xff).toUpperCase();
        }

        return out;
    }


    public void procesar() throws Exception {
        boolean termino = false;
        boolean esperando = true;
        int estado = 0;
        String respuesta = "";
        String line = "";
        byte[] buffer;

        writer.println(HOLA);
        while (!termino) {
            if (reader.ready()) {
                esperando = true;
                line = reader.readLine();
                if (line == null || line.equals(""))
                    continue;
                else if (line.toLowerCase().contains(ERROR.toLowerCase()) && estado != 5) throw new Exception(line);
                else if (line.toLowerCase().contains(OK.toLowerCase())) System.out.println("Servidor: " + line);

                switch (estado) {
                    case 0:
                        if (line.equals(OK)) {
                            System.out.println("INICIANDO");
                            respuesta = AlGORITMOS;
                            respuesta += seguridad.darAlgos();
                            writer.println(respuesta);
                            estado = 1;
                        }
                        break;
                    case 1:
                        if (line.equals(OK)) {
                            System.out.println("Se intercambiar� el Certificado Digital");
                            seguridad.setLlaveAsimetrica();
                            java.security.cert.X509Certificate certi = seguridad.crearCertificado();
                            byte[] bytesCertiPem = certi.getEncoded();
                            String certiString = new String(Hex.toHexString(bytesCertiPem));
                            String certiFinal = certiString;
                            writer.println(certiFinal);

                            estado = 2;
                        }
                        break;
                    case 2:
                        if (!line.equals(OK)) {
                            System.out.println("Se recibi� el Certificado Digital del Servidor");
                            System.out.println(line);
                            System.out.println("Procesando certificado...");
                            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                            InputStream in = new ByteArrayInputStream(Hex.decode(line));
                            X509Certificate certiServi = (X509Certificate) certFactory.generateCertificate(in);
                            seguridad.setCertificado(certiServi);

                            writer.println(OK);
                            t1 = System.currentTimeMillis();
                            estado = 3;
                        }
                        break;
                    case 3:
                        buffer = Hex.decode(line);

                        String valor = seguridad.decifrarAsimetrica(buffer);
                        seguridad.setLlaveSimetrica(valor.getBytes());
                        System.out.println(valor.getBytes());
                        System.out.println("Llave secreta recibida");
                        System.out.println("Enviando llave secreta...");
                        buffer = seguridad.cifrarAsimetrica(valor);
                        buffer = Hex.encode(buffer);
                        writer.println(new String(buffer));
                        System.out.println("Enviada");
                        estado = 4;

                        break;
                    case 4:
                        if (line.equals(OK)) {
                            t2 = System.currentTimeMillis();
                            tiempoVerificacion += t2 - t1;
                            //System.out.println("Ingrese su identificador de acceso:");
                            //String id = sc.nextInt()+"";
                            String id = "1";
                            buffer = seguridad.cifrarSimetrica(id.getBytes());
                            buffer = Hex.encode(buffer);
                            writer.println(new String(buffer));
                            t3 = System.currentTimeMillis();
                            buffer = seguridad.getLlaveDigest((id.getBytes()));
                            buffer = Hex.encode(buffer);
                            writer.println(new String(buffer));

                            estado = 5;
                        }

                        break;

                    case 5:
                        t4 = System.currentTimeMillis();
                        tiempoRespuesta += t4 - t3;
                        if (line.contains(OK)) {
                            System.out.println("Estado: " + line.split(":")[1]);
                        } else
                            System.out.println("Hubo un error al realizar la consulta: " + line);

                        writer.println("CPU");
                        estado = 6;
                        break;

                    case 6:
                        if (line.contains(OK)) {
                            String hola = line.split(":")[1];
                            cpu += Double.parseDouble(hola);
                        }
                        termino = true;
                        break;

                    default:
                        estado = 0;
                        break;
                }
            } else {
                if (esperando) {
                    //System.out.println("waiting...");
                    esperando = false;
                }
            }
        }

    }

    @Override
    public void fail() {
        // TODO Auto-generated method stub

    }

    @Override
    public void success() {
        // TODO Auto-generated method stub

    }

    @Override
    public void execute() {
        // TODO Auto-generated method stub
        Cliente c = new Cliente();
    }

    public static void main(String[] args) {
        new Cliente();
    }
}
