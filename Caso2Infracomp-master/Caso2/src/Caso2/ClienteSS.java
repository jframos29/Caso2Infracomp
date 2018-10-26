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

import javax.xml.bind.DatatypeConverter;

import java.security.cert.X509Certificate;

import org.bouncycastle.util.encoders.Hex;


public class ClienteSS {


	public static final String HOLA = "HOLA";
	public static final String OK = "OK";
	public static final String AlGORITMOS = "ALGORITMOS";
	public static final String ERROR = "ERROR";
	public static final String SEPARADOR = ":";



	public static final String[] ALGS_SIMETRICOS = {"AES","Blowfish"};
	public static final String[] ALGS_ASIMETRICOS = {"RSA"};
	public static final String[] ALGS_HMAC = {"HMACMD5","HMACSHA1","HMACSHA256"};


	private Socket socketCliente;


	private Scanner sc;

	private BufferedReader reader;

	private PrintWriter writer;

	private Seguridad seguridad;


	public ClienteSS(){
		try{
			System.out.println("----------------Caso 2 - Infraestructura Computacional----------------");
			System.out.println("Integrantes:\nSergio Cárdenas 201613444, Juan Felipe Ramos 201616932, Maria Alejandra Abril 201530720");
			sc = new Scanner(System.in);
			seguridad = new Seguridad();

			System.out.println("Ingrese el puerto al que se quiere conectar:\n ");
			int puerto = sc.nextInt();
			System.out.println("\nListado de algoritmos disponibles:\n ");

			System.out.println("Algoritmos Simétricos:");
			for(int i = 0 ; i< ALGS_SIMETRICOS.length; i++){
				System.out.println(ALGS_SIMETRICOS[i]);
			}
			System.out.println("\nAlgoritmos Asimétricos");
			for(int i = 0 ; i< ALGS_ASIMETRICOS.length; i++){
				System.out.println(ALGS_ASIMETRICOS[i]);
			}
			System.out.println("\nAlgoritmos de Hash");
			for(int i = 0; i<ALGS_HMAC.length;i++){
				System.out.println(ALGS_HMAC[i]);
			}
			System.out.println("\nIngrese los algoritmos que desea usar separados por comas:");
			String algos = sc.next();
			String[] algoritmos = algos.split(","); 
			seguridad.SetAlgs(algoritmos);
			socketCliente = new Socket("localhost",puerto);
			socketCliente.setKeepAlive(true);
			writer = new PrintWriter(socketCliente.getOutputStream(), true);
			reader = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));			

			procesar();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		try {
			reader.close();
			socketCliente.close();
			writer.close();
			sc.close();
		}
		catch (IOException e) {
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


	public void  procesar() throws Exception{
 		boolean termino = false;
		boolean esperando = true;
		int estado = 0;
		String respuesta = "";
		String line = "";
		byte[] buffer;

		writer.println(HOLA);
		while(!termino){
			if(reader.ready()){
				esperando = true;
				line = reader.readLine();
				if(line == null || line.equals(""))
					continue;
				else if(line.toLowerCase().contains(ERROR.toLowerCase()) && estado != 5) throw new Exception(line);
				else if(line.toLowerCase().contains(OK.toLowerCase())) System.out.println("Servidor: " + line);

				switch(estado){
				case 0:
					if(line.equals(OK)){
						System.out.println("INICIANDO");
						respuesta = AlGORITMOS;
						respuesta+= seguridad.darAlgos();
						writer.println(respuesta);
						estado=1;
					}
					break;
				case 1:
					if(line.equals(OK)) {
						System.out.println("Se intercambiará el Certificado Digital");
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
					if(!line.equals(OK)) {
						System.out.println("Se recibió el Certificado Digital del Servidor");
						System.out.println(line);
						System.out.println("Procesando certificado...");
						
						writer.println(OK);

						estado = 3;
					}
					break;
				case 3:
					System.out.println("Llave secreta recibida");
					System.out.println("Enviando llave secreta...");
					writer.println(line);
					
					estado = 4;
					
					break;
				case 4:
					if(line.equals(OK)) {
						System.out.println("Ingrese su identificador de acceso:");
						String id = sc.nextInt()+"";
						writer.println(id);
						writer.println(id);
						
						estado = 5;
					}
					
					break;
					
				case 5:
					if(line.contains(OK)) {
						System.out.println("Estado: "+line.split(":")[1]);
					}
					else
						System.out.println("Hubo un error al realizar la consulta: "+line);
					
					termino = true;
					
				default: 
					estado = 0;
					break;
				}
			}else{
				if(esperando){
					System.out.println("waiting...");
					esperando = false;
				}
			}
		}

	}

	public static void main(String[] args) {
		new ClienteSS();
	}
}
