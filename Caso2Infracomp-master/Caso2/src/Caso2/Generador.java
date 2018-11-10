package Caso2;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

import java.io.File;
import java.io.PrintWriter;

public class Generador {

    private LoadGenerator generador;
    private static PrintWriter escritor;
    public Generador() {
        Task cliente = new Cliente();
        int numTask =80;
        int gapBetween= 100;
        generador = new LoadGenerator("Cliente", numTask, cliente, gapBetween);
        generador.generate();

        escritor.println(Cliente.tiempoVerificacion.doubleValue()/((double)numTask - (double)Cliente.numPerdidas)+";"+
                Cliente.tiempoRespuesta.doubleValue()/((double)numTask- (double)Cliente.numPerdidas)+";"+
                (double)Cliente.numPerdidas/((double)numTask)+"\n");


    }

    public static void main(String[] args) {
        try {
            escritor = new PrintWriter(new File("./data/pruebas80_100_2.csv"));

            for(int i = 0; i< 10 ; i++) {
                escritor.write((i+1)+";");
                new Generador();

                Long start = System.currentTimeMillis();
                while((System.currentTimeMillis()-start)<5000);
                Cliente.tiempoVerificacion = 0L;
                Cliente.tiempoRespuesta = 0L;
                Cliente.numPerdidas = 0;
            }
            escritor.close();

        }catch (Exception e) {
            // TODO: handle exception

            e.printStackTrace();
        }

    }

}
