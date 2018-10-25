package Caso2;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.x509.X509V3CertificateGenerator;

public class Seguridad {



	private String algSimetrico;

	private SecretKey llave;



	private String algAsimetrico;


	private String AlgHMAC;

	
	private KeyPair pair;

	
	private X509Certificate certificado;
	
	public String getAlgSimetrico() {
		return algSimetrico;
	}
	public void setAlgSimetrico(String algSimetrico) {
		this.algSimetrico = algSimetrico;
	}
	public SecretKey getLlave() {
		return llave;
	}
	public void setLlave(SecretKey llave) {
		this.llave = llave;
	}
	public String getAlgAsimetrico() {
		return algAsimetrico;
	}
	public void setAlgAsimetrico(String algAsimetrico) {
		this.algAsimetrico = algAsimetrico;
	}
	public String getAlgHMAC() {
		return AlgHMAC;
	}
	public void setAlgHMAC(String algHMAC) {
		AlgHMAC = algHMAC;
	}
	public KeyPair getPair() {
		return pair;
	}
	public void setPair(KeyPair pair) {
		this.pair = pair;
	}
	public X509Certificate getCertificado() {
		return certificado;
	}
	public void setCertificado(X509Certificate certificado) {
		this.certificado = certificado;
	}
	

	public String darAlgos(){
		return ":"+algSimetrico+":"+algAsimetrico+":"+AlgHMAC;
	}
	
	public void SetAlgs(String[] algs){
		algSimetrico = algs[0];
		algAsimetrico = algs[1];
		AlgHMAC = algs[2];
	}
	
	
	public void setLlaveSimetrica(byte[] valor)throws Exception{
		llave = new SecretKeySpec(valor, algSimetrico);
	}
	
	public void setLlaveAsimetrica()throws Exception{
		KeyPairGenerator generador = KeyPairGenerator.getInstance(algAsimetrico);
		SecureRandom ran = SecureRandom.getInstance("SHA1PRNG");
		generador.initialize(1024,ran);
		pair = generador.generateKeyPair();
	}
	
	public byte[] cifrarSimetrica(byte[] arg) throws Exception{
		String PADDING = "";
		String res = "";
		if(algSimetrico.equals(Cliente.ALGS_SIMETRICOS[0])){
			System.out.println("Algoritmo: "+Cliente.ALGS_SIMETRICOS[0]);
			res = "/ECB/PKCS5Padding";
		}
		PADDING = algSimetrico + res;
		Cipher cipher = Cipher.getInstance(PADDING);
		cipher.init(Cipher.ENCRYPT_MODE, llave);
		return cipher.doFinal(arg);
	}
	
	public byte[] cifrarAsimetrica(String arg)throws Exception{

		Cipher cipher = Cipher.getInstance(algAsimetrico);
		byte[] byts = arg.getBytes();
		String txt = new String(byts);
		System.out.println("la clave original es: "+ txt);
		cipher.init(Cipher.ENCRYPT_MODE, certificado.getPublicKey());

		byte[] bytsCifra = cipher.doFinal(byts);

		System.out.println("clave Cifrada "+ bytsCifra);
		return bytsCifra;	
	}

	public String decifrarSimetricamente(byte[] arg) throws Exception{
		String PADDING = "";
		String res = "";
		if(algSimetrico.equals(Cliente.ALGS_SIMETRICOS[0])|| algAsimetrico.equals(Cliente.ALGS_SIMETRICOS[1])){
			res = "/ECB/PKCS5Padding";
		}
		PADDING = algSimetrico + res;
		Cipher cipher = Cipher.getInstance(PADDING);
		cipher.init(Cipher.DECRYPT_MODE, llave);
		byte[] cifrado = cipher.doFinal(arg);
		String resultado = new String(cifrado);
		return resultado;
	}
	
	
	public String decifrarAsimetricamente(byte[] arg)throws Exception{
		Cipher ci = Cipher.getInstance(algAsimetrico);
		ci.init(Cipher.DECRYPT_MODE, pair.getPrivate());
		byte[] txtOriginal = ci.doFinal(arg);
		String originalString = new String(txtOriginal);
		return originalString;

	}
	
	public X509Certificate crearCertificado() throws Exception{
		Date fechaInicio = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		Date fechaFin = calendar.getTime();             
		BigInteger numeroSerie = new BigInteger(""+Math.abs(SecureRandom.getInstance("SHA1PRNG").nextLong()));  
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
		X500Principal dn = new X500Principal("CN=Test CA Certificate");
		certGen.setSerialNumber(numeroSerie);
		certGen.setIssuerDN(dn);
		certGen.setNotBefore(fechaInicio);
		certGen.setNotAfter(fechaFin);
		certGen.setSubjectDN(dn);                       
		certGen.setPublicKey(pair.getPublic());
		certGen.setSignatureAlgorithm("SHA1withRSA");

		return certGen.generate(pair.getPrivate());
	}
	
	public byte[] getLlaveDigest(byte[] buffer) throws Exception{
		Mac mac = Mac.getInstance(AlgHMAC);
	    mac.init(llave);
	    byte[] bytes = mac.doFinal(buffer);
	    return bytes;
	}



}

