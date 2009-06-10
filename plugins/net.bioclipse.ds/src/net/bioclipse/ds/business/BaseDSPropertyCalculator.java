package net.bioclipse.ds.business;

import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.ui.sdfeditor.business.IPropertyCalculator;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TestRun;

import org.apache.log4j.Logger;


public abstract class BaseDSPropertyCalculator implements IPropertyCalculator<TestRun> {

    private static final Logger logger = Logger.getLogger(
                                               BaseDSPropertyCalculator.class);

    public abstract String getTestID();
    public abstract String getPropertyName();

    public TestRun calculate( ICDKMolecule molecule ) {

        IDSManager ds = net.bioclipse.ds.Activator.getDefault().getJavaManager();
        try {
            List<ITestResult> results = ds.runTest( getTestID(), molecule );
            TestRun tr= new TestRun();
            for (ITestResult result : results){
                tr.addResult( result );
                result.setTestRun( tr );
            }
            
            return tr;
        } catch ( BioclipseException e ) {
            LogUtils.handleException( e, logger, Activator.PLUGIN_ID);
        }
        return null;
    }


    public TestRun parse( String value ) {

        //We do not parse these results (at least not now)
        return new TestRun();
    }

    public String toString( Object value ) {

        @SuppressWarnings("unchecked")
        List<ITestResult> results = (List<ITestResult>)value;

        //No results = negative
        if (results.size()<=0){
            return "NEGATIVE";
        }

        //Serialize consensus
        TestRun tr = results.get( 0 ).getTestRun();
        return tr.getConsensusString();
    }

}
