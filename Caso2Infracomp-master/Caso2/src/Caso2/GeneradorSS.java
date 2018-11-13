package Caso2;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;



public class GeneradorSS {

    private LoadGenerator generador;
    private static PrintWriter escritor;
    public GeneradorSS() {
        Task clienteSS= new ClienteSS();
        int numTask =200;
        int gapBetween= 40;
        generador = new LoadGenerator("ClienteSS", numTask, clienteSS, gapBetween);
        generador.generate();
        escritor.println((ClienteSS.tiempoVerificacion.doubleValue()/((double)numTask - (double)ClienteSS.numPerdidas))+";"+
                (ClienteSS.tiempoRespuesta.doubleValue()/((double)numTask- (double)ClienteSS.numPerdidas))+";"+
                (ClienteSS.cpu/((double)numTask- (double)ClienteSS.numPerdidas))+";"+
                ((double)ClienteSS.numPerdidas/((double)numTask)+"\n"));
        ClienteSS.tiempoVerificacion = 0L;
        ClienteSS.tiempoRespuesta = 0L;
        ClienteSS.numPerdidas = 0;
        ClienteSS.cpu = 0;

    }

    public static void main(String[] args) {
        try{
            escritor = new PrintWriter(new FileOutputStream(new File("./data/pruebasSS200_40_2.csv"), true));
            for(int i = 0; i<1; i++) {
            	new GeneradorSS();
            	Thread.sleep(5000);
            }
            escritor.close();
        }
        catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
