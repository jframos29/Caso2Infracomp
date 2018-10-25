package Caso2;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import java.security.cert.CertificateFactory;
import java.util.Random;
import java.util.Scanner;

import javax.xml.bind.DatatypeConverter;

import java.security.cert.X509Certificate;

import org.bouncycastle.util.encoders.Base64;

import org.bouncycastle.util.encoders.Hex;


public class Cliente {


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


	public Cliente(){
		try{
			System.out.println("----------------Caso 2 - Infraestructura Computacional----------------");
			System.out.println("Integrantes:\nSergio Cárdenas 201613444, Juan Felipe Ramos 2016xxxxx, Maria Alejandra Abril 201xxxxxx");
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

			String algos = sc.next();
			String[] algoritmos = algos.split(","); 
			seguridad.SetAlgs(algoritmos);
			socketCliente = new Socket("localhost",puerto);
			socketCliente.setKeepAlive(true);
			writer = new PrintWriter(socketCliente.getOutputStream(), true);
			reader = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));			

			procesar();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		try {
			reader.close();
			socketCliente.close();
			writer.close();
			sc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
		long reto = 0;
		String comando = "";
		boolean responde = false;
		byte[]cifra;

		writer.println(HOLA);
		while(!termino){
			if(reader.ready()){
				esperando = true;
				comando = reader.readLine();
				if(comando == null || comando.equals(""))
					continue;
				else if(comando.toLowerCase().contains(ERROR.toLowerCase())) throw new Exception(comando);
				else if(comando.toLowerCase().contains(OK.toLowerCase())) System.out.println("Servidor: " + comando);

				switch(estado){
				case 0:
					if(comando.equals(OK)){
						System.out.println("INICIANDO");
						respuesta = AlGORITMOS;
						respuesta+= seguridad.darAlgos();
						writer.println(respuesta);
						estado=1;
					}
					break;
				case 1:
					if(comando.equals(OK)) {
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
					if(!comando.equals(OK)) {
						System.out.println("Se recibió el Certificado Digital del Servidor");
						System.out.println(comando);
						System.out.println("Procesando certificado...");
						CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
						InputStream in = new ByteArrayInputStream(Hex.decode(comando));
						X509Certificate certiServi = (X509Certificate) certFactory.generateCertificate(in);
						seguridad.setCertificado(certiServi);
						
						writer.println(OK);

						estado = 3;
					}
					break;
				case 3:
					cifra = Hex.decode(comando);
					String valor = seguridad.decifrarAsimetricamente(cifra);
					System.out.println("Llave secreta: "+valor);
					seguridad.setLlaveSimetrica(cifra);
					cifra = seguridad.cifrarAsimetrica(valor);
					cifra = Hex.encode(cifra);
					writer.println(new String(cifra));
					
					estado = 4;
					
					break;
				case 4:
					if(comando.equals(OK)) {
						System.out.println("Ingrese identificador de acceso");
						String id = sc.next();
						cifra = seguridad.cifrarSimetrica(id.getBytes());
						cifra = Hex.encode(cifra);
						writer.println(cifra);
						
						estado = 5;
					}
					
					break;
					
				case 5:
					if(!responde){
						byte[] llave = Hex.decode(comando);
						respuesta = seguridad.decifrarAsimetricamente(llave);
						seguridad.setLlaveSimetrica(respuesta.getBytes());
						System.out.println("Ingrese usuario");
						String usuario = sc.next();
						System.out.println("Ingrese clave");
						String clave =sc.next();
						String respuestaUs = usuario +","+clave;
						cifra = seguridad.cifrarSimetrica(respuestaUs.getBytes());
						String send = DatatypeConverter.printHexBinary(cifra);
						respuesta = Hex.toHexString(cifra);
						writer.println(send);

						estado = 5;
						responde = false;
					}	
					comando = seguridad.decifrarSimetricamente(Hex.decode(comando));
					if(!responde&&comando.equals(OK)){
						System.out.println("Consultando...");
						System.out.print("Ingrese su número de cédula\n>");
						String cedula = sc.next().trim();
						cifra = seguridad.cifrarSimetrica(cedula.getBytes());
						respuesta = new String(Hex.encode(cifra));
						respuesta += ":"+new String(Hex.encode(seguridad.cifrarSimetrica(seguridad.getLlaveDigest(cedula.getBytes()))));
						writer.println(respuesta);
						estado = 6;
						responde = false;
					}
					else if (comando.equals(ERROR))
						throw new Exception ("Ocurrio un error en el servidor");
					break;
				case 6:
					comando = seguridad.decifrarSimetricamente(Hex.decode(comando));
					if(!responde&&comando.equals(OK)) {
						termino = true;
						break;
					}
					else
						throw new Exception ("Ocurrio un error");
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
		new Cliente();
	}
}
