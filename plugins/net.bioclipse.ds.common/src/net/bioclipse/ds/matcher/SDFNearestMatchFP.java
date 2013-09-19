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
package net.bioclipse.ds.matcher;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.domain.IBioObject;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.ExternalMoleculeMatch;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.CDKConstants;


/**
 * FP implementation for exact matches in SDFile where tanimoto=1
 * 
 * @author ola
 *
 */
public class SDFNearestMatchFP extends BaseSDFMatcher implements IDSTest{

    private static final String CDK_FP_PROPERTY_KEY="CDK Fingerprint";
    private static final String TANIMOTO_PARAMETER="distance.tanimoto";
    private static final String INCLUDE_EXACT_PARAMETER="includeExact";
    
    private static final Logger logger = Logger.getLogger(SDFNearestMatchFP.class);
    DecimalFormat formatter = new DecimalFormat( "0.00" );

    private float tanimoto;
	private boolean includeExact=false;

    /**
     * Read tanimoto on initialization
     */
    @Override
    public void initialize( IProgressMonitor monitor ) throws DSException {
        super.initialize( monitor );

        //Read the params from test (defined in extension in manifest)
        String tanimotoString=getParameters().get( TANIMOTO_PARAMETER );
        if (tanimotoString!=null && tanimotoString.length()>0){
            //Parse tanimoto string into a Float
            tanimoto=Float.parseFloat( tanimotoString );
        }else{
            logger.error("Error parsing required parameter: " 
                         + TANIMOTO_PARAMETER + " in test: " + getId());
        }
        
        //Optional field
        String includeExactStr = getParameters().get( INCLUDE_EXACT_PARAMETER );
        if (includeExactStr!=null && includeExactStr.length()>0){
        	try{
        	includeExact=Boolean.parseBoolean(includeExactStr);
        	}catch (Exception e){
        		logger.equals("Could not parse includeExact parameter: " + includeExactStr);
        	}
        }

    }
   
    
    /**
     * Adds CDK FP as required property in SDFile
     */
    @Override
    List<String> getRequiredProperties() {

        //Add fingerprint property, we need this in the impl below
        List<String> ret=new ArrayList<String>();
        ret.add(CDK_FP_PROPERTY_KEY);
        return ret;    
    }
    
    /**
     * Add the tanimoto parameter
     */
    @Override
	public List<String> getRequiredParameters() {
    
        List<String> ret=super.getRequiredParameters();
        ret.add( TANIMOTO_PARAMETER );
        return ret;
    }

    /**
     * FP implementation for finding exact matches in an SDFModel
     */
	protected List<? extends ITestResult> doRunTest(IBioObject input,
			IProgressMonitor monitor) {
		
		if (!(input instanceof ICDKMolecule))
			return returnError("Input is not a Molecule", "");
		ICDKMolecule cdkmol = (ICDKMolecule) input;
    	
//    	//We divide in 5 parts
//    	monitor.beginTask(getName(), 5);

        //Store results here
        ArrayList<ExternalMoleculeMatch> results=new 
                                             ArrayList<ExternalMoleculeMatch>();

        //The CDK manager is needed for tanimoto
        ICDKManager cdk=Activator.getDefault().getJavaCDKManager();

        try {
            //Start by searching for inchiKey
            //================================
            BitSet molFP = cdkmol.getFingerprint( IMolecule.Property.
                                                  USE_CALCULATED );
            logger.debug( "FP to search for: " + molFP);
            logger.debug( "Molecule to search for: " + cdkmol);
            int noMols=getSDFmodel().getNumberOfMolecules();
            logger.debug( "# molecules to search: " + noMols);

//            int workedLimit=noMols/5;

            //Search the index for this FP
            for (int i=0; i<noMols; i++){
                BitSet dbFP = getSDFmodel().getPropertyFor( i, CDK_FP_PROPERTY_KEY);
                //Null check not required since verified in initialize()

                if (dbFP==null){
                    logger.error( "Expected property: " + CDK_FP_PROPERTY_KEY + 
                                 " could not be read in SDFModel for " +
                                 "index: " + i);
                }else if (dbFP.size()!=molFP.size()){
                    logger.error( "Index " + i + " in DB has FP size=" 
                                 + dbFP.size() + 
                                 " but molecule searched for has FP size=" 
                                 + molFP.size());
                }else{

                    float calcTanimoto = cdk.calculateTanimoto( dbFP, molFP );
                    
                    if (calcTanimoto == 1 && includeExact==false){
                        //Skip if one
                        logger.debug("Skipped tanimoto 1 in FP Nearest " +
                        		"Neighbor matcher.");
                    }
                    else if (calcTanimoto >= tanimoto){

                        //A hit found
                        ICDKMolecule matchmol = getSDFmodel().getMoleculeAt( i );
                        String molResponse = getSDFmodel().getPropertyFor( i, 
                                                         getResponseProperty());
                        String cdktitle=(String) matchmol
                          .getAtomContainer().getProperty( CDKConstants.TITLE );
                        
                        String molname="Index " + i;
                        if (cdktitle!=null)
                            molname=cdktitle;

                        ExternalMoleculeMatch match =null;

                        if (isClassification){
                        	match = new ExternalMoleculeMatch(molname + 
                        			" (tanimoto=" + formatter.format(calcTanimoto), matchmol, 
                        			getConclusion(molResponse));
                        }else{
                        	match = new ExternalMoleculeMatch(molname + 
                        			" (tanimoto=" + formatter.format(calcTanimoto) + "), value=" + molResponse, matchmol, 
                        			getConclusion(molResponse));
                        }
                        
						Map<String, Map<String, String>> categories = new HashMap<String, Map<String,String>>();
						Map<String,String> props = new HashMap<String, String>();
                        props.put("Observed value" , molResponse);
                        categories.put("Observations", props);
                        match.setProperties(categories);
                        
                        results.add( match);
                    }
                }

                if (monitor.isCanceled())
                    return returnError( "Cancelled","");
//                if (i%workedLimit==0){
//                	monitor.worked(1);
//                	System.out.println("processed: " + i);
//                }

            }
            
        } catch ( Exception e ) {
            LogUtils.debugTrace( logger, e );
            return returnError( "Test failed: " , e.getMessage());
        }
        
        return results;
    }
    
}
