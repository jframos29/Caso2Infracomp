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
        int numTask =3;
        int gapBetween= 5000;
        generador = new LoadGenerator("Cliente", numTask, cliente, gapBetween);
        generador.generate();

        escritor.println(Cliente.tiempoVerificacion.doubleValue()/((double)numTask - (double)Cliente.numPerdidas)+";"+
                Cliente.tiempoRespuesta.doubleValue()/((double)numTask- (double)Cliente.numPerdidas)+";"+
                (Cliente.cpu/((double)numTask- (double)Cliente.numPerdidas))+";"+
                (double)Cliente.numPerdidas/((double)numTask)+"\n");
        Cliente.tiempoVerificacion = 0L;
        Cliente.tiempoRespuesta = 0L;
        Cliente.numPerdidas = 0;
        Cliente.cpu = 0;


    }

    public static void main(String[] args) {
        try {
            escritor = new PrintWriter(new File("pruebas400_20_8_1.csv"));
            new Generador();
            escritor.close();

        }catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

}
