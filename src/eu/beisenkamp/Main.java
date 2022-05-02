package eu.beisenkamp;

import GLOOP.GLVektor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bertelte
 */
public class Main {


    static int ystart = 25;
    static int yschritt = 1;
    static int ymax = 150;
    static int yBohrungMax = ystart + 15;
    static double xBohrung = 0.15;
    static double zBohrung = 0.02;
    static double radiusBohrung = 0.0015;
    static double lymax = 0.05;
    static double lystart = 0.08;
    static double br = 1;
    static double yspitze = 158.0;
    static GLVektor V = new GLVektor(-0.01,0,0);


    public static void main(String[] args) {


        List<GLVektor> profile = new ArrayList<>();

        try(Scanner scanner = new Scanner(new File("seligdatfile.txt"))){
            while (scanner.hasNext()){
                String lineTemp = scanner.nextLine();
                String[] line = lineTemp.trim().split(" ");
                double x1 = Double.parseDouble(line[0]);
                double x3 = Double.parseDouble(line[1]);
                GLVektor x1x3 = new GLVektor(x1,0,x3);
                profile.add(x1x3);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        try(PrintWriter writer = new PrintWriter(
                new FileOutputStream(
                        new File("objekt.stl")))){


            writer.println("solid fluegel");


            for(int y=ystart; y< ymax; y+=yschritt){

                for(int zaehler = 0; zaehler < profile.size()-1 ;zaehler++){

                    //untere Punkte aus der Liste
                    GLVektor ap = new GLVektor( profile.get(zaehler));
                    ap.multipliziere(skalierungsfaktor(y));
                    ap.rotiere(rotationsfaktor(y), 0, 1, 0);
                    GLVektor np = new GLVektor(profile.get(zaehler+1));
                    np.multipliziere(skalierungsfaktor(y));
                    np.rotiere(rotationsfaktor(y), 0, 1, 0);

                    //obere Punkte aus der Liste
                    GLVektor apo = new GLVektor(profile.get(zaehler));
                    apo.multipliziere(skalierungsfaktor(y+yschritt));
                    apo.rotiere(rotationsfaktor(y+yschritt), 0, 1, 0);
                    GLVektor npo = new GLVektor(profile.get(zaehler+1));
                    npo.multipliziere(skalierungsfaktor(y+yschritt));
                    npo.rotiere(rotationsfaktor(y+yschritt), 0, 1, 0);

                    //Flügel
                    GLVektor A = new GLVektor(ap.x*br,y/1000.0,ap.z*br);
                    GLVektor B = new GLVektor(np.x*br,y/1000.0,np.z*br);
                    GLVektor D = new GLVektor(apo.x*br,(y+yschritt)/1000.0,apo.z*br);
                    GLVektor C = new GLVektor(npo.x*br,(y+yschritt)/1000.0,npo.z*br);
                    writeFacet(writer,A,B,C);
                    writeFacet(writer,A,C,D);

                    //Bohrung
                    double startwinkel = -90;
                    GLVektor apb = new GLVektor(xBohrung, 0, zBohrung);
                    apb.multipliziere(skalierungsfaktor(ystart));
                    apb.rotiere(rotationsfaktor(ystart), 0, 1, 0);
                    GLVektor radius = new GLVektor(0, 0, radiusBohrung);
                    radius.rotiere(-startwinkel - 360.0 * zaehler / (profile.size()-1), 0, 1, 0);
                    apb.addiere(radius);
                    GLVektor npb = new GLVektor(xBohrung, 0, zBohrung);
                    npb.multipliziere(skalierungsfaktor(ystart));
                    radius = new GLVektor(0, 0, radiusBohrung);
                    npb.rotiere(rotationsfaktor(ystart), 0, 1, 0);
                    radius.rotiere(-startwinkel - 360.0 * (zaehler + 1) / (profile.size()-1), 0, 1, 0);
                    npb.addiere(radius);
                    GLVektor Ab = new GLVektor(apb.x * br, y / 1000.0, apb.z * br);
                    GLVektor Bb = new GLVektor(npb.x * br, y / 1000.0, npb.z * br);
                    GLVektor Db = new GLVektor(apb.x * br, (y + yschritt) / 1000.0, apb.z * br);
                    GLVektor Cb = new GLVektor(npb.x * br, (y + yschritt) / 1000.0, npb.z * br);
                    if(y < yBohrungMax) {
                        writeFacet(writer, Ab, Cb, Bb);
                        writeFacet(writer, Ab, Db, Cb);
                    }

                    //Untere Flügelfläche
                    if (y == ystart){
                        /*GLVektor C1 = new GLVektor(profile.get(0));
                        C1.multipliziere(skalierungsfaktor(ystart));
                        C1.rotiere(rotationsfaktor(ystart), 0, 1, 0);
                        GLVektor C2 = new GLVektor(C1.x*br, ystart/1000.0, C1.z*br);
                        writeFacet(writer,C2,A,B);*/
                        writeFacet(writer, A, B, Ab);
                        writeFacet(writer, Bb, Ab, B);
                    }
                    if(y == yBohrungMax) {
                        GLVektor center = new GLVektor(xBohrung, 0, zBohrung);
                        center.multipliziere(skalierungsfaktor(ystart));
                        center.rotiere(rotationsfaktor(ystart), 0, 1, 0);
                        center = new GLVektor(center.x*br,yBohrungMax/1000.0,center.z*br);
                        writeFacet(writer, Ab,Bb,center);
                    }


                    //Obere Flügelfläche
                    if (y == ymax-yschritt){
                        GLVektor C1 = new GLVektor(profile.get(0));
                        C1.multipliziere(skalierungsfaktor(ymax));
                        C1.rotiere(rotationsfaktor(ymax), 0, 1, 0);
                        GLVektor C2 = new GLVektor(C1.x*br, ymax/1000.0, C1.z*br);
                        writeFacet(writer,C2,D,C);
                    }

                }
            }
            // Flügelspitze
           /* for(int y=ymax; y< yspitze; y+=yschritt){

                for(int zaehler = 0; zaehler < profile.size()-1 ;zaehler++){



                    //untere Punkte aus der Liste
                    GLVektor ap = new GLVektor( profile.get(zaehler));
                    ap.multipliziere(skalierungsfaktor(y));
                    ap.rotiere(rotationsfaktor(ymax), 0, 1, 0);
                    GLVektor np = new GLVektor(profile.get(zaehler+1));
                    np.multipliziere(skalierungsfaktor(y));
                    np.rotiere(rotationsfaktor(ymax), 0, 1, 0);

                   //obere Punkte aus Liste
                    GLVektor apo = new GLVektor(profile.get(zaehler));
                    apo.multipliziere(skalierungsfaktor(y+yschritt));
                    apo.rotiere(rotationsfaktor(ymax), 0, 1, 0);
                    GLVektor npo = new GLVektor(profile.get(zaehler+1));
                    npo.multipliziere(skalierungsfaktor(y+yschritt));
                    npo.rotiere(rotationsfaktor(ymax), 0, 1, 0);

                    //Verschiebung
                    ap.addiere(V);
                    np.addiere(V);
                    apo.addiere(V);
                    npo.addiere(V);


                    //XRotation
                    ap.rotiere(xrotationsfaktor(y), 0, 0, 1);
                    np.rotiere(xrotationsfaktor(y), 0, 0, 1);
                    apo.rotiere(xrotationsfaktor(y+yschritt), 0, 0, 1);
                    npo.rotiere(xrotationsfaktor(y+yschritt), 0, 0, 1);

                     //Rückverschiebung
                    ap.subtrahiere(V);
                    np.subtrahiere(V);
                    apo.subtrahiere(V);
                    npo.subtrahiere(V);



                    //Flügel
                    GLVektor A = new GLVektor(ap.x*br,ap.y+ymax/1000.0,ap.z*br);
                    GLVektor B = new GLVektor(np.x*br,np.y+ymax/1000.0,np.z*br);
                    GLVektor D = new GLVektor(apo.x*br,apo.y+ymax/1000.0,apo.z*br);
                    GLVektor C = new GLVektor(npo.x*br,npo.y+ymax/1000.0,npo.z*br);
                    writeFacet(writer,A,B,C);
                    writeFacet(writer,A,C,D);


                }
            }*/



            writer.println("endsolid");


        }   catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void writeFacet(PrintWriter pwriter, GLVektor pA, GLVektor pB, GLVektor pC){
        GLVektor ab = new GLVektor(pA,pB);
        GLVektor ac = new GLVektor(pA,pC);


        GLVektor normal = ab.gibKreuzprodukt(ac);

        pwriter.println(" facet normal "+ normal.x+ " "+ normal.y+" "+normal.z);
        pwriter.println("  outer loop");
        pwriter.println("   vertex "+ pA.x+ " "+ pA.y+" "+pA.z);
        pwriter.println("   vertex "+ pB.x+ " "+ pB.y+" "+pB.z);
        pwriter.println("   vertex "+ pC.x+ " "+ pC.y+" "+pC.z);
        pwriter.println("  endloop");
        pwriter.println(" endfacet");

    }

    private static double skalierungsfaktor(int py) {
        /*double m = (lymax-lystart)/(ymax-ystart);
        double b = (lymax-m*ymax);
        return m*py+b;*/
        return lystart*ystart*1.0/py;
    }

    private static double rotationsfaktor(int py){
        double slz = 5;
        double alpha = Math.atan(slz*py/ymax);
        return Math.toDegrees(alpha);
    }

    private static double xrotationsfaktor (int py){
        double m = 90/(yspitze-ymax);
        double b = 90-m*yspitze;
        return -(m*py+b);

    }
}