package Caso2;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

import java.io.File;
import java.io.PrintWriter;



public class GeneradorSS {

    private LoadGenerator generador;
    private static PrintWriter escritor;
    public GeneradorSS() {
        Task clienteSS= new ClienteSS();
        int numTask =80;
        int gapBetween= 100;
        generador = new LoadGenerator("ClienteSS", numTask, clienteSS, gapBetween);
        generador.generate();

        escritor.println((ClienteSS.tiempoVerificacion.doubleValue()/((double)numTask - (double)ClienteSS.numPerdidas))+";"+
                (ClienteSS.tiempoRespuesta.doubleValue()/((double)numTask- (double)ClienteSS.numPerdidas))+";"+
                ((double)ClienteSS.numPerdidas/((double)numTask)+"\n"));


    }

    public static void main(String[] args) {
        try {
            escritor = new PrintWriter(new File("./data/pruebasSS80_100_8.csv"));

            for(int i = 0; i< 10 ; i++) {
                escritor.write((i+1)+";");
                new GeneradorSS();
                Long start = System.currentTimeMillis();
                while((System.currentTimeMillis()-start)<5000);
                ClienteSS.tiempoVerificacion = 0L;
                ClienteSS.tiempoRespuesta = 0L;
                ClienteSS.numPerdidas = 0;
            }
            escritor.close();

        }catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }}
