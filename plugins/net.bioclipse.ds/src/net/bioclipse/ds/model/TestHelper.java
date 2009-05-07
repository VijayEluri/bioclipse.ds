package net.bioclipse.ds.model;

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.impl.DSException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;


public class TestHelper {

    private static final Logger logger = Logger.getLogger(TestHelper.class);

    public static List<IDSTest> readTestsFromEP(){

        List<IDSTest> retlist = new ArrayList<IDSTest>();

        IExtensionRegistry registry = Platform.getExtensionRegistry();

        if ( registry == null ) return retlist;
        // it likely means that the Eclipse workbench has not
        // started, for example when running tests

        IExtensionPoint serviceObjectExtensionPoint = registry
        .getExtensionPoint("net.bioclipse.ds.test");

        IExtension[] serviceObjectExtensions
        = serviceObjectExtensionPoint.getExtensions();

        for(IExtension extension : serviceObjectExtensions) {
            for( IConfigurationElement element
                    : extension.getConfigurationElements() ) {

                if (element.getName().equals("test")){

                    String pname=element.getAttribute("name");

                    Object obj;
                    try {
                        obj = element.createExecutableExtension("class");
                        if (obj instanceof IDSTest){

                            IDSTest test=(IDSTest)obj;

                            test.setName(pname);
                            String pid=element.getAttribute("id");
                            test.setId(pid);
                            String picon=element.getAttribute("icon");
                            test.setIcon(picon);
                            
                            String pluginID=element.getNamespaceIdentifier();
                            test.setPluginID( pluginID );
                            
                            for( IConfigurationElement subelement
                                    : element.getChildren() ) {
                                if ("resource".equals( subelement.getName() )){
                                    String name=subelement.getAttribute( "name" );
                                    String path=subelement.getAttribute( "path" );
                                    test.addParameter(name,path);
                                }
                            }
                            retlist.add( test );

                            logger.debug("Added Decision support Test from EP: " + element.
                                         getAttribute("name") + " to " + pname);
                        }else{
                            logger.error("WarningTest class " + pname + " must implement IWarningTest ");
                        }
                    } catch ( CoreException e ) {
                        logger.error("Error creating class for :" + pname +": " + e.getLocalizedMessage() );
                    }

                }
            }
        }

        return retlist;
    }

    public static void runTests( List<TestRun> testRuns ) {

        List<String> errorStrings=new ArrayList<String>();
        
        for (TestRun testrun : testRuns){

            System.out.println("Running testrun: " + testrun);

            logger.debug( "Before run: " + testrun);
            
            IMolecule mol=testrun.getMol();
            IDSTest test = testrun.getTest();
            
            //Should run test on mol and produce matches
            try {
                List<ITestResult> matches = test.runWarningTest(mol, testrun);
                testrun.setMatches( matches );
                testrun.setRun( true );
            } catch ( DSException e ) {
                errorStrings.add( e.getMessage() );
            }

            logger.debug( "After run: " + testrun);

        }
        
        if (errorStrings.size()>0){
            String emsg="The following errors were detected:\n\n";
            for (String ms : errorStrings){
                emsg=emsg+ms+"\n";
            }
            logger.error( emsg );
        }
        
    }

}
