package Caso2;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.Mac;
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
		cipher.init(Cipher.ENCRYPT_MODE, certificado.getPublicKey());
		byte[] bytsCifra = cipher.doFinal(byts);
		return bytsCifra;	
	}

	public String decifrarSimetrica(byte[] arg) throws Exception{
		String PADDING = "";
		String res = "";
		if(algSimetrico.equals(Cliente.ALGS_SIMETRICOS[0])){
			res = "/ECB/PKCS5Padding";
		}
		PADDING = algSimetrico + res;
		Cipher cipher = Cipher.getInstance(PADDING);
		cipher.init(Cipher.DECRYPT_MODE, llave);
		byte[] cifrado = cipher.doFinal(arg);
		String resultado = new String(cifrado);
		return resultado;
	}
	
	
	public String decifrarAsimetrica(byte[] arg)throws Exception{
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

