/* *****************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.tests;

import static org.junit.Assert.*;

import java.util.List;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.ISubStructure;
import net.bioclipse.core.api.BioclipseException;
import net.bioclipse.core.api.domain.IMolecule;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.business.IDSManager;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.jobs.BioclipseUIJob;

import org.junit.Test;
import org.openscience.cdk.interfaces.IAtom;


public class TestSignSIc {

    @Test
    public void runSignSicTest() throws BioclipseException, DSException{

        IDSManager ds = Activator.getDefault().getJavaManager();
        ICDKManager cdk=net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();

        ICDKMolecule mol = cdk.fromSMILES( "C1CCCCC1CC(CCC)CCC" );
        List<ITestResult> ret = ds.runTest( "signsic.bursi", mol );
        assertNotNull( ret);

        System.out.println("=============================");
        System.out.println("Results:");
        for (ITestResult res : ret){
            System.out.println(res);
        }
        System.out.println("=============================");

        //Some QA
        assertTrue( ret.size()==1 );
        ITestResult testres = ret.get( 0 );
        
        assertTrue( testres instanceof ISubStructure );
        ISubStructure submatch=(ISubStructure)testres;
        
        assertEquals( "HIT 1", testres.getName());
        for (IAtom atom : submatch.getAtomContainer().atoms()){
            System.out.println("Atom: " + mol.getAtomContainer().getAtomNumber( atom ));
        }
//        assertTrue( submatch.getAtomContainer().ccontains( 1 ) );
//        assertTrue( submatch.getMatchingAtoms().contains( 5 ) );
//        assertTrue( submatch.getMatchingAtoms().contains( 8 ) );
//        assertFalse( submatch.getMatchingAtoms().contains( 2 ) );

    }

    
    @Test
    public void runSignSicTestInJob() throws BioclipseException, DSException, InterruptedException{

        IDSManager ds = Activator.getDefault().getJavaManager();
        ICDKManager cdk=net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();

        final ICDKMolecule mol = cdk.fromSMILES( "C1CCCCC1CC(CCC)CCC" );
        ds.runTest( "signsic.bursi", mol, new BioclipseUIJob<List<ITestResult>>(){

            @Override
            public void runInUI() {
                List<ITestResult> ret = getReturnValue();
                assertNotNull( ret);

                assertNotNull( ret);

                System.out.println("=============================");
                System.out.println("Results runSmartsBursiInclexclTestInJob:");
                for (ITestResult res : ret){
                    System.out.println(res);
                }
                System.out.println("=============================");

                //Some QA
                assertTrue( ret.size()==1 );
                ITestResult testres = ret.get( 0 );
                
                assertTrue( testres instanceof ISubStructure );
                ISubStructure submatch=(ISubStructure)testres;
                
                assertEquals( "HIT 1", testres.getName());
                for (IAtom atom : submatch.getAtomContainer().atoms()){
                    System.out.println("Atom: " + mol.getAtomContainer().getAtomNumber( atom ));
                }
//                assertTrue( submatch.getAtomContainer().ccontains( 1 ) );
//                assertTrue( submatch.getMatchingAtoms().contains( 5 ) );
//                assertTrue( submatch.getMatchingAtoms().contains( 8 ) );
//                assertFalse( submatch.getMatchingAtoms().contains( 2 ) );

                
            }

        });
        

    }

}
