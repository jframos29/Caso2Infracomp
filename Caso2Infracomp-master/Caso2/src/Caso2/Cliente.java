package Caso2;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
	public static final String CERTSRV = "CERTSRV:";
	public static final String CERTCLNT = "CERTCLNT";
	public static final String SEPARADOR = ":";



	public static final String[] ALGS_SIMETRICOS = {"DES","AES","Blowfish", "RC4"};
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
			System.out.println("Listado de algoritmos disponible:\n ");

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
		String certificado = "";
		boolean responde = false;
		Random rand = new Random();
		byte[]cifra;

		writer.println(HOLA);
		while(!termino){
			if(reader.ready()){
				esperando = true;
				comando = reader.readLine();
				if(comando == null || comando.equals(""))continue;
				else if(comando.toLowerCase().contains(ERROR.toLowerCase())) throw new Exception(comando);
				else if(estado ==1 && responde) certificado +=comando+"\n";
				else System.out.println("Servidor: " + comando);

				switch(estado){
				case 0:
					if(comando.equals(OK)){
						System.out.println("INICIANDO");
						respuesta = AlGORITMOS;
						respuesta+= seguridad.darAlgos();
						writer.println(respuesta);
						estado=1;
						responde=false;
					}
					break;
				case 1:
					if(comando.equals(OK) && !responde) {
						System.out.println("Se intercambiará el Certificado Digital");
						seguridad.setLlaveAsimetrica();
						java.security.cert.X509Certificate certi = seguridad.crearCertificado();
						byte[] bytesCertiPem = certi.getEncoded();
						String certiString = new String(convertByteArrayHexa(bytesCertiPem));
						String certiFinal = certiString;
						writer.println(certiFinal);

						responde = true;
					}
					else if(responde && !comando.equals(OK)) {
						System.out.println("Se recibió el Certificado Digital del Servidor");
						certificado = certificado.replace(CERTSRV, "");
						System.out.println(certificado);
						PrintWriter pw = new PrintWriter(new FileOutputStream("data/cert.txt"));
						pw.print(comando);
						pw.close();
						System.out.println("Procesando...");
						reto = Math.abs(rand.nextLong());
						CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
						X509Certificate certiServi = (X509Certificate) certFactory.generateCertificate(new FileInputStream("data/cert.txt"));
						seguridad.setCertificado(certiServi);
						cifra = seguridad.cifrarAsimetrica(""+reto);
						cifra = Hex.encode(cifra);
						
						writer.println(OK);
						Thread.sleep(500);
						writer.println(new String(cifra));

						estado = 2;
						responde=false;
					}
					
					break;
				case 2:
					if(!responde){

						cifra = Hex.decode(comando);
						String valor = new String(cifra);
						if(valor.equals(""+reto))writer.println(OK);
						else throw new Exception("El reto recibido del servidor no coincide con el enviado");

						estado = 3;
						responde=false;
					}
					break;
				case 3:
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

						estado = 4;
						responde = false;
					}	
					break;
				case 4:
					comando = seguridad.decifrarSimetricamente(Hex.decode(comando));
					if(!responde&&comando.equals(OK)){
						System.out.println("Consultando...");
						System.out.print("Ingrese su número de cédula\n>");
						String cedula = sc.next().trim();
						cifra = seguridad.cifrarSimetrica(cedula.getBytes());
						respuesta = new String(Hex.encode(cifra));
						respuesta += ":"+new String(Hex.encode(seguridad.cifrarSimetrica(seguridad.getLlaveDigest(cedula.getBytes()))));
						writer.println(respuesta);
						estado = 5;
						responde = false;
					}
					else if (comando.equals(ERROR))
						throw new Exception ("Ocurrio un error en el servidor");
					break;
				case 5:
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
