package net.bioclipse.ds.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.SubStructureMatch;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.HighlightAtomGenerator.HighlightAtomDistance;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;


public class ImageHelper {

    public static byte[] createImage( net.bioclipse.core.domain.IMolecule bcmol,
                                      ITestResult match ) 
                                      throws BioclipseException {

        //Default values
        int WIDTH = 150;
        int HEIGHT = 150;

        return createImage(bcmol, match, WIDTH, HEIGHT, 0.4);
        
    }
    
    public static byte[] createImage( net.bioclipse.core.domain.IMolecule bcmol,
                        ITestResult match, int WIDTH, int HEIGHT, double zoom)
                                                     throws BioclipseException {

        if (bcmol==null)
            return null;
        
        ICDKManager cdk = net.bioclipse.cdk.business.Activator
                                              .getDefault().getJavaCDKManager();
        ICDKMolecule cdkmol= cdk.asCDKMolecule( bcmol );

        // the draw area and the image should be the same size
        Rectangle drawArea = new Rectangle(WIDTH, HEIGHT);
        Image image = new BufferedImage(
                WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        
        //Generate 2D
        IMolecule mol = new Molecule(cdkmol.getAtomContainer());
        StructureDiagramGenerator sdg = new StructureDiagramGenerator();
        sdg.setMolecule(mol, true);
        try {
            sdg.generateCoordinates();
        } catch (Exception e) { }
        mol = sdg.getMolecule();
        
        // generators make the image elements
        List<IGenerator<IAtomContainer>> generators = new ArrayList<IGenerator<IAtomContainer>>();

        //Add the standard generators
        generators.add(new BasicBondGenerator());

        SubStructureMatch newMatch=null;
        
        //If we have a match:
        //We need to generate a new ISubstructureMatch from pre-clone and SDG
//        if ( match instanceof SubStructureMatch ) {
//            SubStructureMatch submatch = (SubStructureMatch) match;
//
//            newMatch=new SubStructureMatch(submatch.getName(),
//                                                    submatch.getClassification());
//            newMatch.setTestRun( ((SubStructureMatch) match).getTestRun() );
//            IAtomContainer newac = NoNotificationChemObjectBuilder.getInstance().newAtomContainer();
//            for (IAtom atom : match.getAtomContainer().atoms()){
//                int atomno=cdkmol.getAtomContainer().getAtomNumber( atom );
//                IAtom newAtom=mol.getAtom( atomno );
//                newac.addAtom( newAtom );
//            }
//            
//            newMatch.setAtomContainer( newac );
//        }
        
        if (newMatch!=null){
            BlueRedColorScaleGenerator generator=new BlueRedColorScaleGenerator();
//            generator.add( newMatch );
            generators.add(generator);
        }
        
        generators.add(new BasicAtomGenerator());
        
        // the renderer needs to have a toolkit-specific font manager 
        AtomContainerRenderer renderer = new AtomContainerRenderer(generators, new AWTFontManager());
        
        // the call to 'setup' only needs to be done on the first paint
        renderer.setup(mol, drawArea);
        renderer.getRenderer2DModel().setRenderingParameter(HighlightAtomDistance.class, 18.0 );

        //TODO: belows does not seem to work properly
//        renderer.setZoomToFit( WIDTH, HEIGHT, WIDTH, HEIGHT );
        renderer.setZoom( zoom );
        
        // paint the background
        Graphics2D g2 = (Graphics2D)image.getGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        
        // the paint method also needs a toolkit-specific renderer
        renderer.paint(mol, new AWTDrawVisitor(g2));

        ByteArrayOutputStream bo=new ByteArrayOutputStream();
        
        try {
            ImageIO.write((RenderedImage)image, "PNG", bo);
//            ImageIO.write((RenderedImage)image, "PNG", new File("/Users/ola/tmp/molOLA.png"));
            bo.flush();
            bo.close();
            byte[] imagedata=bo.toByteArray();
            return imagedata;
        } catch ( IOException e1 ) {
            e1.printStackTrace();
        }
        
        return null;

    }
}
